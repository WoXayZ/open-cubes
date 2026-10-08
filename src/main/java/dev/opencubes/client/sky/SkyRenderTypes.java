package dev.opencubes.client.sky;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

/** Faces of an active sky block. Sampler0 is the sky copy from {@link SkyBlockCapture}. */
public final class SkyRenderTypes {

    public static final RenderType SKY_WINDOW = RenderType.create(
            "opencubes_sky_window",
            RenderSetup.builder(SkyShaders.SKY_WINDOW)
                    // The capture texture is constructed when the mod class loads, before the sampler
                    // cache exists, so its own sampler stays null. A null sampler makes the draw drop
                    // Sampler0 and the world crashes every frame. Resolve one at draw time instead.
                    .withTexture("Sampler0", SkyBlockCapture.TEXTURE,
                            () -> RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR))
                    .createRenderSetup());

    private SkyRenderTypes() {}
}
