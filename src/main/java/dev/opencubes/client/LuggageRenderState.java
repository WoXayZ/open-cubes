package dev.opencubes.client;

import dev.opencubes.content.luggage.LuggageEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jspecify.annotations.Nullable;

public class LuggageRenderState extends LivingEntityRenderState {
    public boolean special;
    public float walkPosition;
    public float walkSpeed;
    public @Nullable LuggageEntity entity;
}
