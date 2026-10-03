package dev.opencubes.content.luggage;

import dev.opencubes.OCConstants;
import dev.opencubes.registry.OCItems;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;

/** Filled luggage never despawns and resists fire/explosion when on the ground. */
@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class LuggageDropHandler {

    private LuggageDropHandler() {}

    private static boolean isLuggage(ItemStack stack) {
        return stack.is(OCItems.LUGGAGE.get());
    }

    @SubscribeEvent
    public static void onToss(ItemTossEvent event) {
        ItemEntity entity = event.getEntity();
        if (isLuggage(entity.getItem())) {
            entity.setUnlimitedLifetime();
            entity.setInvulnerable(true);
        }
    }

    @SubscribeEvent
    public static void onExpire(ItemExpireEvent event) {
        if (isLuggage(event.getEntity().getItem())) {
            event.getEntity().setUnlimitedLifetime();
            event.addExtraLife(Short.MAX_VALUE - 1);
        }
    }
}
