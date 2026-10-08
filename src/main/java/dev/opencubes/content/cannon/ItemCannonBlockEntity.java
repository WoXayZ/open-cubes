package dev.opencubes.content.cannon;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.util.ItemHandlerBridge;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.IItemHandler;

public class ItemCannonBlockEntity extends BlockEntity implements Pointable {

    @Nullable
    private BlockPos target;
    private float yaw;
    private float pitch = 45.0F;
    private float speed = 0.55F;

    public ItemCannonBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.ITEM_CANNON.get(), pos, state);
        Direction facing = state.hasProperty(ItemCannonBlock.FACING)
                ? state.getValue(ItemCannonBlock.FACING)
                : Direction.NORTH;
        yaw = facing.toYRot();
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getSpeed() {
        return speed;
    }

    @Nullable
    public BlockPos getTarget() {
        return target;
    }

    @Override
    public void setTarget(Level level, BlockPos target, Player player) {
        this.target = target.immutable();
        recomputeTrajectory();
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    private void recomputeTrajectory() {
        if (target == null) {
            return;
        }
        float[] aim = new float[3];
        CannonTrajectory.aimAt(worldPosition, target, aim);
        yaw = aim[0];
        pitch = aim[1];
        speed = aim[2];
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ItemCannonBlockEntity cannon) {
        if (level.getGameTime() % OCCommonConfig.ITEM_CANNON_FIRE_INTERVAL.get() != 0
                || !level.hasNeighborSignal(pos)) {
            return;
        }
        ItemStack stack = extractFromNeighbors((ServerLevel) level, pos);
        if (stack.isEmpty()) {
            return;
        }
        cannon.fire(stack);
    }

    private static ItemStack extractFromNeighbors(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            var found = level.getCapability(Capabilities.Item.BLOCK, pos.relative(dir), dir.getOpposite());
            if (found == null) {
                continue;
            }
            IItemHandler handler = ItemHandlerBridge.asSlots(found);
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack extracted = handler.extractItem(slot, 64, false);
                if (!extracted.isEmpty()) {
                    return extracted;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private void fire(ItemStack stack) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        Vec3 muzzle = CannonTrajectory.muzzlePosition(worldPosition, yaw, pitch);
        Vec3 motion = CannonTrajectory.motionFromAngles(yaw, pitch, speed);

        ItemEntity projectile = new ItemEntity(server, muzzle.x, muzzle.y, muzzle.z, stack);
        projectile.setPickUpDelay(40);
        // Same initial motion the Pointer BER simulates (gravity + 0.98 drag in CannonTrajectory).
        projectile.setDeltaMovement(motion);
        projectile.setUnlimitedLifetime();
        server.addFreshEntity(projectile);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        if (target != null) {
            tag.putLong("Target", target.asLong());
        }
        tag.putFloat("Yaw", yaw);
        tag.putFloat("Pitch", pitch);
        tag.putFloat("Speed", speed);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        target = tag.keySet().contains("Target") ? BlockPos.of(tag.getLongOr("Target", 0L)) : null;
        yaw = tag.getFloatOr("Yaw", 0.0F);
        pitch = tag.getFloatOr("Pitch", 0.0F);
        speed = tag.keySet().contains("Speed") ? tag.getFloatOr("Speed", 0.0F) : 0.55F;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}
