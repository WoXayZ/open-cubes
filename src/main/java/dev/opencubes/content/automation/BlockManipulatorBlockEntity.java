package dev.opencubes.content.automation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Caps how often a breaker/placer can fire in one tick so a redstone storm cannot melt the
 * server. Overflow work is retried on the next tick while still powered.
 */
public abstract class BlockManipulatorBlockEntity extends BlockEntity {

    private int actionCount;

    protected BlockManipulatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract int getActionLimit();

    protected abstract boolean canWork(BlockState targetState, BlockPos target, Direction facing);

    protected abstract void doWork(BlockState targetState, BlockPos target, Direction facing);

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  BlockManipulatorBlockEntity manipulator) {
        boolean retry = manipulator.actionCount > 0;
        manipulator.actionCount = 0;
        if (retry && state.getValue(BlockManipulatorBlock.POWERED)) {
            manipulator.triggerAction(state);
        }
    }

    public void triggerAction() {
        if (level == null) {
            return;
        }
        triggerAction(getBlockState());
    }

    protected void triggerAction(BlockState state) {
        if (level == null || level.isClientSide) {
            return;
        }
        if (actionCount > getActionLimit()) {
            return;
        }

        Direction facing = state.getValue(BlockManipulatorBlock.FACING);
        BlockPos target = worldPosition.relative(facing);
        if (!level.isLoaded(target)) {
            return;
        }

        BlockState targetState = level.getBlockState(target);
        if (canWork(targetState, target, facing)) {
            doWork(targetState, target, facing);
            actionCount++;
        }
    }
}
