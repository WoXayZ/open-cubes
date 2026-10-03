package dev.opencubes.network;

import dev.opencubes.OCConstants;
import dev.opencubes.content.paint.PaintMixerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Colour picked in the Paint Mixer screen. Button ids only carry a small int, so RGB needs its own
 * packet. When {@code startMix} is true the server sets the colour and begins crafting (Mix button).
 */
public record SetPaintColorPayload(BlockPos pos, int rgb, boolean startMix) implements CustomPacketPayload {

    public static final Type<SetPaintColorPayload> TYPE = new Type<>(OCConstants.id("set_paint_color"));

    public static final StreamCodec<FriendlyByteBuf, SetPaintColorPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeBlockPos(payload.pos());
                        buf.writeInt(payload.rgb());
                        buf.writeBoolean(payload.startMix());
                    },
                    buf -> new SetPaintColorPayload(buf.readBlockPos(), buf.readInt(), buf.readBoolean()));

    public SetPaintColorPayload(BlockPos pos, int rgb) {
        this(pos, rgb, false);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetPaintColorPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (!player.level().isLoaded(payload.pos())
                || player.distanceToSqr(payload.pos().getCenter()) > 64.0D) {
            return;
        }
        if (player.level().getBlockEntity(payload.pos()) instanceof PaintMixerBlockEntity mixer) {
            if (payload.startMix()) {
                mixer.requestMix(payload.rgb());
            } else {
                mixer.setTargetColor(payload.rgb());
            }
        }
    }
}
