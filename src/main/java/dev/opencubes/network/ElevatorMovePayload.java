package dev.opencubes.network;

import dev.opencubes.OCConstants;
import dev.opencubes.content.elevator.ElevatorTravel;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * "I jumped" or "I crouched" while standing on an elevator. The client cannot be trusted with
 * anything more than that, so it sends the intent and the server decides what happens.
 */
public record ElevatorMovePayload(boolean up) implements CustomPacketPayload {

    public static final Type<ElevatorMovePayload> TYPE = new Type<>(OCConstants.id("elevator_move"));

    public static final StreamCodec<ByteBuf, ElevatorMovePayload> STREAM_CODEC =
            ByteBufCodecs.BOOL.map(ElevatorMovePayload::new, ElevatorMovePayload::up);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ElevatorMovePayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ElevatorTravel.travel(player, payload.up() ? Direction.UP : Direction.DOWN);
        }
    }
}
