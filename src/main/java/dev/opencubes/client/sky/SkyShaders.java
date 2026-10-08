package dev.opencubes.client.sky;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.opencubes.OCConstants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

/**
 * Position-only pipeline. The fragment shader ignores vertex UVs and samples the captured sky
 * at the pixel being drawn.
 */
@EventBusSubscriber(modid = OCConstants.MOD_ID, value = Dist.CLIENT)
public final class SkyShaders {

    public static final RenderPipeline SKY_WINDOW = RenderPipeline.builder()
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withLocation(OCConstants.id("pipeline/sky_window"))
            .withVertexShader(OCConstants.id("core/sky_window"))
            .withFragmentShader(OCConstants.id("core/sky_window"))
            .withSampler("Sampler0")
            .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .withCull(false)
            .build();

    private SkyShaders() {}

    @SubscribeEvent
    public static void registerPipeline(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(SKY_WINDOW);
    }
}
