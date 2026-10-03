package dev.opencubes.content.flight;

import dev.opencubes.OCConstants;
import dev.opencubes.registry.OCAttachments;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class GliderFlightHandler {

    private GliderFlightHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
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
}
