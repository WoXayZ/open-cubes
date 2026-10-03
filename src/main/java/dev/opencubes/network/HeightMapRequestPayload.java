package dev.opencubes.network;

import dev.opencubes.OCConstants;
import dev.opencubes.content.heightmap.HeightMapManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record HeightMapRequestPayload(int[] mapIds) implements CustomPacketPayload {

    public static final Type<HeightMapRequestPayload> TYPE = new Type<>(OCConstants.id("height_map_request"));

    public static final StreamCodec<ByteBuf, HeightMapRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()).map(
                    list -> list.stream().mapToInt(Integer::intValue).toArray(),
                    arr -> java.util.Arrays.stream(arr).boxed().toList()),
            HeightMapRequestPayload::mapIds,
            HeightMapRequestPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HeightMapRequestPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            HeightMapManager.handleRequest(player, payload.mapIds());
        }
    }
}
