package dev.opencubes.client.crane;

import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class MountedBlockRenderState extends EntityRenderState {
    public boolean draw;
    public final MovingBlockRenderState movingBlock = new MovingBlockRenderState();
}
