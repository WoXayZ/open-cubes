package dev.opencubes.content.scaffolding;

import dev.opencubes.config.OCCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Scaffolding that vanishes on its own. It behaves exactly like the vanilla block - climb it from
 * the inside, walk across the top, run it out sideways from the held item and watch it fall once
 * nothing holds it up - but it never drops and random ticks eat it away.
 *
 * <p>Vanilla's stability search is hard-coded to {@link net.minecraft.world.level.block.Blocks#SCAFFOLDING},
 * so every method that measures distance is reimplemented here against this block instead.
 * {@code codec()} is deliberately inherited: {@link ScaffoldingBlock} narrows the return type, and
 * the codec is only ever used by the block list data report.
 */
public class TemporaryScaffoldingBlock extends ScaffoldingBlock {

    public TemporaryScaffoldingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        int distance = distanceTo(level, pos);
        return defaultBlockState()
                .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER)
                .setValue(DISTANCE, distance)
                .setValue(BOTTOM, isBottom(level, pos, distance));
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int distance = distanceTo(level, pos);
        BlockState updated = state.setValue(DISTANCE, distance).setValue(BOTTOM, isBottom(level, pos, distance));
        if (distance == STABILITY_MAX_DISTANCE) {
            if (state.getValue(DISTANCE) == STABILITY_MAX_DISTANCE) {
                FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, updated);
                falling.dropItem = false;
            } else {
                level.destroyBlock(pos, false);
            }
        } else if (state != updated) {
            level.setBlock(pos, updated, Block.UPDATE_ALL);
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return distanceTo(level, pos) < STABILITY_MAX_DISTANCE;
    }

    @Override
    public boolean isScaffolding(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int rate = OCCommonConfig.SCAFFOLDING_DESPAWN_RATE.get();
        if (rate > 0 && random.nextInt(rate) != 0) {
            return;
        }
        int protect = OCCommonConfig.SCAFFOLDING_PLAYER_PROTECT_RADIUS.get();
        if (protect > 0) {
            double cx = pos.getX() + 0.5D;
            double cy = pos.getY() + 0.5D;
            double cz = pos.getZ() + 0.5D;
            double rangeSq = (double) protect * protect;
            for (Player player : level.players()) {
                if (!player.isSpectator() && player.distanceToSqr(cx, cy, cz) <= rangeSq) {
                    return;
                }
            }
        }
        // Leave the water behind when a waterlogged piece rots away.
        level.setBlock(pos, state.getFluidState().createLegacyBlock(), Block.UPDATE_ALL);
    }

    /** Vanilla {@link ScaffoldingBlock#getDistance}, but counting this block instead of the vanilla one. */
    public int distanceTo(BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = pos.mutable().move(Direction.DOWN);
        BlockState below = level.getBlockState(cursor);
        int distance = STABILITY_MAX_DISTANCE;
        if (below.is(this)) {
            distance = below.getValue(DISTANCE);
        } else if (below.isFaceSturdy(level, cursor, Direction.UP)) {
            return 0;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState neighbour = level.getBlockState(cursor.setWithOffset(pos, direction));
            if (neighbour.is(this)) {
                distance = Math.min(distance, neighbour.getValue(DISTANCE) + 1);
                if (distance == 1) {
                    break;
                }
            }
        }

        return distance;
    }

    private boolean isBottom(BlockGetter level, BlockPos pos, int distance) {
        return distance > 0 && !level.getBlockState(pos.below()).is(this);
    }
}
