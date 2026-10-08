package dev.opencubes.content.heightmap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1.21 saved height maps as {@code data/opencubes_height_map_<id>.dat} beside the world.
 * 26.1 looks them up under {@code data/minecraft/}. This copies a legacy file into the new
 * storage the first time that map is read, before a missing lookup is cached as empty.
 */
public final class HeightMapLegacy {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeightMapLegacy.class);

    private HeightMapLegacy() {}

    public static void importCounter(ServerLevel level) {
        var storage = level.getDataStorage();
        if (storage.get(HeightMapIdCounter.TYPE) != null) {
            return;
        }
        CompoundTag root = read(level, HeightMapIdCounter.STORAGE_NAME);
        if (root == null) {
            return;
        }
        HeightMapIdCounter.CODEC.parse(NbtOps.INSTANCE, payload(root)).resultOrPartial(LOGGER::warn)
                .ifPresent(counter -> storage.set(HeightMapIdCounter.TYPE, counter));
    }

    public static HeightMapData importMap(ServerLevel level, int mapId) {
        var storage = level.getDataStorage();
        HeightMapData existing = storage.get(HeightMapData.type(mapId));
        if (existing != null) {
            return existing;
        }
        CompoundTag root = read(level, HeightMapData.storageName(mapId));
        if (root == null) {
            return null;
        }
        HeightMapData data = HeightMapData.load(mapId, payload(root));
        storage.set(HeightMapData.type(mapId), data);
        return data;
    }

    private static CompoundTag payload(CompoundTag root) {
        return root.getCompound("data").orElse(root);
    }

    private static CompoundTag read(ServerLevel level, String name) {
        Path file = legacyFile(level, name);
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            return NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
        } catch (IOException exception) {
            LOGGER.warn("Could not read legacy height map {}", file, exception);
            return null;
        }
    }

    private static Path legacyFile(ServerLevel level, String name) {
        Path root = level.getServer().getWorldPath(LevelResource.ROOT);
        Identifier dimension = level.dimension().identifier();
        Path folder = dimension.equals(Level.OVERWORLD.identifier())
                ? root.resolve("data")
                : root.resolve("dimensions").resolve(dimension.getNamespace()).resolve(dimension.getPath()).resolve("data");
        return folder.resolve(name + ".dat");
    }
}
