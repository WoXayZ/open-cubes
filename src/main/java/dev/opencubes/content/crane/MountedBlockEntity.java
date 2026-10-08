package dev.opencubes.content.crane;

import dev.opencubes.registry.OCEntities;
import dev.opencubes.util.OCFakePlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * A block temporarily turned into an entity while the crane carries it.
 * Places back into the world when released over free space.
 */
public class MountedBlockEntity extends Entity {

    private static final EntityDataAccessor<BlockState> DATA_BLOCK =
            SynchedEntityData.defineId(MountedBlockEntity.class, EntityDataSerializers.BLOCK_STATE);

    private static final Direction[] PLACE_DIRS = {
            Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN
    };

    @Nullable
    private CompoundTag blockEntityTag;
    private boolean suspended;

    public MountedBlockEntity(EntityType<? extends MountedBlockEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public MountedBlockEntity(Level level) {
        this(OCEntities.MOUNTED_BLOCK.get(), level);
    }

    public static @Nullable MountedBlockEntity create(Player player, ServerLevel level, BlockPos pos) {
        if (!MagnetPickup.canPickBlock(level, pos)) {
            return null;
        }
        BlockState state = level.getBlockState(pos);
        BlockEntity be = level.getBlockEntity(pos);
        CompoundTag teTag = null;
        if (be != null) {
            teTag = be.saveWithFullMetadata(level.registryAccess());
        }

        if (!OCFakePlayers.silentRemoveBlock(level, pos, player)) {
            return null;
        }

        MountedBlockEntity entity = new MountedBlockEntity(level);
        entity.setCarried(state, teTag);
        entity.setPos(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        return entity;
    }

    /** Suspended blocks skip auto-place until released (e.g. golden egg float phase). */
    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
        setNoGravity(suspended);
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void releaseFromSuspension() {
        suspended = false;
        setNoGravity(false);
        setDeltaMovement(0.0D, -0.9D, 0.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BLOCK, Blocks.STONE.defaultBlockState());
    }

    public void setCarried(BlockState state, @Nullable CompoundTag beTag) {
        entityData.set(DATA_BLOCK, state);
        this.blockEntityTag = beTag;
    }

    public BlockState getCarried() {
        return entityData.get(DATA_BLOCK);
    }

    public boolean canRelease() {
        return level().getBlockState(blockPosition()).canBeReplaced();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() || suspended) {
            return;
        }
        if (isPassenger()) {
            return;
        }
        if (tryPlace()) {
            discard();
        } else {
            dropAsItems();
            discard();
        }
    }

    private boolean tryPlace() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        BlockPos pos = blockPosition();
        if (placeAt(serverLevel, pos)) {
            return true;
        }
        for (Direction dir : PLACE_DIRS) {
            if (placeAt(serverLevel, pos.relative(dir))) {
                return true;
            }
        }
        return false;
    }

    private boolean placeAt(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).canBeReplaced()) {
            return false;
        }
        BlockState state = getCarried();
        if (!level.setBlock(pos, state, Block.UPDATE_ALL)) {
            return false;
        }
        if (blockEntityTag != null) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null) {
                CompoundTag tag = blockEntityTag.copy();
                tag.putInt("x", pos.getX());
                tag.putInt("y", pos.getY());
                tag.putInt("z", pos.getZ());
                be.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
                be.setChanged();
            }
        }
        level.updateNeighborsAt(pos, state.getBlock());
        return true;
    }

    private void dropAsItems() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockState state = getCarried();
        ItemStack stack = new ItemStack(state.getBlock());
        if (!stack.isEmpty()) {
            spawnAtLocation(serverLevel, stack);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        tag.read("BlockState", CompoundTag.CODEC).ifPresent(stateTag -> entityData.set(
                DATA_BLOCK, NbtUtils.readBlockState(BuiltInRegistries.BLOCK, stateTag)));
        tag.read("BlockEntity", CompoundTag.CODEC).ifPresent(beTag -> blockEntityTag = beTag);
        suspended = tag.getBooleanOr("Suspended", false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        tag.store("BlockState", CompoundTag.CODEC, NbtUtils.writeBlockState(getCarried()));
        if (blockEntityTag != null) {
            tag.store("BlockEntity", CompoundTag.CODEC, blockEntityTag.copy());
        }
        tag.putBoolean("Suspended", suspended);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (!isInvulnerableToBase(source)) {
            markHurt();
        }
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void move(MoverType type, net.minecraft.world.phys.Vec3 movement) {
        if (!isPassenger()) {
            super.move(type, movement);
        }
    }
}
