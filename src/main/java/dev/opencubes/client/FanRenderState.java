package dev.opencubes.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FanRenderState extends BlockEntityRenderState {
    public BlockState blockState = Blocks.AIR.defaultBlockState();
    public float yaw;
    public float bladeRotation;
}
