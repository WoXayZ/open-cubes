package dev.opencubes.content.paint;

import dev.opencubes.registry.OCBlockEntities;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CanvasBlockEntity extends BlockEntity {

    private final Map<Direction, CanvasFaceData> faces = new EnumMap<>(Direction.class);
    private BlockState paintedBlock = Blocks.AIR.defaultBlockState();

    public CanvasBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.CANVAS.get(), pos, state);
        for (Direction direction : Direction.values()) {
            faces.put(direction, new CanvasFaceData());
        }
    }

    public CanvasFaceData face(Direction direction) {
        return faces.get(direction);
    }

    public BlockState getPaintedBlock() {
        return paintedBlock;
    }

    public void setPaintedBlock(BlockState state) {
        this.paintedBlock = state == null ? Blocks.AIR.defaultBlockState() : state;
        setChanged();
        sync();
    }

    public boolean applyPaint(Direction side, int argb, boolean allSides) {
        boolean changed = false;
        if (allSides) {
            for (Direction direction : Direction.values()) {
                faces.get(direction).applyPaint(argb);
                changed = true;
            }
        } else {
            faces.get(side).applyPaint(argb);
            changed = true;
        }
        if (changed) {
            setChanged();
            sync();
        }
        return changed;
    }

    public boolean placeStencil(Direction side, StencilPattern pattern, int rotation) {
        boolean ok = faces.get(side).putCover(pattern, rotation);
        if (ok) {
            setChanged();
            sync();
        }
        return ok;
    }

    public Optional<StencilPattern> rotateOrPopStencil(Direction side, boolean pop) {
        CanvasFaceData face = faces.get(side);
        Optional<StencilPattern> result;
        if (pop) {
            result = face.popCover();
        } else {
            if (face.cover() == null) {
                return Optional.empty();
            }
            face.rotateCover();
            result = Optional.of(face.cover().pattern());
        }
        setChanged();
        sync();
        return result;
    }

    public Optional<ItemStack> squeegee(Direction side, boolean allSides) {
        Optional<ItemStack> dropped = Optional.empty();
        if (allSides) {
            for (Direction direction : Direction.values()) {
                Optional<StencilPattern> pattern = faces.get(direction).clearAll();
                if (pattern.isPresent() && dropped.isEmpty()) {
                    dropped = Optional.of(StencilItem.create(pattern.get()));
                }
            }
        } else {
            Optional<StencilPattern> pattern = faces.get(side).clearAll();
            if (pattern.isPresent()) {
                dropped = Optional.of(StencilItem.create(pattern.get()));
            }
        }
        setChanged();
        sync();
        return dropped;
    }

    public boolean allFacesEmpty() {
        return faces.values().stream().allMatch(CanvasFaceData::isEmpty);
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("PaintedBlock", NbtUtils.writeBlockState(paintedBlock));
        CompoundTag facesTag = new CompoundTag();
        for (Direction direction : Direction.values()) {
            facesTag.put(direction.getSerializedName(), faces.get(direction).save());
        }
        tag.put("Faces", facesTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("PaintedBlock")) {
            paintedBlock = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("PaintedBlock"));
        }
        CompoundTag facesTag = tag.getCompound("Faces");
        for (Direction direction : Direction.values()) {
            if (facesTag.contains(direction.getSerializedName())) {
                faces.put(direction, CanvasFaceData.load(facesTag.getCompound(direction.getSerializedName())));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        loadAdditional(packet.getTag(), registries);
    }
}
