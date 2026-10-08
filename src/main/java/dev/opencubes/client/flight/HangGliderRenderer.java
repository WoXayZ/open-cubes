package dev.opencubes.client.flight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.OCConstants;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

/**
 * Delta-wing glider drawn in player model space of a prone pilot (pixels, x = pilot's left,
 * -y = flight direction, +z = up). The control bar is placed where {@link GliderArmPose} puts the
 * hands.
 */
public final class HangGliderRenderer {

    public static final Identifier TEXTURE = OCConstants.id("textures/entity/hang_glider.png");
    private static final float TEX_W = 128.0F;
    private static final float TEX_H = 64.0F;

    /** Sail outline (half span); must match the sail art in the texture, mapped 1:1. */
    private static final float NOSE_Y = -30.0F;
    private static final float TIP_X = 34.0F;
    private static final float TIP_Y = 12.0F;
    private static final float NOTCH_X = 16.0F;
    private static final float NOTCH_Y = 15.0F;
    private static final float TAIL_Y = 22.0F;
    private static final float SAIL_U = 34.0F;
    private static final float SAIL_V = 30.0F;

    private static final float KEEL_Z = 13.0F;
    private static final float TIP_DROP = 1.2F;
    private static final float SAIL_THICKNESS = 0.35F;
    private static final float BAR_HALF = 12.5F;
    private static final float APEX_Y = 1.5F;
    private static final float HANG_Y = 7.0F;
    private static final float BACK_Z = 2.0F;

    private static final int CELL_WOOD = 0;
    private static final int CELL_WOOD_DARK = 1;
    private static final int CELL_HEM = 2;
    private static final int CELL_STRAP = 4;
    private static final int WHITE = 0xFFFFFF;

    private HangGliderRenderer() {}

