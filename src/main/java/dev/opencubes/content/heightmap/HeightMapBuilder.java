package dev.opencubes.content.heightmap;

import dev.opencubes.content.heightmap.HeightMapData.LayerData;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.MapColor;

/**
 * Port of OpenBlocks {@code MapDataBuilder}: samples loaded chunks into a height map.
 * Heights are stored relative to {@link Level#getMinBuildHeight()}.
 */
public class HeightMapBuilder {

    public final int mapId;
    private HeightMapData data;

    public HeightMapBuilder(int mapId) {
        this.mapId = mapId;
    }

    public void loadMap(ServerLevel level) {
        this.data = HeightMapManager.getOrCreate(level, mapId);
    }

    public void resetMap(ServerLevel level, int x, int z) {
        this.data = HeightMapManager.getOrCreate(level, mapId);
        data.centerX = (x >> 4) << 4;
        data.centerZ = (z >> 4) << 4;
        data.dimension = level.dimension().location().toString();

        if (data.layers == null || data.layers.length != HeightMapData.LAYER_COUNT) {
            data.layers = new LayerData[HeightMapData.LAYER_COUNT];
        }
        if (data.layers[HeightMapData.LAYER_TERRAIN] == null) {
            data.layers[HeightMapData.LAYER_TERRAIN] = new LayerData();
        }
        data.layers[HeightMapData.LAYER_TERRAIN].alpha = (byte) 255;

        if (data.layers[HeightMapData.LAYER_LIQUIDS] == null) {
            data.layers[HeightMapData.LAYER_LIQUIDS] = new LayerData();
        }
        data.layers[HeightMapData.LAYER_LIQUIDS].alpha = (byte) 128;
        HeightMapManager.markDataUpdated(level, mapId);
    }

    public int size() {
        return 4 << data.scale;
    }

    public void resize(BitSet bitmap) {
        bitmap.clear();
        // BitSet grows as bits are set; ensure capacity by setting then clearing high bit.
        int needed = size() * size();
        if (needed > 0) {
            bitmap.set(needed - 1);
            bitmap.clear(needed - 1);
        }
    }

    public void resizeIfNeeded(BitSet bitmap) {
        int needed = size() * size();
        if (bitmap.length() < needed) {
            resize(bitmap);
        }
    }

    public Set<ChunkJob> createJobs(BitSet finishedChunks) {
        Map<ChunkPos, ChunkJob> result = new HashMap<>();
        int blocksPerPixel = 1 << data.scale;
        int pixelsPerChunk = 16 / blocksPerPixel;
        int chunksPerSide = 64 / pixelsPerChunk;

        int middleChunkX = data.centerX >> 4;
        int middleChunkZ = data.centerZ >> 4;

        int bitNum = 0;
        for (int mapX = 0, chunkX = middleChunkX - chunksPerSide / 2;
             chunkX < middleChunkX + chunksPerSide / 2;
             mapX += pixelsPerChunk, chunkX++) {
            for (int mapY = 0, chunkZ = middleChunkZ - chunksPerSide / 2;
                 chunkZ < middleChunkZ + chunksPerSide / 2;
                 mapY += pixelsPerChunk, chunkZ++) {
                if (!finishedChunks.get(bitNum)) {
                    ChunkPos chunk = new ChunkPos(chunkX, chunkZ);
                    result.put(chunk, new ChunkJob(chunk, pixelsPerChunk, mapX, mapY, bitNum));
                }
                bitNum++;
            }
        }
        return new HashSet<>(result.values());
    }

    public static ChunkJob doNextChunk(ServerLevel level, double x, double z, Collection<ChunkJob> jobs) {
        if (jobs.isEmpty()) {
            return null;
        }
        PriorityQueue<JobDistance> distances = new PriorityQueue<>();
        for (ChunkJob job : jobs) {
            ChunkPos chunk = job.chunk;
            double dx = chunk.getMinBlockX() + 7.5 - x;
            double dz = chunk.getMinBlockZ() + 7.5 - z;
            distances.add(new JobDistance(dx * dx + dz * dz, job));
        }
        while (!distances.isEmpty()) {
            ChunkJob job = distances.poll().job;
            LevelChunk chunk = level.getChunkSource().getChunkNow(job.chunk.x, job.chunk.z);
            if (chunk != null && !chunk.isEmpty()) {
                job.mapChunk(level, chunk);
                return job;
            }
        }
        return null;
    }

    public static ItemStack upgradeToMap(ServerLevel level, ItemStack stack) {
        if (stack.getItem() instanceof HeightMapItem) {
            return stack;
        }
        if (stack.getItem() instanceof EmptyMapItem) {
            return EmptyMapItem.upgradeToMap(level, stack);
        }
        throw new IllegalArgumentException("Invalid map item: " + stack.getItem());
    }

    public final class ChunkJob {
        public final ChunkPos chunk;
        public final int pixelsPerChunk;
        public final int mapMinX;
        public final int mapMinY;
        public final int bitNum;

