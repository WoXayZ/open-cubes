package dev.opencubes.client.village;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.AABB;

public class VillageHighlighterRenderState extends BlockEntityRenderState {
    public boolean draw;
    public AABB box = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
}
