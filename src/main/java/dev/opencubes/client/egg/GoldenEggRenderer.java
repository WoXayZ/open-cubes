package dev.opencubes.client.egg;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import dev.opencubes.content.egg.GoldenEggBlockEntity;
import java.util.Random;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;

/**
 * OpenBlocks golden egg visuals: accelerating spin, rise, translucent phantom, and rainbow star
 * beams while floating / falling.
 */
public class GoldenEggRenderer implements BlockEntityRenderer<GoldenEggBlockEntity> {

    private static final float PHANTOM_SCALE = 1.5F;
    private static final float BEAM_START_DISTANCE = 2.0F;
    private static final float BEAM_END_DISTANCE = 10.0F;
    private static final float MAX_OPACITY = 192.0F;
    private static final Random STAR_RANDOM = new Random(432L);

    private final BlockEntityRendererProvider.Context context;
    private final RandomSource modelRandom = RandomSource.create();

    public GoldenEggRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(GoldenEggBlockEntity egg, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BlockState state = egg.getBlockState();
        float rotation = egg.isIdle() ? 0.0F : egg.getRotation(partialTick);
        float progress = egg.isIdle() ? 0.0F : egg.getRiseProgress(partialTick);
        float offset = egg.isIdle() ? 0.0F : egg.getOffset(partialTick);

        poseStack.pushPose();
        poseStack.translate(0.5D, offset, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        renderEggModel(state, poseStack, buffers, packedLight, packedOverlay);

        if (egg.phase().specialEffects) {
            renderPhantom(state, poseStack, buffers, packedLight, packedOverlay, progress);
            renderStar(poseStack, rotation, progress);
        }

        poseStack.popPose();
    }

    private void renderEggModel(BlockState state, PoseStack poseStack, MultiBufferSource buffers,
                                int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        BakedModel model = context.getBlockRenderDispatcher().getBlockModel(state);
        ModelBlockRenderer modelRenderer = context.getBlockRenderDispatcher().getModelRenderer();
        for (var renderType : model.getRenderTypes(state, modelRandom, ModelData.EMPTY)) {
            modelRenderer.renderModel(
                    poseStack.last(),
                    buffers.getBuffer(renderType),
                    state,
                    model,
                    1.0F, 1.0F, 1.0F,
                    packedLight,
                    packedOverlay,
                    ModelData.EMPTY,
                    renderType);
        }
        poseStack.popPose();
    }

    private void renderPhantom(BlockState state, PoseStack poseStack, MultiBufferSource buffers,
                               int packedLight, int packedOverlay, float progress) {
        float scale = PHANTOM_SCALE * (0.2F + progress * 0.8F);
        poseStack.pushPose();
        poseStack.translate(0.0D, -0.1D * progress, 0.0D);
        poseStack.scale(scale, scale, scale);
        // Slightly brighten via light boost; alpha is handled by translucent cutout layers poorly,
        // so we just draw a larger second egg for the glowing shell look.
        renderEggModel(state, poseStack, buffers, 0xF000F0, packedOverlay);
        poseStack.popPose();
    }

    /**
     * Port of OpenBlocks {@code renderStar} (same math as the dragon death burst).
     */
    private static void renderStar(PoseStack poseStack, float rotation, float progress) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.5D, 0.0D);
        // Opposite spin at ~20% speed, slightly tilted (OpenBlocks star burst).
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation * -0.2F));
        poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));

        float fade = 0.0F;
        if (progress > 0.8F) {
            fade = (progress - 0.8F) / 0.2F;
        }
        int alpha = (int) (MAX_OPACITY * (1.0F - fade));
        int beams = (int) ((progress + progress * progress) / 2.0F * 60.0F);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        STAR_RANDOM.setSeed(432L);
        Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < beams; i++) {
            poseStack.mulPose(Axis.XP.rotationDegrees(STAR_RANDOM.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(STAR_RANDOM.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(STAR_RANDOM.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(STAR_RANDOM.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(STAR_RANDOM.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(STAR_RANDOM.nextFloat() * 360.0F + progress * 90.0F));
            matrix = poseStack.last().pose();

            float length = STAR_RANDOM.nextFloat() * BEAM_END_DISTANCE + 5.0F + fade * 10.0F;
            float width = STAR_RANDOM.nextFloat() * BEAM_START_DISTANCE + 1.0F + fade * 2.0F;

            BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN,
                    DefaultVertexFormat.POSITION_COLOR);
            buffer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(255, 255, 255, alpha);
            buffer.addVertex(matrix, (float) (-0.866D * width), length, -0.5F * width)
                    .setColor(255, 0, 255, 0);
            buffer.addVertex(matrix, (float) (0.866D * width), length, -0.5F * width)
                    .setColor(255, 0, 255, 0);
            buffer.addVertex(matrix, 0.0F, length, 1.0F * width).setColor(255, 0, 255, 0);
            buffer.addVertex(matrix, (float) (-0.866D * width), length, -0.5F * width)
                    .setColor(255, 0, 255, 0);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(GoldenEggBlockEntity egg) {
        return !egg.isIdle();
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
