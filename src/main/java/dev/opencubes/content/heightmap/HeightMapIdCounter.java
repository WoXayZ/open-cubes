package dev.opencubes.content.heightmap;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

/** Allocates unique height-map ids for a dimension's data storage. */
public class HeightMapIdCounter extends SavedData {

    public static final String STORAGE_NAME = "opencubes_height_map_ids";

    private int nextId;

    public static Factory<HeightMapIdCounter> factory() {
        return new Factory<>(HeightMapIdCounter::new, HeightMapIdCounter::load);
    }

    public static HeightMapIdCounter load(CompoundTag tag, HolderLookup.Provider provider) {
        HeightMapIdCounter counter = new HeightMapIdCounter();
        counter.nextId = tag.getInt("NextId");
        return counter;
    }

    public int nextId() {
        int id = nextId++;
        setDirty();
        return id;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("NextId", nextId);
        return tag;
    }
}
