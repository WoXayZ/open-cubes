package dev.opencubes.content.paint;

import dev.opencubes.registry.OCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Converts a suitable world block into a canvas that remembers the original state. */
public final class CanvasReplace {

    private CanvasReplace() {}

    public static boolean canReplace(Level level, BlockPos pos, BlockState state) {
        if (state.isAir() || state.getBlock() instanceof CanvasBlock) {
            return false;
        }
        if (level.getBlockEntity(pos) != null) {
            return false;
        }
        if (state.getBlock() instanceof DoorBlock || state.is(BlockTags.BEDS) || state.is(Blocks.PISTON)
                || state.is(Blocks.STICKY_PISTON) || state.is(Blocks.MOVING_PISTON)) {
            return false;
        }
        // Tag hook for datapack blacklists (empty by default until filled).
        if (state.is(net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.BLOCK,
                dev.opencubes.OCConstants.id("canvas_replace_blacklist")))) {
            return false;
        }
        return state.isCollisionShapeFullBlock(level, pos) || !state.canOcclude();
    }

    public static boolean replace(Level level, BlockPos pos) {
        BlockState old = level.getBlockState(pos);
        if (!canReplace(level, pos, old)) {
            return false;
        }
        Block canvas = old.canOcclude() ? OCBlocks.CANVAS.get() : OCBlocks.GLASS_CANVAS.get();
        level.setBlock(pos, canvas.defaultBlockState(), Block.UPDATE_ALL);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CanvasBlockEntity canvasBe) {
            canvasBe.setPaintedBlock(old);
            return true;
        }
        return false;
    }
}
