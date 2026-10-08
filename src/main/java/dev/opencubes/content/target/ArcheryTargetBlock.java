package dev.opencubes.content.target;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import dev.opencubes.registry.OCBlockEntities;

/**
 * Flat against the face it was placed on until powered, then stands upright for archery.
 * Hits emit a redstone signal based on distance from the bullseye.
 */
public class ArcheryTargetBlock extends BaseEntityBlock {

    public static final MapCodec<ArcheryTargetBlock> CODEC = simpleCodec(ArcheryTargetBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final VoxelShape FLOOR_FLAT = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
    private static final VoxelShape CEILING_FLAT = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    // Upright model board sits on the side opposite FACING (front faces FACING).
    private static final VoxelShape UP_NORTH = Block.box(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape UP_SOUTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D);
    private static final VoxelShape UP_WEST = Block.box(12.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape UP_EAST = Block.box(0.0D, 0.0D, 0.0D, 4.0D, 16.0D, 16.0D);
    public ArcheryTargetBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE, POWERED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean powered = level.hasNeighborSignal(pos);

        for (Direction direction : context.getNearestLookingDirections()) {
            BlockState candidate;
            if (direction.getAxis() == Direction.Axis.Y) {
                // Face the player on floor/ceiling, like the previous ground-only placement.
                Direction facing = context.getHorizontalDirection().getOpposite();
                candidate = defaultBlockState()
                        .setValue(FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR)
                        .setValue(FACING, facing)
                        .setValue(POWERED, powered);
            } else {
                candidate = defaultBlockState()
                        .setValue(FACE, AttachFace.WALL)
                        .setValue(FACING, direction.getOpposite())
                        .setValue(POWERED, powered);
            }
            if (candidate.canSurvive(level, pos)) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canAttachTo(level, pos, getSupportDirection(state));
    }

    private static boolean canAttachTo(LevelReader level, BlockPos pos, Direction supportDir) {
        BlockPos support = pos.relative(supportDir);
        return level.getBlockState(support).isFaceSturdy(level, support, supportDir.getOpposite());
    }

    /** Direction from this block toward the block it is attached to. */
    public static Direction getSupportDirection(BlockState state) {
        return switch (state.getValue(FACE)) {
            case CEILING -> Direction.UP;
            case FLOOR -> Direction.DOWN;
            case WALL -> state.getValue(FACING).getOpposite();
        };
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (getSupportDirection(state) == direction && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return shapeFor(state);
    }

    private static VoxelShape shapeFor(BlockState state) {
        AttachFace face = state.getValue(FACE);
        boolean powered = state.getValue(POWERED);
        Direction facing = state.getValue(FACING);

        if (face == AttachFace.FLOOR && !powered) {
            return FLOOR_FLAT;
        }
        if (face == AttachFace.CEILING && !powered) {
            return CEILING_FLAT;
        }
        // Powered (any face) and wall-folded: thin board flush with the support, front toward FACING.
        return uprightShape(facing);
    }

    private static VoxelShape uprightShape(Direction facing) {
        return switch (facing) {
            case SOUTH -> UP_SOUTH;
            case WEST -> UP_WEST;
            case EAST -> UP_EAST;
            default -> UP_NORTH;
        };
    }

    /** Shared entry used by {@link #onProjectileHit}, entity collision, and the impact event. */
    public static void handleArrowHit(Level level, BlockPos pos, BlockState state, Vec3 hit) {
        if (level.isClientSide() || !state.getValue(POWERED)) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof ArcheryTargetBlockEntity target) {
            target.onHit(hit);
        }
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (projectile instanceof AbstractArrow) {
            handleArrowHit(level, hit.getBlockPos(), state, hit.getLocation());
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                   net.minecraft.world.level.redstone.Orientation fromPos, boolean isMoving) {
        if (!level.isClientSide()) {
            boolean powered = level.hasNeighborSignal(pos);
            if (state.getValue(POWERED) != powered) {
                level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
            }
            if (!state.canSurvive(level, pos)) {
                level.destroyBlock(pos, true);
            }
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof ArcheryTargetBlockEntity target) {
            return target.signalStrength();
        }
        return 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof ArcheryTargetBlockEntity target) {
            return target.signalStrength();
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcheryTargetBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide() ? null
                : createTickerHelper(type, OCBlockEntities.ARCHERY_TARGET.get(), ArcheryTargetBlockEntity::serverTick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public static int accuracyStrength(BlockPos pos, BlockState state, Vec3 hit) {
        Direction facing = state.getValue(FACING);
        double cx = pos.getX() + 0.5D;
        double cy = pos.getY() + 0.5D;
        double cz = pos.getZ() + 0.5D;
        double dx = hit.x - cx;
        double dy = hit.y - cy;
        double dz = hit.z - cz;
        double dist = switch (facing.getAxis()) {
            case X -> Math.sqrt(dy * dy + dz * dz);
            case Z -> Math.sqrt(dx * dx + dy * dy);
            default -> Math.sqrt(dx * dx + dz * dz);
        };
        return Mth.clamp(15 - (int) (dist * 32.0D), 0, 15);
    }
}
