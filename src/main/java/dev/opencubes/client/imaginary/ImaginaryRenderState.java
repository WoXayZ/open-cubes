package dev.opencubes.client.imaginary;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.AABB;

public class ImaginaryRenderState extends BlockEntityRenderState {
    public boolean visible;
    public float red = 1.0F;
    public float green = 1.0F;
    public float blue = 1.0F;
    public float alpha = 0.45F;
    public final List<AABB> boxes = new ArrayList<>();
}
