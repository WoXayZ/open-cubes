package dev.opencubes.content.grave;

import com.mojang.logging.LogUtils;
import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.registry.OCSounds;
import dev.opencubes.util.ExperienceUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.slf4j.Logger;

public class GraveBlockEntity extends BlockEntity {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_MESSAGE = "Message";

    private String username = "";
    private Component deathMessage;
    private int xp;
    private ItemStackHandler items = new ItemStackHandler(1);

    public GraveBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.GRAVE.get(), pos, state);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? "" : username;
        setChanged();
        sync();
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = Math.max(0, xp);
        setChanged();
    }

    public void setDeathMessage(Component message) {
        this.deathMessage = message == null ? null : message.copy();
        setChanged();
    }

    public void setLoot(ItemStackHandler loot) {
        this.items = loot;
        setChanged();
    }

    public void copyFromPlayer(Player player) {
        ItemStackHandler copy = new ItemStackHandler(player.getInventory().getContainerSize());
        for (int i = 0; i < copy.getSlots(); i++) {
            copy.setStackInSlot(i, player.getInventory().getItem(i).copy());
        }
        setLoot(copy);
        setXp(ExperienceUtil.totalExperience(player));
    }

    public void onActivated(Player player, ItemStack held) {
        if (!held.isEmpty() && held.is(ItemTags.SHOVELS)) {
            robGrave(player, held);
        } else if (deathMessage != null) {
            player.sendSystemMessage(deathMessage);
        } else if (!username.isEmpty()) {
            player.sendSystemMessage(Component.translatable("opencubes.misc.grave_of", username));
        }
    }

    /**
     * Sneak-use: break the grave in place (drop loot + XP via {@code onRemove}), without
     * putting a grave block item into the player's inventory.
     */
    public boolean tryBreakOpen(Player player) {
        if (level == null || level.isClientSide || player.isSpectator()) {
            return false;
        }
        // removeBlock triggers GraveBlock.onRemove -> dropContents (items + XP).
        level.removeBlock(worldPosition, false);
        return true;
    }

    private void robGrave(Player player, ItemStack shovel) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        boolean dropped = false;
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                dropped = true;
                ItemEntity entity = new ItemEntity(level, worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, stack);
                level.addFreshEntity(entity);
                items.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
        if (dropped) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
                    worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D,
                    12, 0.2D, 0.2D, 0.2D, 0.05D);
            if (level.random.nextDouble() < OCCommonConfig.GRAVES_SPECIAL_ACTION.get()) {
                ohNoes(player);
            }
            shovel.hurtAndBreak(2, player, Player.getSlotForHand(player.getUsedItemHand()));
            setChanged();
        }
    }

    private void ohNoes(Player player) {
        level.playSound(null, player.blockPosition(), OCSounds.GRAVE_ROB.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        if (level instanceof ServerLevel serverLevel && serverLevel.getLevelData() instanceof ServerLevelData data) {
            data.setThunderTime(35 * 20);
            data.setRainTime(35 * 20);
            data.setThundering(true);
            data.setRaining(true);
        }
    }

    public void dropContents(ServerLevel level, BlockPos pos) {
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        stack);
                level.addFreshEntity(entity);
                items.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
        int remaining = xp;
        xp = 0;
        while (remaining > 0) {
            int orb = ExperienceOrb.getExperienceValue(remaining);
            remaining -= orb;
            level.addFreshEntity(new ExperienceOrb(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    orb));
        }
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GraveBlockEntity grave) {
        if (!OCCommonConfig.GRAVES_SPAWN_SKELETONS.get() || level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        if (level.random.nextDouble() >= OCCommonConfig.GRAVES_SKELETON_RATE.get()) {
            return;
        }
        List<Mob> hostiles = level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(7.0D),
                mob -> mob.getType() == EntityType.SKELETON || mob.getType() == EntityType.BAT
                        || mob instanceof net.minecraft.world.entity.monster.Enemy);
        if (hostiles.size() >= 5) {
            return;
        }
        Mob living = level.random.nextBoolean()
                ? new Skeleton(EntityType.SKELETON, level)
                : new Bat(EntityType.BAT, level);
        living.moveTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F, 0.0F);
        if (living.checkSpawnRules(level, net.minecraft.world.entity.MobSpawnType.EVENT)
                && living.checkSpawnObstruction(level)) {
            level.addFreshEntity(living);
        }
    }

    private void sync() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().blockChanged(worldPosition);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            loadAdditional(tag, registries);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        username = tag.getString("Username");
        xp = tag.getInt("xp");
        if (tag.contains("Items")) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
        }
        if (tag.contains(TAG_MESSAGE)) {
            deathMessage = Component.Serializer.fromJson(tag.getString(TAG_MESSAGE), registries);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Username", username);
        tag.putInt("xp", xp);
        tag.put("Items", items.serializeNBT(registries));
        if (deathMessage != null) {
            tag.putString(TAG_MESSAGE, Component.Serializer.toJson(deathMessage, registries));
        }
    }
}
