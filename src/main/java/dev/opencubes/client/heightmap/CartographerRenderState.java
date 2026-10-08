package dev.opencubes.client.heightmap;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class CartographerRenderState extends EntityRenderState {
    public float yaw;
    public final ItemStackRenderState item = new ItemStackRenderState();
}
