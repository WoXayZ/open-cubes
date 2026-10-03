package dev.opencubes.client.sky;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.opencubes.OCConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * Copies the colour buffer right after the vanilla sky pass, before terrain, so active sky blocks
 * can show it. Vanilla draws clouds at the very end of the frame, so they are drawn a second time
 * into the copy, on an empty depth buffer so terrain does not hide them. Only runs while a sky
 * block asked for it during the previous frame.
 */
@EventBusSubscriber(modid = OCConstants.MOD_ID, value = Dist.CLIENT)
public final class SkyBlockCapture {

    private static TextureTarget skyTarget;
    private static boolean ready;
    private static boolean requested;

    private SkyBlockCapture() {}

    public static void requestCapture() {
        requested = true;
    }

    public static boolean isReady() {
        return ready && skyTarget != null;
    }

    public static int colorTextureId() {
        return skyTarget != null ? skyTarget.getColorTextureId() : 0;
    }

    @SubscribeEvent
    public static void afterSky(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }
        ready = false;
        if (!requested) {
            return;
        }
        requested = false;

        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget main = minecraft.getMainRenderTarget();
        ensureTarget(main.width, main.height);
        RenderSystem.assertOnRenderThread();
        skyTarget.clear(Minecraft.ON_OSX);
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, skyTarget.frameBufferId);
        GL30.glBlitFramebuffer(
                0, 0, main.width, main.height,
                0, 0, skyTarget.width, skyTarget.height,
                GL11.GL_COLOR_BUFFER_BIT,
                GL11.GL_NEAREST);
        renderClouds(minecraft, event);
        main.bindWrite(true);
        ready = true;
    }

    private static void renderClouds(Minecraft minecraft, RenderLevelStageEvent event) {
        if (minecraft.level == null || minecraft.options.getCloudsType() == CloudStatus.OFF) {
            return;
        }
        Camera camera = event.getCamera();
        Vec3 pos = camera.getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        boolean foggy = minecraft.level.effects().isFoggyAt(Mth.floor(pos.x), Mth.floor(pos.y))
                || minecraft.gui.getBossOverlay().shouldCreateWorldFog();
        // Same fog vanilla has active when it draws its own clouds; terrain fog comes right after this stage anyway.
        FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_TERRAIN,
                Math.max(minecraft.gameRenderer.getRenderDistance(), 32.0F), foggy, partialTick);

        // Fabulous sends clouds to their own target; vanilla clears it again before its own cloud pass.
        RenderTarget cloudsTarget = Minecraft.useShaderTransparency()
                ? minecraft.levelRenderer.getCloudsTarget()
                : null;
        if (cloudsTarget != null) {
            cloudsTarget.clear(Minecraft.ON_OSX);
        } else {
            skyTarget.bindWrite(true);
        }
        minecraft.levelRenderer.renderClouds(new PoseStack(), event.getModelViewMatrix(),
                event.getProjectionMatrix(), partialTick, pos.x, pos.y, pos.z);

        if (cloudsTarget != null) {
            skyTarget.bindWrite(true);
            RenderSystem.enableBlend();
            // Translucent blending onto a cleared target leaves premultiplied colour.
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            cloudsTarget.blitToScreen(skyTarget.width, skyTarget.height, false);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    private static void ensureTarget(int width, int height) {
        if (skyTarget == null || skyTarget.width != width || skyTarget.height != height) {
            if (skyTarget != null) {
                skyTarget.destroyBuffers();
            }
            skyTarget = new TextureTarget(width, height, true, Minecraft.ON_OSX);
            skyTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        }
    }
}
