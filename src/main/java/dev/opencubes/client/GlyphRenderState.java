package dev.opencubes.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;

public class GlyphRenderState extends EntityRenderState {
    public char character;
    public Direction facing = Direction.NORTH;
}
