package dev.opencubes.content.ladder;

import com.mojang.serialization.MapCodec;
import dev.opencubes.config.OCCommonConfig;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A ladder that unrolls downward on placement, consuming one item per segment.
 *
 * <p>Collision is one-sided: only entities facing the ladder collide with it, so you can
 * walk past from behind without catching on the rope.
 *
 * <p>Breaking a segment drops that segment and cascades downward so the rest of the chain
 * also drops (OpenBlocks behaviour). Unsupported segments never dissolve through
 * {@code updateShape → air} (that path double-drops with the cascade); they schedule a tick
 * instead.
 */
public class RopeLadderBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {

    public static final MapCodec<RopeLadderBlock> CODEC = simpleCodec(RopeLadderBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape EAST = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);
    private static final VoxelShape WEST = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SOUTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
    private static final VoxelShape NORTH = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);

    /** True while a cascade is tearing down the chain so shape updates do not also drop. */
    private static final ThreadLocal<Boolean> CASCADING = ThreadLocal.withInitial(() -> false);

    public RopeLadderBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends RopeLadderBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> EAST;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (entity instanceof LivingEntity living
                    && living.getDirection() != state.getValue(FACING).getOpposite()) {
                return Shapes.empty();
            }
        }
        return getShape(state, level, pos, context);
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        LevelReader level = context.getLevel();
        FluidState fluid = level.getFluidState(pos);
        Direction clicked = context.getClickedFace();

        Direction facing = null;

        // Prefer the face the player clicked when it can actually hold the ladder.
        if (clicked.getAxis().isHorizontal() && canSupport(level, pos, clicked)) {
            facing = clicked;
        } else if (clicked == Direction.DOWN) {
            BlockState above = level.getBlockState(pos.above());
            if (above.is(this)) {
                facing = above.getValue(FACING);
            }
        }

        // Vanilla ladder style: try looking directions so edge/corner aim still resolves.
        if (facing == null) {
            for (Direction look : context.getNearestLookingDirections()) {
                if (!look.getAxis().isHorizontal()) {
                    continue;
                }
                Direction candidate = look.getOpposite();
                if (canSupport(level, pos, candidate)) {
                    facing = candidate;
                    break;
                }
            }
        }

        if (facing == null) {
            facing = findSupport(level, pos);
        }
        if (facing == null) {
            return null;
        }

        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Nullable
    private Direction findSupport(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (canSupport(level, pos, direction)) {
                return direction;
            }
        }
        return null;
    }

    private boolean canSupport(LevelReader level, BlockPos pos, Direction facing) {
        BlockPos behind = pos.relative(facing.getOpposite());
        BlockState support = level.getBlockState(behind);
        if (support.isFaceSturdy(level, behind, facing)) {
            return true;
        }
        // OpenBlocks parity: any non-replaceable neighbour counts (partial blocks / edges).
        if (!support.isAir() && !support.canBeReplaced()) {
            return true;
        }
        BlockState above = level.getBlockState(pos.above());
        return above.is(this) && above.getValue(FACING) == facing;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupport(level, pos, state.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        // Never return air here: Block.updateOrDestroy would drop the segment, and the
        // onRemove cascade would drop it again. Schedule a tick for unsupported segments
        // that were not removed by a cascade (for example the wall behind was broken).
        if (!CASCADING.get() && !state.canSurvive(level, pos) && level instanceof Level realLevel
                && !realLevel.isClientSide) {
            realLevel.scheduleTick(pos, this, 1);
        }
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, !OCCommonConfig.ROPE_LADDER_INFINITE.get());
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        if (level.isClientSide || !(placer instanceof Player player)) {
            return;
        }

        boolean infinite = OCCommonConfig.ROPE_LADDER_INFINITE.get() || player.getAbilities().instabuild;
        Direction facing = state.getValue(FACING);
        BlockPos placePos = pos.below();

        // setPlacedBy runs before BlockItem shrinks the stack, so count still includes the top segment.
        while (placePos.getY() >= level.getMinBuildHeight()
                && (infinite || stack.getCount() > 1)) {
            if (!level.getBlockState(placePos).canBeReplaced()) {
                break;
            }
            if (!canSupport(level, placePos, facing)) {
                break;
            }

            BlockState placed = defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(WATERLOGGED, level.getFluidState(placePos).getType() == Fluids.WATER);
            level.setBlock(placePos, placed, Block.UPDATE_ALL);
            if (!infinite) {
                stack.shrink(1);
            }
            placePos = placePos.below();
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        super.onRemove(state, level, pos, newState, moved);
        if (level.isClientSide || state.is(newState.getBlock()) || moved || CASCADING.get()) {
            return;
        }
        boolean drop = !OCCommonConfig.ROPE_LADDER_INFINITE.get();
        CASCADING.set(true);
        try {
            BlockPos below = pos.below();
            while (below.getY() >= level.getMinBuildHeight() && level.getBlockState(below).is(this)) {
                BlockPos next = below.below();
                level.destroyBlock(below, drop);
                below = next;
            }
        } finally {
            CASCADING.set(false);
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (OCCommonConfig.ROPE_LADDER_INFINITE.get()) {
            return List.of();
        }
        return super.getDrops(state, params);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
