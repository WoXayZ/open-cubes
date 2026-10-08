package dev.opencubes.content.grave;

import dev.opencubes.util.ServerLevels;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import dev.opencubes.OCConstants;
import dev.opencubes.compat.curios.CuriosCompat;
import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.content.inventory.PlayerInventoryStore;
import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCGameRules;
import dev.opencubes.util.ExperienceUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.slf4j.Logger;

@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class GraveDeathHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, Integer> PENDING_XP = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<Runnable> NEXT_TICK = new ConcurrentLinkedQueue<>();

    private GraveDeathHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player instanceof FakePlayer) {
            return;
        }
        if (player.level().isClientSide()) {
            return;
        }

        int xp = ExperienceUtil.totalExperience(player);
        PENDING_XP.put(player.getUUID(), xp);
        ExperienceUtil.consume(player, xp);

        if (OCCommonConfig.DUMP_DEAD_PLAYER_INVENTORIES.get()) {
            try {
                var file = PlayerInventoryStore.INSTANCE.storePlayerInventory(player, "death");
                LOGGER.info("Death inventory for {} saved as {}. Restore with /opencubes inventory restore {} {}",
                        player.getGameProfile().name(),
                        file.getFileName(),
                        player.getGameProfile().name(),
                        PlayerInventoryStore.stripFilename(file.getFileName().toString()));
            } catch (Exception e) {
                LOGGER.error("Failed to dump death inventory for {}", player.getGameProfile().name(), e);
            }
        }
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && PENDING_XP.containsKey(player.getUUID())) {
            event.setDroppedExperience(0);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player instanceof FakePlayer) {
            return;
        }
        if (player.level().isClientSide() || !OCCommonConfig.GRAVES_ENABLED.get()) {
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!serverLevel.getGameRules().get(OCGameRules.SPAWN_GRAVES)) {
            return;
        }
        if (player.getAbilities().instabuild) {
            return;
        }
        if (serverLevel.getGameRules().get(GameRules.KEEP_INVENTORY)) {
            return;
        }
        if (event.getDrops().isEmpty()) {
            return;
        }
        List<ItemEntity> lootEntities = new ArrayList<>(event.getDrops());
        if (OCCommonConfig.GRAVES_REQUIRE_ITEM.get() && !consumeGraveFromDrops(lootEntities)) {
            return;
        }
        event.getDrops().clear();

        List<ItemStack> stacks = new ArrayList<>();
        if (CuriosCompat.isLoaded()) {
            CuriosCompat.collectEquipped(player, stacks);
        }
        collectCosmeticArmor(player, stacks);
        for (ItemEntity entity : lootEntities) {
            if (!entity.getItem().isEmpty()) {
                stacks.add(entity.getItem().copy());
            }
        }
        if (stacks.isEmpty()) {
            return;
        }

        int xp = PENDING_XP.getOrDefault(player.getUUID(), 0);
        PENDING_XP.remove(player.getUUID());

        GameProfile profile = player.getGameProfile();
        BlockPos deathPos = player.blockPosition();
        Component deathMessage = buildDeathMessage(player);
        ServerLevel level = ServerLevels.of(player);

        NEXT_TICK.add(() -> placeGrave(level, profile, deathPos, stacks, xp, deathMessage));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Runnable task;
        while ((task = NEXT_TICK.poll()) != null) {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.error("Grave placement task failed", e);
            }
        }
    }

    private static Component buildDeathMessage(ServerPlayer player) {
        Component cause = player.getCombatTracker().getDeathMessage();
        double day = player.level().getGameTime() / 24000.0D;
        MutableComponent dayComponent = Component.literal(String.format("%.1f", day))
                .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD);
        return Component.translatable("opencubes.misc.grave_msg", cause, dayComponent);
    }

    private static void placeGrave(ServerLevel level, GameProfile profile,
                                   BlockPos deathPos, List<ItemStack> stacks, int xp,
                                   Component deathMessage) {
        if (!level.getServer().isRunning()) {
            dropStacks(level, deathPos, stacks);
            return;
        }

        BlockPos gravePos = findLocation(level, deathPos);
        ItemStackHandler loot = PlayerInventoryStore.fromStacks(stacks);

        if (gravePos == null) {
            LOGGER.warn("No grave location for {} - dropping loot and writing backup", profile.name());
            if (OCCommonConfig.GRAVES_BACKUP.get()) {
                PlayerInventoryStore.INSTANCE.storeHandler(loot, profile.name(), "grave", level, meta -> {
                    meta.putString(PlayerInventoryStore.TAG_PLAYER_NAME, profile.name());
                    meta.putString(PlayerInventoryStore.TAG_PLAYER_UUID, profile.id().toString());
                    meta.putBoolean("Placed", false);
                    meta.putInt(PlayerInventoryStore.TAG_XP, xp);
                    meta.putInt("PlayerX", deathPos.getX());
                    meta.putInt("PlayerY", deathPos.getY());
                    meta.putInt("PlayerZ", deathPos.getZ());
                }, level.registryAccess());
            }
            dropStacks(level, deathPos, stacks);
            return;
        }

        if (OCCommonConfig.GRAVES_BASE.get()) {
            BlockPos under = gravePos.below();
            if (level.isEmptyBlock(under)) {
                level.setBlock(under, Blocks.DIRT.defaultBlockState(), 3);
            }
        }

        BlockState graveState = OCBlocks.GRAVE.get().defaultBlockState();
        level.setBlock(gravePos, graveState, 3);
        if (level.getBlockEntity(gravePos) instanceof GraveBlockEntity grave) {
            grave.setUsername(profile.name());
            grave.setLoot(loot);
            grave.setXp(xp);
            grave.setDeathMessage(deathMessage);

            if (OCCommonConfig.GRAVES_BACKUP.get()) {
                PlayerInventoryStore.INSTANCE.storeHandler(loot, profile.name(), "grave", level, meta -> {
                    meta.putString(PlayerInventoryStore.TAG_PLAYER_NAME, profile.name());
                    meta.putString(PlayerInventoryStore.TAG_PLAYER_UUID, profile.id().toString());
                    meta.putBoolean("Placed", true);
                    meta.putInt(PlayerInventoryStore.TAG_XP, xp);
                    meta.putInt("PlayerX", deathPos.getX());
                    meta.putInt("PlayerY", deathPos.getY());
                    meta.putInt("PlayerZ", deathPos.getZ());
                    meta.putInt("GraveX", gravePos.getX());
                    meta.putInt("GraveY", gravePos.getY());
                    meta.putInt("GraveZ", gravePos.getZ());
                }, level.registryAccess());
            }

            LOGGER.info("Grave for {} spawned at {} (died at {}), XP={}",
                    profile.name(), gravePos, deathPos, xp);
        } else {
            LOGGER.warn("Failed to create grave BE at {}", gravePos);
            dropStacks(level, gravePos, stacks);
        }
    }

    private static void dropStacks(Level level, BlockPos pos, List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D,
                        pos.getZ() + 0.5D, stack));
            }
        }
    }

    private static BlockPos findLocation(ServerLevel level, BlockPos origin) {
        BlockPos polite = search(level, origin, false);
        if (polite != null) {
            return polite;
        }
        if (OCCommonConfig.GRAVES_DESTRUCTIVE.get()) {
            return search(level, origin, true);
        }
        return null;
    }

    /** Consumes one grave block from the death drops. Returns false if none were present. */
    private static boolean consumeGraveFromDrops(List<ItemEntity> drops) {
        for (int i = 0; i < drops.size(); i++) {
            ItemStack stack = drops.get(i).getItem();
            if (stack.is(OCBlocks.GRAVE.asItem())) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    drops.remove(i);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Pulls cosmetic armor into the grave via reflection when Cosmetic Armor Reworked is present.
     * TODO: replace with a supported API hook if one is exposed for grave mods.
     */
    private static void collectCosmeticArmor(ServerPlayer player, List<ItemStack> stacks) {
        if (!ModList.get().isLoaded("cosmeticarmorreworked")) {
            return;
        }
        try {
            Class<?> modObjects = Class.forName("lain.mods.cos.impl.ModObjects");
            Field invManField = modObjects.getField("invMan");
            Object inventoryManager = invManField.get(null);
            Method getInventory = inventoryManager.getClass().getMethod("getCosArmorInventory", UUID.class);
            Object inventory = getInventory.invoke(inventoryManager, player.getUUID());
            Method getSlots = inventory.getClass().getMethod("getSlots");
            Method getStackInSlot = inventory.getClass().getMethod("getStackInSlot", int.class);
            Method setStackInSlot = inventory.getClass().getMethod("setStackInSlot", int.class, ItemStack.class);
            int slots = (int) getSlots.invoke(inventory);
            for (int slot = 0; slot < slots; slot++) {
                ItemStack stack = (ItemStack) getStackInSlot.invoke(inventory, slot);
                if (!stack.isEmpty()) {
                    stacks.add(stack.copy());
                    setStackInSlot.invoke(inventory, slot, ItemStack.EMPTY);
                }
            }
        } catch (ReflectiveOperationException e) {
            LOGGER.debug("Could not collect cosmetic armor for grave (Cosmetic Armor Reworked)", e);
        }
    }

    private static BlockPos search(ServerLevel level, BlockPos origin, boolean brutal) {
        int range = OCCommonConfig.GRAVES_SPAWN_RANGE.get() / 2;
        int minY = OCCommonConfig.GRAVES_MIN_Y.get();
        int maxY = OCCommonConfig.GRAVES_MAX_Y.get();
        int baseY = Math.min(Math.max(origin.getY(), minY), maxY);
        BlockPos centre = new BlockPos(origin.getX(), baseY, origin.getZ());

        List<BlockPos> offsets = new ArrayList<>();
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    offsets.add(new BlockPos(x, y, z));
                }
            }
        }
        offsets.sort(Comparator
                .comparingInt((BlockPos p) -> Math.abs(p.getX()) + Math.abs(p.getY()) + Math.abs(p.getZ()))
                .thenComparingInt(p -> -Math.max(Math.max(Math.abs(p.getX()), Math.abs(p.getY())),
                        Math.abs(p.getZ()))));

        for (BlockPos offset : offsets) {
            BlockPos tryPos = centre.offset(offset);
            int y = tryPos.getY();
            if (y < minY || y > maxY || !level.isLoaded(tryPos)) {
                continue;
            }
            BlockState state = level.getBlockState(tryPos);
            if (brutal) {
                if (state.getDestroySpeed(level, tryPos) >= 0.0F && level.getBlockEntity(tryPos) == null) {
                    return tryPos;
                }
            } else if (state.canBeReplaced()) {
                return tryPos;
            }
        }
        return null;
    }
}
