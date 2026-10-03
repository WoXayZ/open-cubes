package dev.opencubes.content.enchant;

import dev.opencubes.OCConstants;
import dev.opencubes.config.OCCommonConfig;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class UnstableEnchantmentHandler {

    public static final ResourceKey<Enchantment> UNSTABLE =
            ResourceKey.create(Registries.ENCHANTMENT, OCConstants.id("unstable"));

    private UnstableEnchantmentHandler() {}

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!OCCommonConfig.UNSTABLE_ENABLED.get()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        if (event.getDistance() <= 4.0F || player.isShiftKeyDown()) {
            return;
        }
        int level = enchantLevel(player, EquipmentSlot.FEET);
        if (level <= 0) {
            return;
        }
        if (!consumeGunpowder(player, level) && !player.getAbilities().instabuild) {
            return;
        }
        event.setCanceled(true);
        explode(player, level, EquipmentSlot.FEET);
        player.setDeltaMovement(player.getDeltaMovement().x, 0.6D + 0.15D * level, player.getDeltaMovement().z);
        player.hurtMarked = true;
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (!OCCommonConfig.UNSTABLE_ENABLED.get()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        DamageSource source = event.getSource();
        if (source.getEntity() == null && source.getDirectEntity() == null) {
            return;
        }
        EquipmentSlot slot = bestArmorSlot(player);
        if (slot == null) {
            return;
        }
        int level = enchantLevel(player, slot);
        if (level <= 0) {
            return;
        }
        if (!consumeGunpowder(player, level) && !player.getAbilities().instabuild) {
            return;
        }
        explode(player, level, slot);
    }

    private static EquipmentSlot bestArmorSlot(Player player) {
        for (EquipmentSlot slot : new EquipmentSlot[] {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        }) {
            if (enchantLevel(player, slot) > 0) {
                return slot;
            }
        }
        return null;
    }

    private static int enchantLevel(LivingEntity entity, EquipmentSlot slot) {
        ItemStack stack = entity.getItemBySlot(slot);
        if (stack.isEmpty() || entity.level().registryAccess() == null) {
            return 0;
        }
        Optional<Holder.Reference<Enchantment>> holder =
                entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(UNSTABLE);
        return holder.map(enchantment -> EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack)).orElse(0);
    }

    private static boolean consumeGunpowder(Player player, int level) {
        int needed = switch (level) {
            case 1 -> 1;
            case 2 -> 2;
            default -> 4;
        };
        int found = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.GUNPOWDER)) {
                found += stack.getCount();
            }
        }
        if (found < needed) {
            return false;
        }
        int remaining = needed;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.GUNPOWDER)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
            }
        }
        return true;
    }

    private static void explode(Player player, int level, EquipmentSlot armorSlot) {
        Level world = player.level();
        if (!(world instanceof ServerLevel server)) {
            return;
        }
        boolean grief = level >= 3 && OCCommonConfig.UNSTABLE_GRIEF.get();
        server.explode(player, player.getX(), player.getY(), player.getZ(),
                1.5F + level * 0.5F,
                grief ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
        if (!player.getAbilities().instabuild) {
            ItemStack armor = player.getItemBySlot(armorSlot);
            armor.hurtAndBreak(level * 4, player, armorSlot);
        }
    }
}
