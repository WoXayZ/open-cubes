package dev.opencubes.network;

import dev.opencubes.OCConstants;
import dev.opencubes.content.tomfoolery.BrickManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BrickDropPayload() implements CustomPacketPayload {

    public static final Type<BrickDropPayload> TYPE = new Type<>(OCConstants.id("brick_drop"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BrickDropPayload> STREAM_CODEC =
            StreamCodec.unit(new BrickDropPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BrickDropPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BrickManager.tryDropBrick(player);
            }
        });
    }
}
