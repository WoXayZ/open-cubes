package dev.opencubes.client.sprinkler;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;

public class SprinklerRenderState extends BlockEntityRenderState {
    public BlockState blockState = Blocks.AIR.defaultBlockState();
    public Direction facing = Direction.NORTH;
    public float tilt;
}
