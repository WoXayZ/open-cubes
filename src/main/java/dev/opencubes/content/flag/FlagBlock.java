package dev.opencubes.content.flag;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.opencubes.registry.OCBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A decorative pennant on a pole: planted on the ground or on top of a fence post, or
 * bracketed onto the side of a block. Standing flags may stack two high into one taller sail.
 */
public class FlagBlock extends Block implements SimpleWaterloggedBlock {

    public static final MapCodec<FlagBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DyeColor.CODEC.fieldOf("colour").forGetter(FlagBlock::colour),
            propertiesCodec()
    ).apply(instance, FlagBlock::new));

    /** True when the flag is bracketed to the side of a block instead of standing on the ground. */
    public static final BooleanProperty WALL = BooleanProperty.create("wall");
    /** Sixteenths of a turn, as on banners. On a wall flag this encodes the facing instead. */
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    /**
     * Lower is a single flag or the base of a tall pair; upper is the second pole segment of a
     * merged two-high sail. Wall flags always stay {@link DoubleBlockHalf#LOWER}.
     */
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    /** True on both halves of a merged two-high standing flag. */
    public static final BooleanProperty TALL = BooleanProperty.create("tall");

    private static final VoxelShape GROUND = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D);
    private static final VoxelShape WALL_NORTH = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape WALL_SOUTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
    private static final VoxelShape WALL_WEST = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape WALL_EAST = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);

    private final DyeColor colour;

    public FlagBlock(DyeColor colour, Properties properties) {
        super(properties);
        this.colour = colour;
        registerDefaultState(defaultBlockState()
                .setValue(WALL, false)
                .setValue(ROTATION, 0)
                .setValue(WATERLOGGED, false)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(TALL, false));
    }

    public DyeColor colour() {
        return colour;
    }

    @Override
    protected MapCodec<? extends FlagBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WALL, ROTATION, WATERLOGGED, HALF, TALL);
    }

    /** The direction a wall flag points away from its support. Meaningless while standing. */
    public static Direction wallFacing(BlockState state) {
        return Direction.fromYRot(RotationSegment.convertToDegrees(state.getValue(ROTATION)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(WALL)) {
            return GROUND;
        }
        return switch (wallFacing(state)) {
            case NORTH -> WALL_NORTH;
            case WEST -> WALL_WEST;
            case EAST -> WALL_EAST;
            default -> WALL_SOUTH;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean waterlogged = level.getFluidState(pos).getType() == Fluids.WATER;
        Direction clicked = context.getClickedFace();

        if (clicked.getAxis().isHorizontal() && canAttachTo(level, pos, clicked)) {
            return defaultBlockState()
                    .setValue(WALL, true)
                    .setValue(ROTATION, RotationSegment.convertToSegment(clicked))
                    .setValue(WATERLOGGED, waterlogged)
                    .setValue(HALF, DoubleBlockHalf.LOWER)
                    .setValue(TALL, false);
        }

        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (belowState.getBlock() instanceof FlagBlock belowFlag
                && !belowState.getValue(WALL)
                && belowState.getValue(HALF) == DoubleBlockHalf.LOWER
                && !belowState.getValue(TALL)) {
            if (belowFlag.colour() != colour) {
                return null;
            }
            return defaultBlockState()
                    .setValue(WALL, false)
                    .setValue(ROTATION, belowState.getValue(ROTATION))
                    .setValue(WATERLOGGED, waterlogged)
                    .setValue(HALF, DoubleBlockHalf.UPPER)
                    .setValue(TALL, true);
        }

        if (belowState.getBlock() instanceof FlagBlock || !Block.canSupportCenter(level, below, Direction.UP)) {
            return null;
        }
        return defaultBlockState()
                .setValue(WALL, false)
                .setValue(ROTATION, RotationSegment.convertToSegment(context.getRotation() + 180.0F))
                .setValue(WATERLOGGED, waterlogged)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(TALL, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos below = pos.below();
            BlockState belowState = level.getBlockState(below);
            if (belowState.getBlock() instanceof FlagBlock && !belowState.getValue(WALL)) {
                int rotation = belowState.getValue(ROTATION);
                // Lower owns the sail facing; upper is only the pole extension.
                level.setBlock(below, belowState.setValue(TALL, true), Block.UPDATE_ALL);
                if (state.getValue(ROTATION) != rotation || !state.getValue(TALL)) {
                    level.setBlock(pos, state.setValue(ROTATION, rotation).setValue(TALL, true),
                            Block.UPDATE_ALL);
                }
            }
        }
    }

    private static boolean canAttachTo(LevelReader level, BlockPos pos, Direction facing) {
        BlockPos support = pos.relative(facing.getOpposite());
        return level.getBlockState(support).isFaceSturdy(level, support, facing);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(WALL)) {
            return canAttachTo(level, pos, wallFacing(state));
        }
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            // Do not require TALL on the lower yet: setPlacedBy flips that after the upper is placed.
            return below.getBlock() instanceof FlagBlock
                    && !below.getValue(WALL)
                    && below.getValue(HALF) == DoubleBlockHalf.LOWER
                    && ((FlagBlock) below.getBlock()).colour() == colour;
        }
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (belowState.getBlock() instanceof FlagBlock) {
            return false;
        }
        return Block.canSupportCenter(level, below, Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (!state.getValue(WALL)) {
            if (direction == Direction.DOWN && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
                if (!(neighbourState.getBlock() instanceof FlagBlock)
                        || !neighbourState.getValue(TALL)
                        || neighbourState.getValue(HALF) != DoubleBlockHalf.LOWER) {
                    return Blocks.AIR.defaultBlockState();
                }
                // Keep the upper pole locked to the lower sail's rotation.
                int belowRotation = neighbourState.getValue(ROTATION);
                if (state.getValue(ROTATION) != belowRotation) {
                    return state.setValue(ROTATION, belowRotation);
                }
            }
            if (direction == Direction.UP && state.getValue(HALF) == DoubleBlockHalf.LOWER
                    && state.getValue(TALL)) {
                if (!(neighbourState.getBlock() instanceof FlagBlock)
                        || neighbourState.getValue(HALF) != DoubleBlockHalf.UPPER) {
                    return state.setValue(TALL, false);
                }
            }
        }
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !state.getValue(WALL) && state.getValue(TALL)) {
            if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
                BlockPos below = pos.below();
                BlockState belowState = level.getBlockState(below);
                if (belowState.getBlock() instanceof FlagBlock) {
                    if (!player.getAbilities().instabuild) {
                        // Upper has no loot; drop both flags from the lower half here.
                        ItemStack drop = new ItemStack(belowState.getBlock());
                        popResource(level, below, drop.copy());
                        popResource(level, below, drop);
                    }
                    level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            } else {
                BlockPos above = pos.above();
                if (level.getBlockState(above).getBlock() instanceof FlagBlock) {
                    level.setBlock(above, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return List.of();
        }
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, params));
        if (state.getValue(TALL) && !drops.isEmpty()) {
            drops.add(drops.getFirst().copy());
        }
        return drops;
    }

    /** Standing flags can be turned by hand, a segment at a time, sneak to go the other way. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (state.getValue(WALL)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            int step = player.isShiftKeyDown() ? -1 : 1;
            setRotationPair(level, pos, state, (state.getValue(ROTATION) + step) & 15);
        }
        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
    }

    private static void setRotationPair(Level level, BlockPos pos, BlockState state, int rotation) {
        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        BlockState lowerState = level.getBlockState(lowerPos);
        if (!(lowerState.getBlock() instanceof FlagBlock)) {
            level.setBlock(pos, state.setValue(ROTATION, rotation), Block.UPDATE_CLIENTS);
            return;
        }
        level.setBlock(lowerPos, lowerState.setValue(ROTATION, rotation), Block.UPDATE_CLIENTS);
        if (lowerState.getValue(TALL)) {
            BlockState upper = level.getBlockState(lowerPos.above());
            if (upper.getBlock() instanceof FlagBlock) {
                level.setBlock(lowerPos.above(), upper.setValue(ROTATION, rotation), Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(stack.getItem() instanceof DyeItem dye)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        DyeColor target = stack.get(net.minecraft.core.component.DataComponents.DYE);
        if (target == colour) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        Block recoloured = OCBlocks.recolour(this, target);
        if (recoloured == null) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            recolourPair(level, pos, state, recoloured);
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
    }

    private void recolourPair(Level level, BlockPos pos, BlockState state, Block recoloured) {
        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        BlockState lowerState = level.getBlockState(lowerPos);
        boolean tall = lowerState.hasProperty(TALL) && lowerState.getValue(TALL);
        int rotation = state.getValue(ROTATION);

        level.setBlockAndUpdate(lowerPos, recoloured.defaultBlockState()
                .setValue(WALL, lowerState.getValue(WALL))
                .setValue(ROTATION, rotation)
                .setValue(WATERLOGGED, lowerState.getValue(WATERLOGGED))
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(TALL, tall));
        if (tall) {
            BlockPos upperPos = lowerPos.above();
            BlockState upperState = level.getBlockState(upperPos);
            level.setBlockAndUpdate(upperPos, recoloured.defaultBlockState()
                    .setValue(WALL, false)
                    .setValue(ROTATION, rotation)
                    .setValue(WATERLOGGED, upperState.hasProperty(WATERLOGGED)
                            ? upperState.getValue(WATERLOGGED) : false)
                    .setValue(HALF, DoubleBlockHalf.UPPER)
                    .setValue(TALL, true));
        }
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(ROTATION, rotation.rotate(state.getValue(ROTATION), 16));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(ROTATION, mirror.mirror(state.getValue(ROTATION), 16));
    }
}
