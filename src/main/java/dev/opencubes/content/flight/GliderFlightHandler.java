package dev.opencubes.content.flight;

import dev.opencubes.OCConstants;
import dev.opencubes.registry.OCAttachments;
import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.registry.OCItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class GliderFlightHandler {

    private GliderFlightHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            copyGliderColour(player);
        }
        if (player.isFallFlying()) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chest.getItem() instanceof ThermalElytraItem) {
                ThermalElytraItem.tickFlight(chest, player);
            }
        }
        GliderState state = player.getData(OCAttachments.GLIDER.get());

        boolean holding = HangGliderItem.isHoldingEngaged(player);
        state.setEngaged(holding);
        if (!holding) {
            return;
        }

        if (!HangGliderPhysics.canDeploy(player)) {
            state.setLastMotionY(player.getDeltaMovement().y);
            return;
        }

        double v = HangGliderPhysics.apply(player, player.level(), state.lastMotionY());
        state.setLastVerticalSpeed(v);
        state.setLastMotionY(v);
    }

    /** Copies hang-glider paint onto the smithing result. */
    private static void copyGliderColour(Player player) {
        if (!(player.containerMenu instanceof SmithingMenu menu)) {
            return;
        }
        ItemStack result = menu.getSlot(SmithingMenu.RESULT_SLOT).getItem();
        if (!result.is(OCItems.THERMAL_ELYTRA.get())) {
            return;
        }
        Integer colour = menu.getSlot(SmithingMenu.ADDITIONAL_SLOT).getItem().get(OCDataComponents.PAINT_COLOR.get());
        if (colour != null && !colour.equals(result.get(OCDataComponents.PAINT_COLOR.get()))) {
            result.set(OCDataComponents.PAINT_COLOR.get(), colour);
        }
    }
}
