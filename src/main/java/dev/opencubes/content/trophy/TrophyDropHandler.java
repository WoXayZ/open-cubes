package dev.opencubes.content.trophy;

import dev.opencubes.OCConstants;
import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * Rare trophy drops on player kills. Chance curve (replaces OpenMods expression engine):
 * {@code (looting + rand/4) * chance - rand > 0}.
 */
@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class TrophyDropHandler {

    private TrophyDropHandler() {}

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!event.isRecentlyHit()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (entityId == null) {
            return;
        }

        var registry = entity.level().registryAccess().lookup(OCRegistries.TROPHY).orElse(null);
        if (registry == null) {
            return;
        }

        Identifier trophyId = null;
        for (var entry : registry.entrySet()) {
            if (entry.getValue().entity().equals(entityId)) {
                trophyId = entry.getKey().identifier();
                break;
            }
        }
        if (trophyId == null) {
            return;
        }

        int looting = 0;
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            looting = EnchantmentHelper.getEnchantmentLevel(
                    attacker.level().registryAccess()
                            .lookupOrThrow(Registries.ENCHANTMENT)
                            .getOrThrow(Enchantments.LOOTING),
                    attacker);
        }

        double chance = OCCommonConfig.TROPHY_DROP_CHANCE.get();
        double bias = entity.getRandom().nextDouble() / 4.0D;
        double selection = entity.getRandom().nextDouble();
        double score = (looting + bias) * chance - selection;
        if (score <= 0.0D) {
            return;
        }

        ItemStack drop = TrophyBlockItem.create(OCBlocks.TROPHY.get(), trophyId);
        ItemEntity item = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), drop);
        item.setDefaultPickUpDelay();
        event.getDrops().add(item);
    }
}
