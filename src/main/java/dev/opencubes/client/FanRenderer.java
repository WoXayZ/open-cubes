package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.opencubes.OCConstants;
import dev.opencubes.content.fan.FanBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Draws the head of the fan: the hoop and the blades inside it.
 *
 * <p>The yaw lives on the block entity rather than in a block state, so neither piece can be
 * baked into the blockstate file. Both are drawn here off the same hub so they cannot drift
 * apart; only the base plate and its post stay behind as a static block model, and those are
 * rotationally symmetric so they do not need the yaw.
 */
public class FanRenderer implements BlockEntityRenderer<FanBlockEntity> {

    public static final ModelResourceLocation BLADES_MODEL =
            ModelResourceLocation.standalone(OCConstants.id("block/fan_blades"));
    public static final ModelResourceLocation FRAME_MODEL =
            ModelResourceLocation.standalone(OCConstants.id("block/fan_frame"));

    /** Height of the blade axle above the block floor, matching the models. */
    private static final double HUB_HEIGHT = 10.0D / 16.0D;

    private final BlockEntityRendererProvider.Context context;

    public FanRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(FanBlockEntity fan, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int light, int overlay) {
        BakedModel frame = model(FRAME_MODEL);
        BakedModel blades = model(BLADES_MODEL);
        if (frame == null || blades == null) {
            return;
        }

        BlockState state = fan.getBlockState();
        float yaw = fan.getYaw();

        poseStack.pushPose();
        hub(poseStack, yaw);
        draw(poseStack, buffers, state, frame, light, overlay);
        poseStack.popPose();

        poseStack.pushPose();
        hub(poseStack, yaw);
        poseStack.translate(0.5D, HUB_HEIGHT, 0.5D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(fan.getBladeRotation(partialTick)));
        poseStack.translate(-0.5D, -HUB_HEIGHT, -0.5D);
        draw(poseStack, buffers, state, blades, light, overlay);
        poseStack.popPose();
    }

    private static void hub(PoseStack poseStack, float yaw) {
        poseStack.translate(0.5D, HUB_HEIGHT, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.translate(-0.5D, -HUB_HEIGHT, -0.5D);
    }

    private void draw(PoseStack poseStack, MultiBufferSource buffers, BlockState state, BakedModel model,
                      int light, int overlay) {
        context.getBlockRenderDispatcher().getModelRenderer().renderModel(
                poseStack.last(),
                buffers.getBuffer(RenderType.cutout()),
                state,
                model,
                1.0F, 1.0F, 1.0F,
                light, overlay,
                ModelData.EMPTY,
                RenderType.cutout());
    }

    private BakedModel model(ModelResourceLocation location) {
        return context.getBlockRenderDispatcher().getBlockModelShaper().getModelManager().getModel(location);
    }
}
