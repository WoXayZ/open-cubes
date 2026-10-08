package dev.opencubes.client.crane;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;

public class MagnetRenderState extends EntityRenderState {
    public boolean hasCable;
    public Vec3 cableTip = Vec3.ZERO;
    public int cableLight;
    public boolean drawBoom;
    public Vec3 boomPivot = Vec3.ZERO;
    public float headYaw;
    public int boomLight;
}
