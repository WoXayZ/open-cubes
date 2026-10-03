package dev.opencubes.network;

import dev.opencubes.OCConstants;
import dev.opencubes.content.heightmap.HeightMapManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record HeightMapDirtyPayload(int[] mapIds) implements CustomPacketPayload {

    public static final Type<HeightMapDirtyPayload> TYPE = new Type<>(OCConstants.id("height_map_dirty"));

    public static final StreamCodec<ByteBuf, HeightMapDirtyPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()).map(
                    list -> list.stream().mapToInt(Integer::intValue).toArray(),
                    arr -> java.util.Arrays.stream(arr).boxed().toList()),
            HeightMapDirtyPayload::mapIds,
            HeightMapDirtyPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HeightMapDirtyPayload payload, IPayloadContext context) {
        HeightMapManager.handleDirtyClient(payload.mapIds());
        PacketDistributor.sendToServer(new HeightMapRequestPayload(payload.mapIds()));
    }
}