        private ChunkJob(ChunkPos chunk, int pixelsPerChunk, int mapMinX, int mapMinY, int bitNum) {
            this.chunk = chunk;
            this.pixelsPerChunk = pixelsPerChunk;
            this.mapMinX = mapMinX;
            this.mapMinY = mapMinY;
            this.bitNum = bitNum;
        }

        private void mapChunk(ServerLevel level, LevelChunk chunk) {
            LayerData ground = data.layers[HeightMapData.LAYER_TERRAIN];
            LayerData liquid = data.layers[HeightMapData.LAYER_LIQUIDS];
            int blocksPerPixel = 16 / pixelsPerChunk;
            int minY = level.getMinBuildHeight();

            int blockInChunkX = 0;
            for (int mapX = mapMinX; mapX < mapMinX + pixelsPerChunk; mapX++) {
                int blockInChunkZ = 0;
                for (int mapY = mapMinY; mapY < mapMinY + pixelsPerChunk; mapY++) {
                    BlockCount count = new BlockCount();
                    count.average(level, chunk, blockInChunkX, blockInChunkZ, blocksPerPixel, minY);
                    int index = mapY * HeightMapData.SIZE + mapX;
                    ground.colorMap[index] = count.groundColor;
                    ground.heightMap[index] = (byte) count.groundHeight;
                    liquid.colorMap[index] = count.liquidColor;
                    liquid.heightMap[index] = (byte) count.liquidHeight;
                    blockInChunkZ += blocksPerPixel;
                }
                blockInChunkX += blocksPerPixel;
            }
            HeightMapManager.markDataUpdated(level, mapId);
        }
    }

    private record JobDistance(double distance, ChunkJob job) implements Comparable<JobDistance> {
        @Override
        public int compareTo(JobDistance o) {
            return Double.compare(distance, o.distance);
        }
    }

    private static final class BlockCount {
        byte groundColor;
        int groundHeight;
        byte liquidColor;
        int liquidHeight;

        void average(Level level, LevelChunk chunk, int startX, int startZ, int size, int minY) {
            double groundHeightSum = 0;
            int[] groundColors = new int[64];
            double liquidHeightSum = 0;
            int liquidCount = 0;
            int[] liquidColors = new int[64];
            int maxY = level.getMaxBuildHeight() - 1;

            for (int x = startX; x < startX + size; x++) {
                for (int z = startZ; z < startZ + size; z++) {
                    BlockState blockLiquid = null;
                    int heightLiquid = 0;
                    BlockState blockSolid = null;
                    int heightSolid = 0;

                    for (int y = maxY; y >= minY; y--) {
                        BlockPos pos = new BlockPos(chunk.getPos().getMinBlockX() + x, y,
                                chunk.getPos().getMinBlockZ() + z);
                        BlockState state = chunk.getBlockState(pos);
                        if (state.isAir() || state.is(dev.opencubes.registry.OCTags.Blocks.CARTOGRAPHER_INVISIBLE)) {
                            continue;
                        }
                        MapColor mapColor = state.getMapColor(level, pos);
                        if (mapColor == MapColor.NONE) {
                            continue;
                        }
                        if (!state.getFluidState().isEmpty()) {
                            if (blockLiquid == null) {
                                blockLiquid = state;
                                heightLiquid = y;
                            }
                        } else {
                            blockSolid = state;
                            heightSolid = y;
                            break;
                        }
                    }

                    if (blockSolid != null) {
                        groundHeightSum += heightSolid;
                        MapColor color = blockSolid.getMapColor(level,
                                new BlockPos(chunk.getPos().getMinBlockX() + x, heightSolid,
                                        chunk.getPos().getMinBlockZ() + z));
                        int id = color.id;
                        if (id >= 0 && id < groundColors.length) {
                            groundColors[id]++;
                        }
                    }
                    if (blockLiquid != null) {
                        liquidHeightSum += heightLiquid;
                        MapColor color = blockLiquid.getMapColor(level,
                                new BlockPos(chunk.getPos().getMinBlockX() + x, heightLiquid,
                                        chunk.getPos().getMinBlockZ() + z));
                        int id = color.id;
                        if (id >= 0 && id < liquidColors.length) {
                            liquidColors[id]++;
                        }
                        liquidCount++;
                    }
                }
            }

            int maxColorCount = -1;
            for (int i = 0; i < groundColors.length; i++) {
                if (groundColors[i] > maxColorCount) {
                    groundColor = (byte) i;
                    maxColorCount = groundColors[i];
                }
            }
            int avgGround = (int) (groundHeightSum / (size * size));
            groundHeight = Math.max(0, Math.min(255, avgGround - minY));

            if (liquidCount > size * size / 2) {
                maxColorCount = -1;
                for (int i = 0; i < liquidColors.length; i++) {
                    if (liquidColors[i] > maxColorCount) {
                        liquidColor = (byte) i;
                        maxColorCount = liquidColors[i];
                    }
                }
                int avgLiquid = (int) (liquidHeightSum / liquidCount);
                liquidHeight = Math.max(0, Math.min(255, avgLiquid - minY));
            }
        }
    }
}
