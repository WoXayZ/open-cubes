package dev.opencubes.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jspecify.annotations.Nullable;

public class TrophyRenderState extends BlockEntityRenderState {
    public boolean draw;
    public float yaw;
    public float scale = 1.0F;
    public float verticalOffset;
    public @Nullable EntityRenderState entity;
}
