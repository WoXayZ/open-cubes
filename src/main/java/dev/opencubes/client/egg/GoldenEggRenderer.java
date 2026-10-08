package dev.opencubes.client.egg;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.opencubes.content.egg.GoldenEggBlockEntity;
import java.util.Random;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * OpenBlocks golden egg visuals: accelerating spin, rise, translucent phantom, and rainbow star
 * beams while floating or falling.
 *
 * <p>The star used to draw with an immediate additive triangle fan. The submit pipeline has no
 * custom blend, so each beam is a pair of dragon-ray triangles (additive, depth write off),
 * emitted both ways so the beam stays visible from either side.
 */
public class GoldenEggRenderer implements BlockEntityRenderer<GoldenEggBlockEntity, GoldenEggRenderState> {

    private static final float PHANTOM_SCALE = 1.5F;
    private static final float BEAM_END_DISTANCE = 10.0F;
    private static final float BEAM_START_DISTANCE = 2.0F;
    private static final float MAX_OPACITY = 192.0F;
    private static final Random STAR_RANDOM = new Random(432L);

    private final BlockModelResolver blockModelResolver;
    private final BlockModelRenderState eggModel = new BlockModelRenderState();

    public GoldenEggRenderer(BlockEntityRendererProvider.Context context) {
        this.blockModelResolver = context.blockModelResolver();
    }

    @Override
    public GoldenEggRenderState createRenderState() {
        return new GoldenEggRenderState();
    }

    @Override
    public void extractRenderState(
            GoldenEggBlockEntity blockEntity,
            GoldenEggRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.blockState = blockEntity.getBlockState();
        state.idle = blockEntity.isIdle();
        state.rotation = state.idle ? 0.0F : blockEntity.getRotation(partialTicks);
        state.progress = state.idle ? 0.0F : blockEntity.getRiseProgress(partialTicks);
        state.offset = state.idle ? 0.0F : blockEntity.getOffset(partialTicks);
        state.specialEffects = blockEntity.phase().specialEffects;
    }

    @Override
    public void submit(GoldenEggRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5D, state.offset, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));

        submitEgg(state.blockState, poseStack, submitNodeCollector, state.lightCoords);

        if (state.specialEffects) {
            float scale = PHANTOM_SCALE * (0.2F + state.progress * 0.8F);
            poseStack.pushPose();
            poseStack.translate(0.0D, -0.1D * state.progress, 0.0D);
            poseStack.scale(scale, scale, scale);
            submitEgg(state.blockState, poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT);
            poseStack.popPose();
            renderStar(poseStack, submitNodeCollector, state.rotation, state.progress);
        }

        poseStack.popPose();
    }

    private void submitEgg(BlockState blockState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light) {
        poseStack.pushPose();
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        this.blockModelResolver.update(this.eggModel, blockState, BlockDisplayContext.create());
        this.eggModel.submitMultiLayer(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    /**
     * Port of OpenBlocks renderStar (same math as the dragon death burst).
     */
    private static void renderStar(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, float rotation, float progress) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.5D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation * -0.2F));
        poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));

        float fade = 0.0F;
        if (progress > 0.8F) {
            fade = (progress - 0.8F) / 0.2F;
        }
        int alpha = (int) (MAX_OPACITY * (1.0F - fade));
        int beams = (int) ((progress + progress * progress) / 2.0F * 60.0F);

        STAR_RANDOM.setSeed(432L);
        for (int i = 0; i < beams; i++) {
            poseStack.mulPose(Axis.XP.rotationDegrees(STAR_RANDOM.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(STAR_RANDOM.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(STAR_RANDOM.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(STAR_RANDOM.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(STAR_RANDOM.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(STAR_RANDOM.nextFloat() * 360.0F + progress * 90.0F));

            float length = STAR_RANDOM.nextFloat() * BEAM_END_DISTANCE + 5.0F + fade * 10.0F;
            float width = STAR_RANDOM.nextFloat() * BEAM_START_DISTANCE + 1.0F + fade * 2.0F;
            int beamAlpha = alpha;
            submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.dragonRays(), (pose, buffer) ->
                    beam(buffer, pose, width, length, beamAlpha));
        }

        poseStack.popPose();
    }

    private static void beam(VertexConsumer buffer, PoseStack.Pose pose, float width, float length, int alpha) {
        float x0 = (float) (-0.866D * width);
        float x1 = (float) (0.866D * width);
        float z0 = -0.5F * width;
        float z1 = width;
        triangle(buffer, pose, 0.0F, 0.0F, 0.0F, 255, 255, 255, alpha, x0, length, z0, 255, 0, 255, 0, x1, length, z0, 255, 0, 255, 0);
        triangle(buffer, pose, 0.0F, 0.0F, 0.0F, 255, 255, 255, alpha, x1, length, z0, 255, 0, 255, 0, 0.0F, length, z1, 255, 0, 255, 0);
        triangle(buffer, pose, 0.0F, 0.0F, 0.0F, 255, 255, 255, alpha, 0.0F, length, z1, 255, 0, 255, 0, x0, length, z0, 255, 0, 255, 0);
        triangle(buffer, pose, 0.0F, 0.0F, 0.0F, 255, 255, 255, alpha, x1, length, z0, 255, 0, 255, 0, x0, length, z0, 255, 0, 255, 0);
        triangle(buffer, pose, 0.0F, 0.0F, 0.0F, 255, 255, 255, alpha, 0.0F, length, z1, 255, 0, 255, 0, x1, length, z0, 255, 0, 255, 0);
        triangle(buffer, pose, 0.0F, 0.0F, 0.0F, 255, 255, 255, alpha, x0, length, z0, 255, 0, 255, 0, 0.0F, length, z1, 255, 0, 255, 0);
    }

    private static void triangle(VertexConsumer buffer, PoseStack.Pose pose,
                                 float x0, float y0, float z0, int r0, int g0, int b0, int a0,
                                 float x1, float y1, float z1, int r1, int g1, int b1, int a1,
                                 float x2, float y2, float z2, int r2, int g2, int b2, int a2) {
        buffer.addVertex(pose, x0, y0, z0).setColor(r0, g0, b0, a0);
        buffer.addVertex(pose, x1, y1, z1).setColor(r1, g1, b1, a1);
        buffer.addVertex(pose, x2, y2, z2).setColor(r2, g2, b2, a2);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        // The old check was per egg (!idle). Off-screen rendering no longer receives the block entity.
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(GoldenEggBlockEntity egg) {
        BlockPos pos = egg.getBlockPos();
        return new AABB(pos.getX() - 8.0D, pos.getY() - 2.0D, pos.getZ() - 8.0D,
                pos.getX() + 9.0D, pos.getY() + GoldenEggBlockEntity.MAX_HEIGHT + 12.0D, pos.getZ() + 9.0D);
    }
}
