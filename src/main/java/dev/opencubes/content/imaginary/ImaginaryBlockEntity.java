package dev.opencubes.content.imaginary;

import dev.opencubes.registry.OCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class ImaginaryBlockEntity extends BlockEntity {

    @Nullable
    private Integer colour;
    private boolean inverted;
    private ImaginaryShape shape = ImaginaryShape.BLOCK;

    public ImaginaryBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.IMAGINARY.get(), pos, state);
    }

    public boolean isPencil() {
        return colour == null;
    }

    public boolean isInverted() {
        return inverted;
    }

    @Nullable
    public Integer colour() {
        return colour;
    }

    public ImaginaryShape shape() {
        return shape;
    }

    public VoxelShape voxelShape() {
        if (shape == ImaginaryShape.STAIRS) {
            Direction facing = getBlockState().getValue(ImaginaryBlock.FACING);
            return shape.rotated(facing);
        }
        return shape.voxelShape();
    }

    public void configure(@Nullable Integer colour, boolean inverted, ImaginaryShape shape) {
        this.colour = colour;
        this.inverted = inverted;
        this.shape = shape == null ? ImaginaryShape.BLOCK : shape;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean is(ImaginaryProperty property, Player player) {
        if (property == ImaginaryProperty.VISIBLE && player.isSpectator()) {
            return true;
        }
        if (property == ImaginaryProperty.SOLID && isPencil()) {
            return true;
        }

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() instanceof ImaginationGlassesItem glasses) {
            return glasses.kind().check(property, helmet, this);
        }
        return inverted;
    }

    public boolean is(ImaginaryProperty property, Entity entity) {
        return entity instanceof Player player && is(property, player);
    }

    public ItemStack createPickStack() {
        if (isPencil()) {
            return ImaginaryItem.createPencil(shape, inverted, ImaginaryItem.defaultUses());
        }
        return ImaginaryItem.createCrayon(colour == null ? 0xFFFFFF : colour, shape, inverted, ImaginaryItem.defaultUses());
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        if (colour != null) {
            tag.putInt("Color", colour);
        }
        tag.putBoolean("IsInverted", inverted);
        tag.putByte("Shape", (byte) shape.ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        colour = tag.keySet().contains("Color") ? tag.getIntOr("Color", 0) : null;
        inverted = tag.getBooleanOr("IsInverted", false);
        int shapeId = tag.getByteOr("Shape", (byte) 0);
        shape = shapeId >= 0 && shapeId < ImaginaryShape.VALUES.length
                ? ImaginaryShape.VALUES[shapeId]
                : ImaginaryShape.BLOCK;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
