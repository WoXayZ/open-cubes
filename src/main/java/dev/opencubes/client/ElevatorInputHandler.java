package dev.opencubes.client;

import dev.opencubes.OCConstants;
import dev.opencubes.content.elevator.Elevator;
import dev.opencubes.network.ElevatorMovePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Turns a jump or a crouch into an elevator request.
 *
 * <p>The server never sees a jump: {@code jumpFromGround} only runs on the client for the
 * local player, which is why upstream needed a packet too. What is new is the cheap local
 * check before sending - the client already knows which block it is standing on, so a jump
 * anywhere else costs nothing.
 */
@EventBusSubscriber(modid = OCConstants.MOD_ID, value = Dist.CLIENT)
public final class ElevatorInputHandler {

    private static boolean wasCrouching;

    private ElevatorInputHandler() {}

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof LocalPlayer player && isStandingOnElevator(player)) {
            PacketDistributor.sendToServer(new ElevatorMovePayload(true));
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            wasCrouching = false;
            return;
        }

        boolean crouching = player.input.shiftKeyDown;
        boolean started = crouching && !wasCrouching;
        wasCrouching = crouching;

        if (started && player.onGround() && isStandingOnElevator(player)) {
            PacketDistributor.sendToServer(new ElevatorMovePayload(false));
        }
    }

    private static boolean isStandingOnElevator(Player player) {
        BlockPos below = new BlockPos(
                Mth.floor(player.getX()),
                Mth.floor(player.getBoundingBox().minY) - 1,
                Mth.floor(player.getZ()));
        return player.level().getBlockState(below).getBlock() instanceof Elevator;
    }
}
