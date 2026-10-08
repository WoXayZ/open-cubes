package dev.opencubes.client.crane;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.opencubes.content.crane.MagnetEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * The stepped magnet, plus a striped cable up to the owner's boom. In first person the wearer's
 * own model is hidden, so the boom is drawn here too.
 */
public class MagnetRenderer extends EntityRenderer<MagnetEntity, MagnetRenderState> {

    private static final Identifier WHITE = Identifier.withDefaultNamespace("textures/misc/white.png");
    private static final double CABLE_HALF_WIDTH = 0.025D;
    private static final double STRIPE = 0.125D;
    private static final int YELLOW = 0xFFF0C820;
    private static final int BLACK = 0xFF262626;

    private final ModelPart magnet;
    private final ModelPart arm;

    public MagnetRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.3F;
        this.magnet = CraneModels.createMagnet().getChild("magnet");
        this.arm = CraneModels.createBackpack().getChild("body").getChild("arm");
    }

    @Override
    public MagnetRenderState createRenderState() {
        return new MagnetRenderState();
    }

    @Override
    public boolean shouldRender(MagnetEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        // The cable can be on screen while the magnet itself is not.
        return entity.getOwner() != null || super.shouldRender(entity, frustum, camX, camY, camZ);
    }

    @Override
    public void extractRenderState(MagnetEntity entity, MagnetRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        Player owner = entity.getOwner();
        state.hasCable = owner != null;
        if (owner == null) {
            return;
        }
        Vec3 origin = entity.getPosition(partialTicks);
        state.cableTip = CraneModels.boomTip(owner, partialTicks).subtract(origin);
        state.cableLight = LightCoordsUtil.pack(
                Math.max(LightCoordsUtil.block(state.lightCoords), 4),
                LightCoordsUtil.sky(state.lightCoords));
        Minecraft mc = Minecraft.getInstance();
        state.drawBoom = owner == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson();
        state.boomPivot = CraneModels.boomPivot(owner, partialTicks).subtract(origin);
        state.headYaw = Mth.rotLerp(partialTicks, owner.yHeadRotO, owner.yHeadRot);
        state.boomLight = state.cableLight;
    }

    @Override
    public void submit(MagnetRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(2.0F, 2.0F, 2.0F);
        int light = state.lightCoords;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutoutCull(CraneModels.MAGNET_TEXTURE),
                (pose, buffer) -> {
                    PoseStack modelPose = new PoseStack();
                    modelPose.last().set(pose);
                    this.magnet.render(modelPose, buffer, light, OverlayTexture.NO_OVERLAY);
                });
        poseStack.popPose();

        if (state.hasCable) {
            Vec3 tip = state.cableTip;
            int cableLight = state.cableLight;
            submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(WHITE), (pose, buffer) ->
                    renderCable(buffer, pose.pose(), new Vec3(0.0D, CraneModels.MAGNET_TOP, 0.0D), tip, cableLight));
            if (state.drawBoom) {
                Vec3 pivot = state.boomPivot;
                float headYaw = state.headYaw;
                int boomLight = state.boomLight;
                poseStack.pushPose();
                poseStack.translate(pivot.x, pivot.y, pivot.z);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - headYaw));
                poseStack.scale(-1.0F, -1.0F, 1.0F);
                submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutoutCull(CraneModels.BACKPACK_TEXTURE),
                        (pose, buffer) -> {
                            this.arm.setPos(0.0F, 0.0F, 0.0F);
                            this.arm.yRot = Mth.PI;
                            PoseStack modelPose = new PoseStack();
                            modelPose.last().set(pose);
                            this.arm.render(modelPose, buffer, boomLight, OverlayTexture.NO_OVERLAY);
                        });
                poseStack.popPose();
            }
        }
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    /** A thin square tube from {@code from} to {@code to}, in alternating yellow and black bands. */
    private static void renderCable(VertexConsumer consumer, Matrix4f matrix, Vec3 from, Vec3 to, int light) {
        Vec3 span = to.subtract(from);
        double length = span.length();
        if (length < 1.0E-3D) {
            return;
        }
        Vec3 dir = span.scale(1.0D / length);
        Vec3 side = dir.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 1.0E-6D) {
            side = new Vec3(1.0D, 0.0D, 0.0D);
        }
        side = side.normalize().scale(CABLE_HALF_WIDTH);
        Vec3 other = dir.cross(side).normalize().scale(CABLE_HALF_WIDTH);
        Vec3[] corners = {side.add(other), side.subtract(other), side.reverse().subtract(other), side.reverse().add(other)};

        int bands = Math.max(1, Mth.ceil(length / STRIPE));
        for (int band = 0; band < bands; band++) {
            Vec3 a = from.add(span.scale((double) band / bands));
            Vec3 b = from.add(span.scale((double) (band + 1) / bands));
            int colour = band % 2 == 0 ? YELLOW : BLACK;
            for (int i = 0; i < 4; i++) {
                Vec3 c0 = corners[i];
                Vec3 c1 = corners[(i + 1) % 4];
                Vec3 normal = c0.add(c1).normalize();
                vertex(consumer, matrix, a.add(c0), colour, normal, light);
                vertex(consumer, matrix, b.add(c0), colour, normal, light);
                vertex(consumer, matrix, b.add(c1), colour, normal, light);
                vertex(consumer, matrix, a.add(c1), colour, normal, light);
            }
        }
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Vec3 pos, int colour, Vec3 normal, int light) {
        consumer.addVertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(colour)
                .setUv(0.5F, 0.5F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal((float) normal.x, (float) normal.y, (float) normal.z);
    }
}