    /**
     * {@code poseStack} must be at the player model origin, in block units. {@code sailColour}
     * tints the greyscale canvas and hem; the frame and strap keep their texture colours.
     */
    public static void render(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int sailColour) {
        int underside = scale(sailColour, 0x9A);
        poseStack.pushPose();
        poseStack.scale(1.0F / 16.0F, 1.0F / 16.0F, 1.0F / 16.0F);
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TEXTURE), (pose, buffer) -> {
        Mesh mesh = new Mesh(buffer, pose, light);

        Vector3f nose = new Vector3f(0, NOSE_Y, KEEL_Z);
        Vector3f tail = new Vector3f(0, TAIL_Y, KEEL_Z);
        float barY = GliderArmPose.GRIP.y;
        float barZ = GliderArmPose.GRIP.z;
        Vector3f apex = new Vector3f(0, APEX_Y, KEEL_Z - 0.5F);

        for (int side = -1; side <= 1; side += 2) {
            Vector3f tip = new Vector3f(side * TIP_X, TIP_Y, KEEL_Z - TIP_DROP);
            Vector3f notch = new Vector3f(side * NOTCH_X, NOTCH_Y, KEEL_Z - TIP_DROP * 0.4F);
            mesh.sail(nose, tip, notch, 0.0F, sailColour);
            mesh.sail(nose, notch, tail, 0.0F, sailColour);
            mesh.sail(nose, tip, notch, -SAIL_THICKNESS, underside);
            mesh.sail(nose, notch, tail, -SAIL_THICKNESS, underside);

            mesh.beam(new Vector3f(0, NOSE_Y + 0.5F, KEEL_Z - 0.4F),
                    new Vector3f(side * TIP_X, TIP_Y, KEEL_Z - TIP_DROP - 0.4F), 1.0F, CELL_HEM, sailColour);
            Vector3f crossEnd = lerp(nose, tip, 0.55F).sub(0, 0, 0.9F);
            mesh.beam(new Vector3f(0, crossEnd.y, KEEL_Z - 0.9F), crossEnd, 0.7F, CELL_WOOD_DARK, WHITE);
            mesh.beam(new Vector3f(side * BAR_HALF, barY, barZ), apex, 0.8F, CELL_WOOD, WHITE);
        }

        mesh.beam(new Vector3f(0, NOSE_Y + 1.0F, KEEL_Z - 0.6F), new Vector3f(0, TAIL_Y, KEEL_Z - 0.6F),
                1.0F, CELL_WOOD_DARK, WHITE);
        mesh.beam(new Vector3f(-BAR_HALF - 0.4F, barY, barZ), new Vector3f(BAR_HALF + 0.4F, barY, barZ),
                0.9F, CELL_WOOD_DARK, WHITE);
        mesh.beam(new Vector3f(0, HANG_Y, KEEL_Z - 1.0F), new Vector3f(0, HANG_Y, BACK_Z), 0.6F, CELL_STRAP, WHITE);
        });

        poseStack.popPose();
    }

    private static int scale(int colour, int factor) {
        int r = ((colour >> 16) & 0xFF) * factor / 255;
        int g = ((colour >> 8) & 0xFF) * factor / 255;
        int b = (colour & 0xFF) * factor / 255;
        return (r << 16) | (g << 8) | b;
    }

    private static Vector3f lerp(Vector3f a, Vector3f b, float t) {
        return new Vector3f(a).lerp(b, t);
    }

    private static final class Mesh {
        private final VertexConsumer consumer;
        private final PoseStack.Pose pose;
        private final int light;

        Mesh(VertexConsumer consumer, PoseStack.Pose pose, int light) {
            this.consumer = consumer;
            this.pose = pose;
            this.light = light;
        }

        /** One sail triangle, UV-mapped top-down onto the sail art; lower skin is offset and darker. */
        void sail(Vector3f a, Vector3f b, Vector3f c, float offset, int colour) {
            Vector3f normal = new Vector3f(b).sub(a).cross(new Vector3f(c).sub(a)).normalize();
            // +z is up. Flip the mirrored wing so both top faces take the light.
            boolean facesUp = normal.z >= 0.0F;
            if (facesUp != offset >= 0.0F) {
                Vector3f swap = b;
                b = c;
                c = swap;
                normal.negate();
            }
            Vector3f[] points = {a, b, c, c};
            for (Vector3f p : points) {
                vertex(p.x, p.y, p.z + offset, (p.x + SAIL_U) / TEX_W, (p.y + SAIL_V) / TEX_H, normal, colour);
            }
        }

        /** Square tube from {@code from} to {@code to}, sampling one solid colour cell. */
        void beam(Vector3f from, Vector3f to, float width, int cell, int colour) {
            Vector3f axis = new Vector3f(to).sub(from).normalize();
            Vector3f ref = Math.abs(axis.z) > 0.9F ? new Vector3f(1, 0, 0) : new Vector3f(0, 0, 1);
            Vector3f a = new Vector3f(axis).cross(ref).normalize().mul(width / 2);
            Vector3f b = new Vector3f(axis).cross(a).normalize().mul(width / 2);
            float u = (72.0F + cell * 8.0F + 4.0F) / TEX_W;
            float v = 4.0F / TEX_H;

            Vector3f[] start = corners(from, a, b);
            Vector3f[] end = corners(to, a, b);
            for (int i = 0; i < 4; i++) {
                int j = (i + 1) % 4;
                Vector3f normal = new Vector3f(start[i]).add(start[j]).sub(from).sub(from).normalize();
                quad(start[i], start[j], end[j], end[i], normal, u, v, colour);
            }
            quad(start[0], start[1], start[2], start[3], new Vector3f(axis).negate(), u, v, colour);
            quad(end[3], end[2], end[1], end[0], axis, u, v, colour);
        }

        private static Vector3f[] corners(Vector3f centre, Vector3f a, Vector3f b) {
            return new Vector3f[] {
                    new Vector3f(centre).add(a).add(b),
                    new Vector3f(centre).sub(a).add(b),
                    new Vector3f(centre).sub(a).sub(b),
                    new Vector3f(centre).add(a).sub(b)
            };
        }

        private void quad(Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3, Vector3f normal, float u, float v,
                          int colour) {
            vertex(p0.x, p0.y, p0.z, u, v, normal, colour);
            vertex(p1.x, p1.y, p1.z, u, v, normal, colour);
            vertex(p2.x, p2.y, p2.z, u, v, normal, colour);
            vertex(p3.x, p3.y, p3.z, u, v, normal, colour);
        }

        private void vertex(float x, float y, float z, float u, float v, Vector3f normal, int colour) {
            consumer.addVertex(pose, x, y, z)
                    .setColor((colour >> 16) & 0xFF, (colour >> 8) & 0xFF, colour & 0xFF, 255)
                    .setUv(u, v)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(pose, normal.x, normal.y, normal.z);
        }
    }
}
