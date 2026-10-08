package dev.opencubes.content.sponge;

import dev.opencubes.config.OCCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/** Shared fluid-agnostic soak used by the Sponge block and Sponge on a Stick. */
public final class FluidSoak {

    public record Result(boolean absorbedAnything, boolean hitLava) {}

    private FluidSoak() {}

    public static Result soak(Level level, BlockPos origin, int range, boolean fullNeighborUpdates) {
        if (level.isClientSide()) {
            return new Result(false, false);
        }
        boolean absorbed = false;
        boolean hitLava = false;
        int flags = fullNeighborUpdates ? 3 : 2;
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    BlockPos work = origin.offset(dx, dy, dz);
                    if (!level.isLoaded(work)) {
                        continue;
                    }
                    FluidState fluid = level.getFluidState(work);
                    if (fluid.isEmpty()) {
                        continue;
                    }
                    absorbed = true;
                    hitLava |= fluid.is(FluidTags.LAVA);
                    level.setBlock(work, Blocks.AIR.defaultBlockState(), flags);
                }
            }
        }
        return new Result(absorbed, hitLava);
    }

    public static void wakeBorderLiquids(Level level, BlockPos origin, int range) {
        int extended = range + 1;
        for (int dx = -extended; dx <= extended; dx++) {
            for (int dy = -extended; dy <= extended; dy++) {
                for (int dz = -extended; dz <= extended; dz++) {
                    BlockPos work = origin.offset(dx, dy, dz);
                    if (!level.isLoaded(work)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(work);
                    if (!state.getFluidState().isEmpty()) {
                        state.handleNeighborChanged(level, work, state.getBlock(), null, false);
                    }
                }
            }
        }
    }

    public static int blockRange() {
        return OCCommonConfig.SPONGE_RANGE.get();
    }

    public static int stickRange() {
        return OCCommonConfig.SPONGE_STICK_RANGE.get();
    }

    public static boolean blockUpdates() {
        return OCCommonConfig.SPONGE_BLOCK_UPDATE.get();
    }

    public static boolean stickUpdates() {
        return OCCommonConfig.SPONGE_STICK_UPDATE.get();
    }
}
