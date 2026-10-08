package dev.opencubes.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class TankRenderState extends BlockEntityRenderState {
    public boolean empty = true;
    public float red;
    public float green;
    public float blue;
    public float alpha;
    public float u0;
    public float v0;
    public float u1;
    public float v1;
    public float x0;
    public float x1;
    public float y0;
    public float y1;
    public float z0;
    public float z1;
    public boolean surfaceHidden;
    public boolean drawBottom;
    public final Side[] sides = new Side[4];

    public TankRenderState() {
        for (int i = 0; i < this.sides.length; i++) {
            this.sides[i] = new Side();
        }
    }

    public static final class Side {
        public Direction direction = Direction.NORTH;
        public boolean draw;
        public float base;
        public float plane;
    }
}
