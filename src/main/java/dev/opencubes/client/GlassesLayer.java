package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.OCConstants;
import dev.opencubes.content.imaginary.ImaginationGlassesItem;
import dev.opencubes.content.imaginary.ImaginationGlassesKind;
import dev.opencubes.content.vision.SonicGlassesItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

/**
 * Sonic glasses use the 1.12 model under an iron helmet. The other glasses put the lens part of
 * their item icon on the face, with arms back to the ears.
 */
public class GlassesLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {

    private static final Identifier SONIC = OCConstants.id("textures/entity/glasses.png");
    private static final Identifier IRON =
            Identifier.withDefaultNamespace("textures/models/armor/iron_layer_1.png");

    /** Model units are pixels; poses from {@code translateAndRotate} are in blocks. */
    private static final float PIXEL = 1.0F / 16.0F;
    /** Just outside the hat layer, which sits half a pixel off the head. */
    private static final float FACE_Z = -4.55F;
    private static final float SIDE_X = 4.6F;
    private static final float LENS_HALF_WIDTH = 4.7F;
    private static final float LENS_CENTRE_Y = -4.0F;
    private static final float EAR_Z = 0.5F;

    private final GlassesModel sonic;
    private final ModelPart helmet;

    public GlassesLayer(RenderLayerParent<S, M> parent, EntityModelSet models) {
        super(parent);
        this.sonic = new GlassesModel(models.bakeLayer(GlassesModel.LAYER));
        this.helmet = models.bakeLayer(ModelLayers.PLAYER_ARMOR.head());
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, S state,
                       float yRot, float xRot) {
        ItemStack stack = state.headEquipment;
        if (stack.getItem() instanceof SonicGlassesItem) {
            renderSonic(poseStack, submitNodeCollector, packedLight, state);
        } else if (stack.getItem() instanceof ImaginationGlassesItem glasses) {
            renderFrames(poseStack, submitNodeCollector, packedLight, stack, glasses);
        }
    }

    private void renderSonic(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, S state) {
        helmet.loadPose(getParentModel().head.storePose());
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.armorCutoutNoCull(IRON),
                (pose, buffer) -> helmet.render(PoseRender.stack(pose), buffer, light, OverlayTexture.NO_OVERLAY));

