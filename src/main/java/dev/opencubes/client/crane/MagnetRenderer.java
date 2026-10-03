package dev.opencubes.client.crane;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.opencubes.content.crane.MagnetEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * The stepped magnet, plus a striped cable up to the owner's boom. In first person the wearer's
 * own model is hidden, so the boom is drawn here too.
 */
public class MagnetRenderer extends EntityRenderer<MagnetEntity> {

    private static final ResourceLocation WHITE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
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
    public boolean shouldRender(MagnetEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        // The cable can be on screen while the magnet itself is not.
        return entity.getOwner() != null || super.shouldRender(entity, frustum, camX, camY, camZ);
    }

    @Override
    public void render(MagnetEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(2.0F, 2.0F, 2.0F);
        magnet.render(poseStack, buffers.getBuffer(RenderType.entityCutout(CraneModels.MAGNET_TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        Player owner = entity.getOwner();
        if (owner != null) {
            Vec3 origin = entity.getPosition(partialTick);
            Vec3 tip = CraneModels.boomTip(owner, partialTick).subtract(origin);
            int light = LightTexture.pack(Math.max(LightTexture.block(packedLight), 4), LightTexture.sky(packedLight));
            renderCable(poseStack, buffers, new Vec3(0.0D, CraneModels.MAGNET_TOP, 0.0D), tip, light);
            Minecraft mc = Minecraft.getInstance();
            if (owner == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson()) {
                renderBoom(poseStack, buffers, owner, origin, partialTick, light);
            }
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    private void renderBoom(PoseStack poseStack, MultiBufferSource buffers, Player owner, Vec3 origin,
                            float partialTick, int light) {
        Vec3 pivot = CraneModels.boomPivot(owner, partialTick).subtract(origin);
        float headYaw = Mth.rotLerp(partialTick, owner.yHeadRotO, owner.yHeadRot);
        poseStack.pushPose();
        poseStack.translate(pivot.x, pivot.y, pivot.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - headYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        arm.setPos(0.0F, 0.0F, 0.0F);
        arm.yRot = Mth.PI;
        arm.render(poseStack, buffers.getBuffer(RenderType.entityCutout(CraneModels.BACKPACK_TEXTURE)),
                light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    /** A thin square tube from {@code from} to {@code to}, in alternating yellow and black bands. */
    private static void renderCable(PoseStack poseStack, MultiBufferSource buffers, Vec3 from, Vec3 to, int light) {
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

        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(WHITE));
        Matrix4f matrix = poseStack.last().pose();
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

    @Override
    public ResourceLocation getTextureLocation(MagnetEntity entity) {
        return CraneModels.MAGNET_TEXTURE;
    }
}
