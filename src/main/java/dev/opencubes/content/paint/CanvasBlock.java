package dev.opencubes.content.paint;

import com.mojang.serialization.MapCodec;
import dev.opencubes.registry.OCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CanvasBlock extends BaseEntityBlock {

    public static final MapCodec<CanvasBlock> CODEC = simpleCodec(CanvasBlock::new);

    public CanvasBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends CanvasBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CanvasBlockEntity(pos, state);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return false;
    }
}
