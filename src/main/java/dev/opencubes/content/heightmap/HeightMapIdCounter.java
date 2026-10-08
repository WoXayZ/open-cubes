package dev.opencubes.content.heightmap;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Allocates unique height-map ids for a dimension's data storage. */
public class HeightMapIdCounter extends SavedData {

    public static final String STORAGE_NAME = "opencubes_height_map_ids";
    public static final Codec<HeightMapIdCounter> CODEC = CompoundTag.CODEC.xmap(tag -> {
        HeightMapIdCounter counter = new HeightMapIdCounter();
        counter.nextId = tag.getIntOr("NextId", 0);
        return counter;
    }, counter -> {
        CompoundTag tag = new CompoundTag();
        tag.putInt("NextId", counter.nextId);
        return tag;
    });
    public static final SavedDataType<HeightMapIdCounter> TYPE = new SavedDataType<>(
            Identifier.withDefaultNamespace(STORAGE_NAME), HeightMapIdCounter::new, CODEC);

    private int nextId;

    public int nextId() {
        int id = nextId++;
        setDirty();
        return id;
    }
}
