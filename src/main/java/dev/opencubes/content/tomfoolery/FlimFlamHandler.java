package dev.opencubes.content.tomfoolery;

import dev.opencubes.OCConstants;
import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCAttachments;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class FlimFlamHandler {

    public static final ResourceKey<Enchantment> FLIM_FLAM =
            ResourceKey.create(Registries.ENCHANTMENT, OCConstants.id("flim_flam"));

    public static final int LUCK_MARGIN = -30;
    public static final int EFFECT_DELAY = 20 * 15;

    private static final Logger LOGGER = LoggerFactory.getLogger(FlimFlamHandler.class);

    private FlimFlamHandler() {}

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer target) || target.level().isClientSide) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer source) || source == target) {
            return;
        }
        if (!OCCommonConfig.TOMFOOLERY_ENABLED.get() || !OCCommonConfig.FLIM_FLAM_ENCHANT_ENABLED.get()) {
            return;
        }

        int sourceLevel = toolLevel(source);
        int targetLevel = armorLevel(target);
        int diff = targetLevel / 3 - sourceLevel;
        if (diff == 0) {
            return;
        }

        ServerPlayer victim = diff > 0 ? source : target;
        int rolls = Math.abs(diff);
        LuckState luck = victim.getData(OCAttachments.LUCK.get());
        for (int i = 0; i < rolls; i++) {
            int roll = victim.getRandom().nextInt(20) + 1;
            if (roll == 20) {
                luck.forceNext = true;
            }
            luck.luck -= roll;
        }
        if (luck.luck < LUCK_MARGIN) {
            luck.forceNext = true;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }
        if (!OCCommonConfig.TOMFOOLERY_ENABLED.get()) {
            return;
        }
        if (player.tickCount % 20 == 0) {
            deliverKarma(player);
        }
    }

    public static boolean deliverKarma(ServerPlayer player) {
        if (!player.isAlive()) {
            return false;
        }
        LuckState property = player.getData(OCAttachments.LUCK.get());
        if (!canFlimFlam(property, player)) {
            return false;
        }

        int luck = property.luck;
        int totalWeight = 0;
        List<FlimFlamDescription> selected = new ArrayList<>();
        for (FlimFlamDescription effect : FlimFlamRegistry.INSTANCE.all()) {
            if (effect.canApply(luck) && !FlimFlamRegistry.INSTANCE.isBlocked(effect)) {
                selected.add(effect);
                totalWeight += effect.weight();
            }
        }
        if (selected.isEmpty()) {
            return false;
        }
        Collections.shuffle(selected, new java.util.Random(player.getRandom().nextLong()));

        while (!selected.isEmpty() && totalWeight > 0) {
            int selectedWeight = player.getRandom().nextInt(totalWeight);
            int current = 0;
            Iterator<FlimFlamDescription> it = selected.iterator();
            while (it.hasNext()) {
                FlimFlamDescription meta = it.next();
                current += meta.weight();
                if (selectedWeight <= current) {
                    try {
                        if (meta.action().execute(player)) {
                            property.luck -= meta.cost();
                            LOGGER.debug("Player {} flim-flammed with {}, luck={}",
                                    player.getGameProfile().getName(), meta.name(), property.luck);
                            if (!meta.silent()) {
                                player.displayClientMessage(Component.translatable("opencubes.misc.flim_flammed"), true);
                            }
                            return true;
                        }
                    } catch (Throwable t) {
                        LOGGER.warn("Error during flimflam '{}'", meta.name(), t);
                    }
                    totalWeight -= meta.weight();
                    it.remove();
                    break;
                }
            }
        }
        return false;
    }

    public static boolean forceEffect(ServerPlayer player, String name) {
        FlimFlamDescription meta = FlimFlamRegistry.INSTANCE.byName(name);
        if (meta == null || FlimFlamRegistry.INSTANCE.isBlocked(meta)) {
            return false;
        }
        return executeForced(player, meta);
    }

    /** Command helper: ignore luck thresholds, still respect config blocklists. */
    public static boolean forceRandom(ServerPlayer player) {
        List<FlimFlamDescription> pool = new ArrayList<>();
        for (FlimFlamDescription effect : FlimFlamRegistry.INSTANCE.all()) {
            if (!FlimFlamRegistry.INSTANCE.isBlocked(effect)) {
                pool.add(effect);
            }
        }
        if (pool.isEmpty()) {
            return false;
        }
        Collections.shuffle(pool, new java.util.Random(player.getRandom().nextLong()));
        for (FlimFlamDescription meta : pool) {
            if (executeForced(player, meta)) {
                return true;
            }
        }
        return false;
    }

    private static boolean executeForced(ServerPlayer player, FlimFlamDescription meta) {
        try {
            if (meta.action().execute(player)) {
                LuckState luck = player.getData(OCAttachments.LUCK.get());
                luck.luck -= meta.cost();
                if (!meta.silent()) {
                    player.displayClientMessage(Component.translatable("opencubes.misc.flim_flammed"), true);
                }
                return true;
            }
        } catch (Throwable t) {
            LOGGER.warn("Error forcing flimflam '{}'", meta.name(), t);
        }
        return false;
    }

    public static int getLuck(Player player) {
        return player.getData(OCAttachments.LUCK.get()).luck;
    }

    public static int modifyLuck(Player player, int amount) {
        LuckState state = player.getData(OCAttachments.LUCK.get());
        state.luck += amount;
        return state.luck;
    }

    private static boolean canFlimFlam(LuckState property, ServerPlayer player) {
        if (property.forceNext) {
            property.forceNext = false;
            property.cooldown = EFFECT_DELAY;
            return true;
        }
        if (property.luck > -LUCK_MARGIN || property.cooldown-- > 0) {
            return false;
        }
        property.cooldown = EFFECT_DELAY;
        double probability = 0.75 * 2.0 * Math.abs(Math.atan(property.luck / 250.0) / Math.PI);
        return player.getRandom().nextDouble() < probability;
    }

    private static int toolLevel(Player player) {
        return enchantLevel(player, player.getMainHandItem());
    }

    private static int armorLevel(Player player) {
        int sum = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                sum += enchantLevel(player, player.getItemBySlot(slot));
            }
        }
        return sum;
    }

    private static int enchantLevel(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        Optional<Holder.Reference<Enchantment>> holder =
                player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(FLIM_FLAM);
        return holder.map(h -> EnchantmentHelper.getItemEnchantmentLevel(h, stack)).orElse(0);
    }
}
