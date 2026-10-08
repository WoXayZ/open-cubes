package dev.opencubes.content.imaginary;



import com.mojang.serialization.MapCodec;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;

import net.minecraft.core.Direction;

import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.context.BlockPlaceContext;

import net.minecraft.world.level.BlockGetter;

import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.BaseEntityBlock;

import net.minecraft.world.level.block.Block;

import net.minecraft.world.level.block.Mirror;

import net.minecraft.world.level.block.RenderShape;

import net.minecraft.world.level.block.Rotation;

import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.block.state.StateDefinition;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.EnumProperty;


import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.storage.loot.LootParams;

import net.minecraft.world.phys.shapes.CollisionContext;

import net.minecraft.world.phys.shapes.EntityCollisionContext;

import net.minecraft.world.phys.shapes.Shapes;

import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Collections;

import java.util.List;



public class ImaginaryBlock extends BaseEntityBlock {



    public static final MapCodec<ImaginaryBlock> CODEC = simpleCodec(ImaginaryBlock::new);



    public static final EnumProperty<ImaginaryShape> SHAPE = EnumProperty.create("shape", ImaginaryShape.class);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;



    public ImaginaryBlock(Properties properties) {

        super(properties);

        registerDefaultState(stateDefinition.any()

                .setValue(SHAPE, ImaginaryShape.BLOCK)

                .setValue(FACING, Direction.NORTH));

    }



    @Override

    protected MapCodec<? extends BaseEntityBlock> codec() {

        return CODEC;

    }



    @Override

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {

        builder.add(SHAPE, FACING);

    }



    @Nullable

    @Override

    public BlockState getStateForPlacement(BlockPlaceContext context) {

        return defaultBlockState()

                .setValue(FACING, context.getHorizontalDirection().getOpposite())

                .setValue(SHAPE, ImaginaryShape.BLOCK);

    }



    @Override

    protected BlockState rotate(BlockState state, Rotation rotation) {

        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));

    }



    @Override

    protected BlockState mirror(BlockState state, Mirror mirror) {

        return state.rotate(mirror.getRotation(state.getValue(FACING)));

    }



    @Nullable

    @Override

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {

        return new ImaginaryBlockEntity(pos, state);

    }



    @Override

    protected RenderShape getRenderShape(BlockState state) {

        return RenderShape.INVISIBLE;

    }



    @Override

    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        ImaginaryBlockEntity be = be(level, pos);

        if (be != null && context instanceof EntityCollisionContext entityContext

                && entityContext.getEntity() != null

                && be.is(ImaginaryProperty.SOLID, entityContext.getEntity())) {

            return be.voxelShape();

        }

        return Shapes.empty();

    }



    @Override

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        ImaginaryBlockEntity be = be(level, pos);

        if (be == null) {

            return Shapes.empty();

        }

        Player player = null;
        if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof Player found) {
            player = found;
        }
        if (player == null && level instanceof Level world && world.isClientSide()) {
            player = dev.opencubes.client.imaginary.ImaginaryClient.player();
        }
        if (player != null && be.is(ImaginaryProperty.VISIBLE, player)) {
            return be.voxelShape();
        }
        if (player != null) {
            return Shapes.empty();
        }

        // Creative pick / outline without entity context - still expose shape when inverted.

        return be.isInverted() ? be.voxelShape() : Shapes.empty();

    }



    @Override

    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {

        return adjacentState.is(this) || super.skipRendering(state, adjacentState, side);

    }



    @Override

    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {

        return Collections.emptyList();

    }



    @Override

    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {

        ImaginaryBlockEntity be = be(level, pos);

        return be != null ? be.createPickStack() : ItemStack.EMPTY;

    }



    @Override

    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,

                              @Nullable BlockEntity blockEntity, ItemStack tool) {

        if (!level.isClientSide() && blockEntity instanceof ImaginaryBlockEntity imaginary) {

            popResource(level, pos, imaginary.createPickStack());

        }

        super.playerDestroy(level, player, pos, state, blockEntity, tool);

    }



    @Override

    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, net.minecraft.world.level.redstone.Orientation fromPos, boolean isMoving) {

        // no-op

    }



    @Nullable

    private static ImaginaryBlockEntity be(BlockGetter level, BlockPos pos) {

        BlockEntity be = level.getBlockEntity(pos);

        return be instanceof ImaginaryBlockEntity imaginary ? imaginary : null;

    }

}


