package dev.opencubes.content.heightmap;

import dev.opencubes.registry.OCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class HeightMapProjectorBlockEntity extends BlockEntity {

    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof HeightMapItem || stack.getItem() instanceof EmptyMapItem;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            onMapSlotChanged();
        }
    };

    private int mapId = -1;
    private byte rotation;

    public HeightMapProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.HEIGHT_MAP_PROJECTOR.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int mapId() {
        return mapId;
    }

    public byte rotation() {
        return rotation;
    }

    public void rotate(int delta) {
        rotation = (byte) ((rotation + delta) & 0x3);
        setChanged();
        sync();
    }

    @Nullable
    public HeightMapData getMap() {
        if (level == null || mapId < 0) {
            return null;
        }
        HeightMapData data = HeightMapManager.getMapData(level, mapId);
        if (level.isClientSide() && data.isEmpty()) {
            HeightMapManager.requestMapData(level, mapId);
        }
        return data.isValid() ? data : null;
    }

    private void onMapSlotChanged() {
        if (level == null || level.isClientSide()) {
            return;
        }
        ItemStack stack = items.getStackInSlot(0);
        if (stack.getCount() == 1) {
            if (stack.getItem() instanceof EmptyMapItem && level instanceof ServerLevel serverLevel) {
                ItemStack upgraded = EmptyMapItem.upgradeToMap(serverLevel, stack);
                items.setStackInSlot(0, upgraded);
                stack = upgraded;
            }
            if (stack.getItem() instanceof HeightMapItem) {
                mapId = HeightMapItem.getMapId(stack);
            } else {
                mapId = -1;
            }
        } else {
            mapId = -1;
        }
        boolean active = mapId >= 0;
        BlockState state = getBlockState();
        if (state.getValue(HeightMapProjectorBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(HeightMapProjectorBlock.ACTIVE, active), 3);
        }
        setChanged();
        sync();
    }

    public AABB renderBoundingBox() {
        return new AABB(worldPosition).expandTowards(0, 5, 0).inflate(1);
    }

    private void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        items.serialize(tag.child("Items"));
        tag.putInt("MapId", mapId);
        tag.putByte("Rotation", rotation);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        if (tag.keySet().contains("Items")) {
            tag.child("Items").ifPresent(items::deserialize);
        }
        mapId = tag.getIntOr("MapId", 0);
        rotation = tag.getByteOr("Rotation", (byte) 0);
        if (level != null && level.isClientSide() && mapId >= 0) {
            HeightMapData data = HeightMapManager.getMapData(level, mapId);
            if (data.isEmpty()) {
                HeightMapManager.requestMapData(level, mapId);
            }
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
