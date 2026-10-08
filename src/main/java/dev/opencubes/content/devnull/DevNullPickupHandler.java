package dev.opencubes.content.devnull;

import dev.opencubes.OCConstants;
import dev.opencubes.registry.OCItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

/**
 * The slot keeps one example of the item. A matching pickup is deleted instead of stored.
 */
@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class DevNullPickupHandler {

    private DevNullPickupHandler() {}

    @SubscribeEvent
    public static void onPickup(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();
        ItemStack pickup = event.getItemEntity().getItem();
        if (pickup.isEmpty()) {
            return;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.is(OCItems.DEV_NULL.get())) {
                continue;
            }
            ItemStack filter = DevNullItem.getContained(stack);
            if (filter.isEmpty() || !ItemStack.isSameItemSameComponents(filter, pickup)) {
                continue;
            }

            pickup.setCount(0);
            // Zeroed stack + deny pickup; do not discard mid-event (that crashed).
            event.setCanPickup(TriState.FALSE);
            return;
        }
    }
}
