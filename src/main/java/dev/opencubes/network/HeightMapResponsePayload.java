package dev.opencubes.network;

import dev.opencubes.OCConstants;
import dev.opencubes.content.heightmap.HeightMapData;
import dev.opencubes.content.heightmap.HeightMapManager;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record HeightMapResponsePayload(Map<Integer, HeightMapData> maps) implements CustomPacketPayload {

    public static final Type<HeightMapResponsePayload> TYPE = new Type<>(OCConstants.id("height_map_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HeightMapResponsePayload> STREAM_CODEC =
            StreamCodec.of(HeightMapResponsePayload::encode, HeightMapResponsePayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, HeightMapResponsePayload payload) {
        buf.writeVarInt(payload.maps.size());
        for (Map.Entry<Integer, HeightMapData> entry : payload.maps.entrySet()) {
            buf.writeVarInt(entry.getKey());
            entry.getValue().write(buf);
        }
    }

    private static HeightMapResponsePayload decode(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<Integer, HeightMapData> maps = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            int id = buf.readVarInt();
            HeightMapData data = new HeightMapData(id, false);
            data.read(buf);
            maps.put(id, data);
        }
        return new HeightMapResponsePayload(maps);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HeightMapResponsePayload payload, IPayloadContext context) {
        for (Map.Entry<Integer, HeightMapData> entry : payload.maps().entrySet()) {
            HeightMapManager.putClientData(entry.getKey(), entry.getValue());
        }
    }
}
