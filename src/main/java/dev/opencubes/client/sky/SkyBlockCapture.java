package dev.opencubes.client.sky;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import dev.opencubes.OCConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copies the main colour target once the sky pass has closed, so the sky window can sample
 * that picture at the fragment's screen position after the terrain has been drawn on top.
 */
@EventBusSubscriber(modid = OCConstants.MOD_ID, value = Dist.CLIENT)
public final class SkyBlockCapture {

    public static final Identifier TEXTURE = OCConstants.id("sky_capture");

    private static final Logger LOGGER = LoggerFactory.getLogger(SkyBlockCapture.class);
    private static final int TEXTURE_USAGE = GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING;
    private static final CaptureTexture CAPTURE = new CaptureTexture();

    private static boolean wanted;
    private static boolean ready;
    private static boolean registered;
    private static boolean loggedFailure;

    private SkyBlockCapture() {}

    public static void requestCapture() {
        wanted = true;
    }

    public static boolean isReady() {
        return ready;
    }

    @SubscribeEvent
    public static void afterSky(RenderLevelStageEvent.AfterSky event) {
        if (!wanted) {
            return;
        }
        wanted = false;
        GpuTexture source = Minecraft.getInstance().getMainRenderTarget().getColorTexture();
        if (source == null) {
            return;
        }
        try {
            if (!registered) {
                Minecraft.getInstance().getTextureManager().register(TEXTURE, CAPTURE);
                registered = true;
            }
            CAPTURE.copyFrom(source);
            ready = true;
        } catch (RuntimeException exception) {
            ready = false;
            if (!loggedFailure) {
                loggedFailure = true;
                LOGGER.warn("Sky window capture failed", exception);
            }
        }
    }

    private static final class CaptureTexture extends AbstractTexture {
        void copyFrom(GpuTexture source) {
            // Class init runs before SamplerCache.initialize(), which leaves this null and the
            // sky draw then fails with "Missing sampler Sampler0".
            this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
            int width = source.getWidth(0);
            int height = source.getHeight(0);
            if (this.texture == null || this.texture.getWidth(0) != width || this.texture.getHeight(0) != height) {
                GpuTexture texture = RenderSystem.getDevice().createTexture(
                        () -> "opencubes_sky_capture", TEXTURE_USAGE, TextureFormat.RGBA8, width, height, 1, 1);
                replace(texture, RenderSystem.getDevice().createTextureView(texture));
            }
            RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(
                    source, this.texture, 0, 0, 0, 0, 0, width, height);
        }

        void replace(GpuTexture texture, GpuTextureView view) {
            if (this.texture != null) {
                this.texture.close();
            }
            if (this.textureView != null) {
                this.textureView.close();
            }
            this.texture = texture;
            this.textureView = view;
        }
    }
}
