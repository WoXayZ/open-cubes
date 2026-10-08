package dev.opencubes.client.egg;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class GoldenEggRenderState extends BlockEntityRenderState {
    public BlockState blockState = Blocks.AIR.defaultBlockState();
    public float rotation;
    public float progress;
    public float offset;
    public boolean specialEffects;
    public boolean idle;
}