        poseStack.pushPose();
        getParentModel().head.translateAndRotate(poseStack);
        poseStack.scale(1.2F, 1.2F, 1.2F);
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(SONIC),
                (pose, buffer) -> sonic.draw(PoseRender.stack(pose), buffer, light));
        poseStack.popPose();
    }

    private void renderFrames(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, ItemStack stack,
                              ImaginationGlassesItem glasses) {
        Lens lens = Lens.of(glasses);
        int colour = 0xFFFFFF;
        if (glasses.kind() == ImaginationGlassesKind.CRAYON) {
            Integer crayon = ImaginationGlassesItem.getCrayonColour(stack);
            if (crayon != null) {
                colour = crayon;
            }
        }

        poseStack.pushPose();
        getParentModel().head.translateAndRotate(poseStack);
        poseStack.scale(PIXEL, PIXEL, PIXEL);
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager()
                .get(new SpriteId(TextureAtlas.LOCATION_ITEMS, lens.sprite()));
        int tint = colour;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(sprite.atlasLocation()),
                (pose, buffer) -> drawFrames(sprite.wrap(buffer), pose.pose(), light, tint, lens));
        poseStack.popPose();
    }

    private void drawFrames(VertexConsumer consumer, Matrix4f matrix, int light, int colour, Lens lens) {
        float halfHeight = LENS_HALF_WIDTH * (lens.v1() - lens.v0()) / (lens.u1() - lens.u0());
        float top = LENS_CENTRE_Y - halfHeight;
        float bottom = LENS_CENTRE_Y + halfHeight;
        Quad front = new Quad(consumer, matrix, light, colour);
        front.uv(lens.u0() / 16.0F, lens.v0() / 16.0F, lens.u1() / 16.0F, lens.v1() / 16.0F);
        front.plate(-LENS_HALF_WIDTH, top, LENS_HALF_WIDTH, bottom, FACE_Z);

        // A single frame pixel of the icon, stretched along each arm.
        Quad arms = new Quad(consumer, matrix, light, colour);
        arms.uv(lens.frameU() / 16.0F, lens.frameV() / 16.0F,
                (lens.frameU() + 1) / 16.0F, (lens.frameV() + 1) / 16.0F);
        float armTop = top + 0.3F;
        arms.box(-SIDE_X - 0.4F, armTop, FACE_Z, -SIDE_X, armTop + 0.6F, EAR_Z);
        arms.box(SIDE_X, armTop, FACE_Z, SIDE_X + 0.4F, armTop + 0.6F, EAR_Z);
    }

    /** Lens rectangle and one solid frame pixel of each glasses icon, in icon pixels. */
    private record Lens(Identifier sprite, float u0, float v0, float u1, float v1, int frameU, int frameV) {
        static Lens of(ImaginationGlassesItem glasses) {
            return switch (glasses.kind()) {
                case PENCIL -> new Lens(icon("glasses_pencil"), 0, 5, 16, 12, 0, 6);
                case CRAYON -> new Lens(icon("glasses_crayon"), 0, 5, 16, 12, 0, 6);
                case TECHNICOLOR -> new Lens(icon("glasses_technicolor"), 0, 5, 16, 12, 0, 6);
                case ADMIN -> new Lens(icon("glasses_admin"), 0, 5, 16, 12, 0, 6);
            };
        }

        private static Identifier icon(String name) {
            return OCConstants.id("item/" + name);
        }
    }

    private static final class Quad {
        private final VertexConsumer consumer;
        private final Matrix4f matrix;
        private final int light;
        private final int colour;
        private float u0;
        private float v0;
        private float u1;
        private float v1;

        Quad(VertexConsumer consumer, Matrix4f matrix, int light, int colour) {
            this.consumer = consumer;
            this.matrix = matrix;
            this.light = light;
            this.colour = colour;
        }

        void uv(float u0, float v0, float u1, float v1) {
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
        }

        /** Front plate facing -Z; the render type does not cull, so it also shows from behind. */
        void plate(float x0, float y0, float x1, float y1, float z) {
            face(0, 0, -1, x0, y0, z, u0, v0, x1, y0, z, u1, v0, x1, y1, z, u1, v1, x0, y1, z, u0, v1);
        }

        void box(float x0, float y0, float z0, float x1, float y1, float z1) {
            face(0, -1, 0, x0, y0, z1, u0, v0, x1, y0, z1, u1, v0, x1, y0, z0, u1, v1, x0, y0, z0, u0, v1);
            face(0, 1, 0, x0, y1, z0, u0, v0, x1, y1, z0, u1, v0, x1, y1, z1, u1, v1, x0, y1, z1, u0, v1);
            face(-1, 0, 0, x0, y0, z0, u0, v0, x0, y0, z1, u1, v0, x0, y1, z1, u1, v1, x0, y1, z0, u0, v1);
            face(1, 0, 0, x1, y0, z1, u0, v0, x1, y0, z0, u1, v0, x1, y1, z0, u1, v1, x1, y1, z1, u0, v1);
        }

        private void face(float nx, float ny, float nz,
                          float xa, float ya, float za, float ua, float va,
                          float xb, float yb, float zb, float ub, float vb,
                          float xc, float yc, float zc, float uc, float vc,
                          float xd, float yd, float zd, float ud, float vd) {
            vertex(nx, ny, nz, xa, ya, za, ua, va);
            vertex(nx, ny, nz, xb, yb, zb, ub, vb);
            vertex(nx, ny, nz, xc, yc, zc, uc, vc);
            vertex(nx, ny, nz, xd, yd, zd, ud, vd);
        }

        private void vertex(float nx, float ny, float nz, float x, float y, float z, float u, float v) {
            consumer.addVertex(matrix, x, y, z)
                    .setColor((colour >> 16) & 0xFF, (colour >> 8) & 0xFF, colour & 0xFF, 255)
                    .setUv(u, v)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(nx, ny, nz);
        }
    }
}
