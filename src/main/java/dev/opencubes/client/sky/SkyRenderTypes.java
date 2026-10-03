package dev.opencubes.client.sky;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/** Opaque faces that show the sky captured right after the vanilla sky pass, in screen space. */
public final class SkyRenderTypes extends RenderType {

    public static final RenderType SKY_WINDOW = create("opencubes_sky_window",
            DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 1536, false, false,
            CompositeState.builder()
                    .setShaderState(new ShaderStateShard(SkyShaders::skyWindow))
                    .setTextureState(new EmptyTextureStateShard(
                            () -> RenderSystem.setShaderTexture(0, SkyBlockCapture.colorTextureId()),
                            () -> {}))
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setCullState(CULL)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setLightmapState(NO_LIGHTMAP)
                    .createCompositeState(false));

    private SkyRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                           boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
    }
}
