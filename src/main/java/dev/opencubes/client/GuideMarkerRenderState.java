package dev.opencubes.client;

import java.util.List;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;

public class GuideMarkerRenderState extends BlockEntityRenderState {
    public boolean draw;
    public float red;
    public float green;
    public float blue;
    public float morph;
    public List<BlockPos> current = List.of();
    public List<BlockPos> previous = List.of();
}
