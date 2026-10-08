package dev.opencubes.client.sky;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class SkyBlockRenderState extends BlockEntityRenderState {
    public boolean draw;
    public final boolean[] faces = new boolean[Direction.values().length];
}
