package dev.opencubes.content.cannon;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import dev.opencubes.registry.OCBlockEntities;

public class ItemCannonBlock extends BaseEntityBlock {

    public static final MapCodec<ItemCannonBlock> CODEC = simpleCodec(ItemCannonBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    /** Traced from item_cannon.json (base, mounts, tilted barrel) with the muzzle pointing north. */
    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D),
            Block.box(1.0D, 1.0D, 5.0D, 3.0D, 9.0D, 11.0D),
            Block.box(13.0D, 1.0D, 5.0D, 15.0D, 9.0D, 11.0D),
            Block.box(5.0D, 2.0D, 0.0D, 11.0D, 12.0D, 14.0D));
    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.NORTH, SHAPE_NORTH,
            Direction.EAST, rotateY(SHAPE_NORTH, 1),
            Direction.SOUTH, rotateY(SHAPE_NORTH, 2),
            Direction.WEST, rotateY(SHAPE_NORTH, 3));

    public ItemCannonBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.getOrDefault(state.getValue(FACING), SHAPE_NORTH);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPES.getOrDefault(state.getValue(FACING), SHAPE_NORTH);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemCannonBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, OCBlockEntities.ITEM_CANNON.get(), ItemCannonBlockEntity::serverTick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** Rotate a shape {@code steps} quarter-turns clockwise around Y (north → east → south → west). */
    private static VoxelShape rotateY(VoxelShape shape, int steps) {
        VoxelShape[] result = {shape};
        for (int i = 0; i < steps; i++) {
            VoxelShape[] next = {Shapes.empty()};
            result[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                // (x, z) → (1 - z, x) in block space (0..1).
                next[0] = Shapes.or(next[0], Shapes.box(1.0D - maxZ, minY, minX, 1.0D - minZ, maxY, maxX));
            });
            result[0] = next[0];
        }
        return result[0];
    }
}
