package dev.opencubes.content.paint;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class GlassCanvasBlock extends CanvasBlock {

    public static final MapCodec<GlassCanvasBlock> CODEC = simpleCodec(GlassCanvasBlock::new);

    public GlassCanvasBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends CanvasBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction side) {
        return adjacent.getBlock() instanceof GlassCanvasBlock || super.skipRendering(state, adjacent, side);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }
}
