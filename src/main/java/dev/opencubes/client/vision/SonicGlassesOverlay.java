package dev.opencubes.client.vision;

import dev.opencubes.OCConstants;
import dev.opencubes.config.OCClientConfig;
import dev.opencubes.content.vision.SonicGlassesItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

/**
 * Sonic Glasses HUD: darkens the world view (before GUI) and shows short-lived category icons
 * for nearby sounds. The obscure blit runs in {@link RenderGuiEvent.Pre} so hotbar / inventory
 * layers stay opaque on top.
 */
@EventBusSubscriber(modid = OCConstants.MOD_ID, value = Dist.CLIENT)
public final class SonicGlassesOverlay {

    private static final Identifier OBSCURE = OCConstants.id("textures/misc/glasses_obsidian.png");
    private static final List<SoundBlip> BLIPS = new ArrayList<>();
    private static final int MAX_BLIPS = 24;
    private static final int BLIP_LIFE = 40;
    private static final int ICON_SIZE = 10;

    private SonicGlassesOverlay() {}

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !SonicGlassesItem.isWearing(mc.player) || event.getSound() == null) {
            return;
        }
        Vec3 pos = new Vec3(event.getSound().getX(), event.getSound().getY(), event.getSound().getZ());
        double range = OCClientConfig.GLASSES_SOUND_RANGE.get();
        if (mc.player.distanceToSqr(pos) > range * range) {
            return;
        }
        Identifier id = event.getSound().getIdentifier();
        String label = id.getPath();
        int slash = label.lastIndexOf('/');
        if (slash >= 0) {
            label = label.substring(slash + 1);
        }
        int dot = label.lastIndexOf('.');
        if (dot >= 0) {
            label = label.substring(dot + 1);
        }
        BLIPS.add(new SoundBlip(pos, label, SoundIconRegistry.resolve(id), BLIP_LIFE));
        while (BLIPS.size() > MAX_BLIPS) {
            BLIPS.remove(0);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Iterator<SoundBlip> it = BLIPS.iterator();
        while (it.hasNext()) {
            SoundBlip blip = it.next();
            blip.life--;
            if (blip.life <= 0) {
                it.remove();
            }
        }
    }

    /** Darken the world under the HUD so hotbar / hearts stay fully opaque. */
    @SubscribeEvent
    public static void onHudPre(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !SonicGlassesItem.isWearing(mc.player)) {
            return;
        }

        float opacity = OCClientConfig.GLASSES_OPACITY.get().floatValue();
        if (opacity <= 0.0F) {
            return;
        }

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        graphics.blit(RenderPipelines.GUI_TEXTURED, OBSCURE, 0, 0, 0.0F, 0.0F, width, height, width, height,
                ARGB.white(opacity));
    }

    /** Sound blips drawn after vanilla HUD so they remain readable over the hotbar. */
    @SubscribeEvent
    public static void onHudPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !SonicGlassesItem.isWearing(mc.player)) {
            return;
        }
        if (mc.level == null) {
            return;
        }

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        Vec3 camera = mc.gameRenderer.getMainCamera().position();
        float yaw = mc.gameRenderer.getMainCamera().yRot();
        float pitch = mc.gameRenderer.getMainCamera().xRot();

        for (SoundBlip blip : BLIPS) {
            Vec3 delta = blip.pos.subtract(camera);
            double dist = delta.length();
            if (dist < 0.01D) {
                continue;
            }
            double dx = delta.x;
            double dy = delta.y;
            double dz = delta.z;
            float yawRad = (float) Math.toRadians(yaw);
            float pitchRad = (float) Math.toRadians(pitch);
            double x = dx * Math.cos(yawRad) + dz * Math.sin(yawRad);
            double z = -dx * Math.sin(yawRad) + dz * Math.cos(yawRad);
            double y = dy * Math.cos(pitchRad) - z * Math.sin(pitchRad);
            z = dy * Math.sin(pitchRad) + z * Math.cos(pitchRad);
            if (z <= 0.1D) {
                continue;
            }
            double scale = (height / 2.0D) / z;
            int sx = Mth.clamp((int) (width / 2.0D + x * scale), 8, width - 8);
            int sy = Mth.clamp((int) (height / 2.0D - y * scale), 8, height - 16);
            float alpha = Mth.clamp(blip.life / (float) BLIP_LIFE, 0.15F, 1.0F);
            graphics.blit(RenderPipelines.GUI_TEXTURED, blip.category.texture(), sx - ICON_SIZE / 2, sy - ICON_SIZE / 2,
                    0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ARGB.white(alpha));
            int textAlpha = Mth.clamp((int) (alpha * 255.0F), 40, 255);
            graphics.centeredText(mc.font, blip.label, sx, sy + ICON_SIZE / 2 + 1,
                    (textAlpha << 24) | 0x00FFAA);
        }
    }

    private static final class SoundBlip {
        private final Vec3 pos;
        private final String label;
        private final SoundIconRegistry.Category category;
        private int life;

        private SoundBlip(Vec3 pos, String label, SoundIconRegistry.Category category, int life) {
            this.pos = pos;
            this.label = label;
            this.category = category;
            this.life = life;
        }
    }
}
