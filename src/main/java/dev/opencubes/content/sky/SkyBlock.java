package dev.opencubes.content.sky;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Active faces are {@link RenderShape#INVISIBLE} and are drawn by {@link SkyBlockEntity}'s
 * renderer as a screen-space window into the real sky. Inactive faces keep the metal shell model.
 */
public class SkyBlock extends BaseEntityBlock {

    public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private final boolean defaultInverted;

    public SkyBlock(Properties properties, boolean defaultInverted) {
        super(properties);
        this.defaultInverted = defaultInverted;
        registerDefaultState(stateDefinition.any()
                .setValue(INVERTED, defaultInverted)
                .setValue(POWERED, false));
    }

    public static final MapCodec<SkyBlock> CODEC = simpleCodec(properties -> new SkyBlock(properties, false));

    @Override
    protected MapCodec<? extends SkyBlock> codec() {
        return CODEC;
    }

    public static boolean isActive(BlockState state) {
        return state.getValue(POWERED) ^ state.getValue(INVERTED);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INVERTED, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean powered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        return defaultBlockState()
                .setValue(INVERTED, defaultInverted)
                .setValue(POWERED, powered);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, net.minecraft.world.level.redstone.Orientation fromPos, boolean isMoving) {
        if (!level.isClientSide()) {
            boolean powered = level.hasNeighborSignal(pos);
            if (state.getValue(POWERED) != powered) {
                level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return isActive(state) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        return isActive(state) || super.skipRendering(state, adjacentState, side);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return isActive(state);
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return isActive(state) ? 1.0F : 0.2F;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SkyBlockEntity(pos, state);
    }
}
