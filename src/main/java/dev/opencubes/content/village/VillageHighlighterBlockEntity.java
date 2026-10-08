package dev.opencubes.content.village;

import dev.opencubes.util.PlayerFeedback;

import dev.opencubes.registry.OCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class VillageHighlighterBlockEntity extends BlockEntity {

    private static final int SCAN_RANGE = 48;
    private static final double BREED_RATIO = 0.35D;
    private static final int DEFAULT_RADIUS = 32;

    private int villagerCount;
    private int bedCount;
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;
    private boolean hasBounds;
    private long lastScanGameTime = Long.MIN_VALUE;

    public VillageHighlighterBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.VILLAGE_HIGHLIGHTER.get(), pos, state);
        clearBounds();
    }

    public int signalStrength() {
        if (!getBlockState().getValue(VillageHighlighterBlock.POWERED)) {
            return 0;
        }
        return needsBreeding() ? 15 : 0;
    }

    public int villagerCount() {
        return villagerCount;
    }

    public int bedCount() {
        return bedCount;
    }

    public boolean hasBounds() {
        return hasBounds;
    }

    public AABB renderBox() {
        if (!hasBounds) {
            int r = DEFAULT_RADIUS;
            return new AABB(-r, -3, -r, r + 1, 4, r + 1);
        }
        return new AABB(
                minX - worldPosition.getX(),
                minY - worldPosition.getY(),
                minZ - worldPosition.getZ(),
                maxX - worldPosition.getX() + 1,
                maxY - worldPosition.getY() + 1,
                maxZ - worldPosition.getZ() + 1);
    }

    public boolean shouldRenderMarkers() {
        return getBlockState().getValue(VillageHighlighterBlock.POWERED);
    }

    public boolean needsBreeding() {
        return bedCount > 0 && villagerCount < bedCount * BREED_RATIO;
    }

    public void reportTo(Player player) {
        PlayerFeedback.tell(player, Component.translatable(
                "opencubes.misc.village_highlighter.status",
                villagerCount,
                bedCount,
                signalStrength()), true);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VillageHighlighterBlockEntity be) {
        if (!state.getValue(VillageHighlighterBlock.POWERED)) {
            if (be.villagerCount != 0 || be.bedCount != 0 || be.hasBounds) {
                be.villagerCount = 0;
                be.bedCount = 0;
                be.clearBounds();
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
                level.updateNeighborsAt(pos, state.getBlock());
            }
            return;
        }
        if (level.getGameTime() - be.lastScanGameTime < 40) {
            return;
        }
        be.lastScanGameTime = level.getGameTime();
        ServerLevel server = (ServerLevel) level;
        AABB area = new AABB(pos).inflate(SCAN_RANGE);

        var villagers = server.getEntitiesOfClass(Villager.class, area, Villager::isAlive);
        var beds = server.getPoiManager()
                .getInRange(type -> type.is(PoiTypes.HOME), pos, SCAN_RANGE, PoiManager.Occupancy.ANY)
                .toList();

        int nextVillagers = villagers.size();
        int nextBeds = beds.size();

        int nextMinX = Integer.MAX_VALUE;
        int nextMinY = Integer.MAX_VALUE;
        int nextMinZ = Integer.MAX_VALUE;
        int nextMaxX = Integer.MIN_VALUE;
        int nextMaxY = Integer.MIN_VALUE;
        int nextMaxZ = Integer.MIN_VALUE;
        boolean nextHasBounds = false;

        for (var bed : beds) {
            BlockPos bedPos = bed.getPos();
            nextMinX = Math.min(nextMinX, bedPos.getX());
            nextMinY = Math.min(nextMinY, bedPos.getY());
            nextMinZ = Math.min(nextMinZ, bedPos.getZ());
            nextMaxX = Math.max(nextMaxX, bedPos.getX());
            nextMaxY = Math.max(nextMaxY, bedPos.getY());
            nextMaxZ = Math.max(nextMaxZ, bedPos.getZ());
            nextHasBounds = true;
        }
        for (Villager villager : villagers) {
            BlockPos vPos = villager.blockPosition();
            nextMinX = Math.min(nextMinX, vPos.getX());
            nextMinY = Math.min(nextMinY, vPos.getY());
            nextMinZ = Math.min(nextMinZ, vPos.getZ());
            nextMaxX = Math.max(nextMaxX, vPos.getX());
            nextMaxY = Math.max(nextMaxY, vPos.getY());
            nextMaxZ = Math.max(nextMaxZ, vPos.getZ());
            nextHasBounds = true;
        }
        if (nextHasBounds) {
            // Pad so the box reads as a village footprint rather than a tight AABB.
            nextMinX -= 2;
            nextMinY -= 1;
            nextMinZ -= 2;
            nextMaxX += 2;
            nextMaxY += 3;
            nextMaxZ += 2;
        }

        boolean changed = nextVillagers != be.villagerCount
                || nextBeds != be.bedCount
                || nextHasBounds != be.hasBounds
                || (nextHasBounds && (nextMinX != be.minX || nextMinY != be.minY || nextMinZ != be.minZ
                || nextMaxX != be.maxX || nextMaxY != be.maxY || nextMaxZ != be.maxZ));
        if (changed) {
            be.villagerCount = nextVillagers;
            be.bedCount = nextBeds;
            be.hasBounds = nextHasBounds;
            if (nextHasBounds) {
                be.minX = nextMinX;
                be.minY = nextMinY;
                be.minZ = nextMinZ;
                be.maxX = nextMaxX;
                be.maxY = nextMaxY;
                be.maxZ = nextMaxZ;
            } else {
                be.clearBounds();
            }
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            level.updateNeighborsAt(pos, state.getBlock());
        }
    }

    private void clearBounds() {
        hasBounds = false;
        minX = minY = minZ = 0;
        maxX = maxY = maxZ = 0;
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putInt("VillagerCount", villagerCount);
        tag.putInt("BedCount", bedCount);
        tag.putBoolean("HasBounds", hasBounds);
        if (hasBounds) {
            tag.putInt("MinX", minX);
            tag.putInt("MinY", minY);
            tag.putInt("MinZ", minZ);
            tag.putInt("MaxX", maxX);
            tag.putInt("MaxY", maxY);
            tag.putInt("MaxZ", maxZ);
        }
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        if (tag.keySet().contains("VillagerCount")) {
            villagerCount = tag.getIntOr("VillagerCount", 0);
            bedCount = tag.getIntOr("BedCount", 0);
        } else {
            // Legacy saves stored meeting POI counts under MeetingPoints.
            villagerCount = 0;
            bedCount = tag.getIntOr("MeetingPoints", 0);
        }
        hasBounds = tag.getBooleanOr("HasBounds", false);
        if (hasBounds) {
            minX = tag.getIntOr("MinX", 0);
            minY = tag.getIntOr("MinY", 0);
            minZ = tag.getIntOr("MinZ", 0);
            maxX = tag.getIntOr("MaxX", 0);
            maxY = tag.getIntOr("MaxY", 0);
            maxZ = tag.getIntOr("MaxZ", 0);
        } else {
            clearBounds();
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
