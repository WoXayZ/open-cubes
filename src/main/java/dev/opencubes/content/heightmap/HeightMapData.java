package dev.opencubes.content.heightmap;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 64×64 two-layer terrain scan stored in dimension SavedData as {@code height_map_<id>}.
 * Height bytes are relative to the world's min build height (clamped 0–255).
 */
public class HeightMapData extends SavedData {

    public static final int SIZE = 64;
    public static final int LAYER_TERRAIN = 0;
    public static final int LAYER_LIQUIDS = 1;
    public static final int LAYER_COUNT = 2;

    public static final HeightMapData INVALID = new HeightMapData(-1, true) {
        @Override
        public boolean isValid() {
            return false;
        }
    };

    public static final HeightMapData EMPTY = new HeightMapData(-1, true) {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean isValid() {
            return false;
        }
    };

    public static final class LayerData {
        public byte alpha;
        public final byte[] heightMap = new byte[SIZE * SIZE];
        public final byte[] colorMap = new byte[SIZE * SIZE];

        public void read(CompoundTag tag) {
            alpha = tag.getByte("Alpha");
            copyInto(tag.getByteArray("Height"), heightMap);
            copyInto(tag.getByteArray("Color"), colorMap);
        }

        public void write(CompoundTag tag) {
            tag.putByte("Alpha", alpha);
            tag.putByteArray("Height", heightMap);
            tag.putByteArray("Color", colorMap);
        }

        public void read(RegistryFriendlyByteBuf buf) {
            alpha = buf.readByte();
            buf.readBytes(heightMap);
            buf.readBytes(colorMap);
        }

        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeByte(alpha);
            buf.writeBytes(heightMap);
            buf.writeBytes(colorMap);
        }

        private static void copyInto(byte[] src, byte[] dst) {
            if (src.length == dst.length) {
                System.arraycopy(src, 0, dst, 0, dst.length);
            }
        }
    }

    public LayerData[] layers = new LayerData[0];
    public String dimension = "";
    public int centerX;
    public int centerZ;
    public byte scale;

    private final int mapId;
    private final boolean stub;

    public HeightMapData(int mapId, boolean stub) {
        this.mapId = mapId;
        this.stub = stub;
    }

    public HeightMapData(int mapId) {
        this(mapId, false);
    }

    public int mapId() {
        return mapId;
    }

    public static String storageName(int mapId) {
        return "opencubes_height_map_" + mapId;
    }

    public boolean isValid() {
        return !stub;
    }

    public boolean isEmpty() {
        return false;
    }

    public static Factory<HeightMapData> factory(int mapId) {
        return new Factory<>(
                () -> new HeightMapData(mapId, false),
                (tag, provider) -> load(mapId, tag, provider));
    }

    public static HeightMapData load(int mapId, CompoundTag tag, HolderLookup.Provider provider) {
        HeightMapData data = new HeightMapData(mapId, false);
        data.read(tag);
        return data;
    }

    public void read(CompoundTag tag) {
        dimension = tag.getString("Dimension");
        centerX = tag.getInt("CenterX");
        centerZ = tag.getInt("CenterZ");
        scale = tag.getByte("Scale");
        ListTag layersTag = tag.getList("Layers", Tag.TAG_COMPOUND);
        layers = new LayerData[layersTag.size()];
        for (int i = 0; i < layersTag.size(); i++) {
            LayerData layer = new LayerData();
            layer.read(layersTag.getCompound(i));
            layers[i] = layer;
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString("Dimension", dimension);
        tag.putInt("CenterX", centerX);
        tag.putInt("CenterZ", centerZ);
        tag.putByte("Scale", scale);
        ListTag list = new ListTag();
        for (LayerData layer : layers) {
            CompoundTag layerTag = new CompoundTag();
            layer.write(layerTag);
            list.add(layerTag);
        }
        tag.put("Layers", list);
        return tag;
    }

    public void read(RegistryFriendlyByteBuf buf) {
        dimension = buf.readUtf();
        centerX = buf.readInt();
        centerZ = buf.readInt();
        scale = buf.readByte();
        int length = buf.readVarInt();
        layers = new LayerData[length];
        for (int i = 0; i < length; i++) {
            LayerData layer = new LayerData();
            layer.read(buf);
            layers[i] = layer;
        }
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(dimension);
        buf.writeInt(centerX);
        buf.writeInt(centerZ);
        buf.writeByte(scale);
        buf.writeVarInt(layers.length);
        for (LayerData layer : layers) {
            layer.write(buf);
        }
    }
}
