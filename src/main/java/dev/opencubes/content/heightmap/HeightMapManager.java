package dev.opencubes.content.heightmap;

import dev.opencubes.network.HeightMapDirtyPayload;
import dev.opencubes.network.HeightMapRequestPayload;
import dev.opencubes.network.HeightMapResponsePayload;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Server SavedData access + client cache + dirty-id broadcast for height maps.
 */
public final class HeightMapManager {

    private static final Set<Integer> DIRTY = new HashSet<>();
    private static final Map<Integer, HeightMapData> CLIENT_CACHE = new ConcurrentHashMap<>();
    private static final Set<Integer> CLIENT_PENDING = ConcurrentHashMap.newKeySet();

    private HeightMapManager() {}

    public static int createNewMap(ServerLevel level, byte scale) {
        HeightMapIdCounter counter = level.getDataStorage().computeIfAbsent(
                HeightMapIdCounter.factory(), HeightMapIdCounter.STORAGE_NAME);
        int id = counter.nextId();
        HeightMapData data = level.getDataStorage().computeIfAbsent(
                HeightMapData.factory(id), HeightMapData.storageName(id));
        data.scale = scale;
        data.setDirty();
        return id;
    }

    public static HeightMapData getMapData(Level level, int mapId) {
        if (mapId < 0) {
            return HeightMapData.INVALID;
        }
        if (level.isClientSide) {
            HeightMapData cached = CLIENT_CACHE.get(mapId);
            return cached != null ? cached : HeightMapData.EMPTY;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return HeightMapData.EMPTY;
        }
        // Only return maps that already exist - do not allocate stubs via computeIfAbsent.
        var storage = serverLevel.getDataStorage();
        HeightMapData data = storage.get(HeightMapData.factory(mapId), HeightMapData.storageName(mapId));
        return data != null ? data : HeightMapData.EMPTY;
    }

    public static HeightMapData getOrCreate(ServerLevel level, int mapId) {
        return level.getDataStorage().computeIfAbsent(
                HeightMapData.factory(mapId), HeightMapData.storageName(mapId));
    }

    public static void markDataUpdated(Level level, int mapId) {
        if (level.isClientSide || mapId < 0) {
            return;
        }
        HeightMapData data = getMapData(level, mapId);
        if (data.isValid()) {
            data.setDirty();
            DIRTY.add(mapId);
        }
    }

    public static void sendPendingUpdates(MinecraftServer server) {
        if (DIRTY.isEmpty()) {
            return;
        }
        int[] ids = DIRTY.stream().mapToInt(Integer::intValue).toArray();
        DIRTY.clear();
        PacketDistributor.sendToAllPlayers(new HeightMapDirtyPayload(ids));
    }

    public static void putClientData(int mapId, HeightMapData data) {
        CLIENT_CACHE.put(mapId, data);
        CLIENT_PENDING.remove(mapId);
    }

    public static void invalidateClient(int mapId) {
        CLIENT_CACHE.remove(mapId);
        CLIENT_PENDING.remove(mapId);
    }

    public static void requestMapData(Level level, int mapId) {
        if (!level.isClientSide || mapId < 0) {
            return;
        }
        if (CLIENT_CACHE.containsKey(mapId) || !CLIENT_PENDING.add(mapId)) {
            return;
        }
        PacketDistributor.sendToServer(new HeightMapRequestPayload(new int[]{mapId}));
    }

    public static void handleRequest(ServerPlayer player, int[] mapIds) {
        Map<Integer, HeightMapData> maps = new HashMap<>();
        for (int mapId : mapIds) {
            HeightMapData data = getMapData(player.level(), mapId);
            if (data.isValid() && !data.isEmpty()) {
                maps.put(mapId, data);
            }
        }
        if (!maps.isEmpty()) {
            PacketDistributor.sendToPlayer(player, new HeightMapResponsePayload(maps));
        }
    }

    public static void handleDirtyClient(int[] mapIds) {
        for (int mapId : mapIds) {
            invalidateClient(mapId);
            // Re-request is driven by projector/item tooltip when they notice EMPTY.
        }
    }
}
