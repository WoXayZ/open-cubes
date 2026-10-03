package dev.opencubes.content.elevator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * An elevator that also turns the player to face a fixed direction on arrival.
 *
 * <p>Upstream needed a block entity for this, because 1.12 metadata was already spent on the
 * sixteen colours. A block state property costs nothing here, so the block entity is gone.
 */
public class RotatingElevatorBlock extends ElevatorBlock {

    public static final MapCodec<RotatingElevatorBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DyeColor.CODEC.fieldOf("colour").forGetter(block -> block.elevatorColour(block.defaultBlockState())),
            propertiesCodec()
    ).apply(instance, RotatingElevatorBlock::new));

    public RotatingElevatorBlock(DyeColor colour, Properties properties) {
        super(colour, properties);
        registerDefaultState(defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends ElevatorBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,
                context.getHorizontalDirection().getOpposite());
    }

    @Override
    public Direction arrivalFacing(BlockState state) {
        // Placement uses getOpposite() so FACING points at the player; the arrow on top points
        // the other way. Arrival should match the painted arrow.
        return state.getValue(HorizontalDirectionalBlock.FACING).getOpposite();
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(HorizontalDirectionalBlock.FACING,
                rotation.rotate(state.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING)));
    }
}
