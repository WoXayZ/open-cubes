package dev.opencubes.content.enchant;

import dev.opencubes.OCConstants;
import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.util.ExperienceUtil;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Last Stand - consume XP instead of dying when a hit would drop you below half a heart.
 * Cost uses post-mitigation damage (§10.2).
 */
@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class LastStandHandler {

    public static final ResourceKey<Enchantment> LAST_STAND =
            ResourceKey.create(Registries.ENCHANTMENT, OCConstants.id("last_stand"));

    private LastStandHandler() {}

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (!OCCommonConfig.LAST_STAND_ENABLED.get()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        float amount = event.getNewDamage();
        if (amount <= 0.0F) {
            return;
        }
        float healthAfter = player.getHealth() - amount;
        if (healthAfter >= 1.0F) {
            return;
        }

        int level = armorLevel(player);
        if (level <= 0) {
            return;
        }

        // Documented curve: xpCost = ceil(postMitigationDamage * 25 / level)
        int xpCost = Math.max(1, (int) Math.ceil(amount * OCCommonConfig.LAST_STAND_XP_PER_DAMAGE.get() / level));
        int available = ExperienceUtil.totalExperience(player);
        if (available < xpCost && !player.getAbilities().instabuild) {
            return;
        }

        if (!player.getAbilities().instabuild) {
            ExperienceUtil.consume(player, xpCost);
        }
        // Leave the player at half a heart.
        float keep = Math.min(amount, player.getHealth() - 1.0F);
        if (keep < 0.0F) {
            keep = 0.0F;
        }
        event.setNewDamage(keep);
    }

    private static int armorLevel(ServerPlayer player) {
        int best = 0;
        Optional<Holder.Reference<Enchantment>> holder =
                player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(LAST_STAND);
        if (holder.isEmpty()) {
            return 0;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
                continue;
            }
            ItemStack stack = player.getItemBySlot(slot);
            best = Math.max(best, EnchantmentHelper.getItemEnchantmentLevel(holder.get(), stack));
        }
        return best;
    }
}
