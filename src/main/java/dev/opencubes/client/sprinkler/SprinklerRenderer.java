package dev.opencubes.client.sprinkler;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.opencubes.OCConstants;
import dev.opencubes.content.sprinkler.SprinklerBlock;
import dev.opencubes.content.sprinkler.SprinklerBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Rocks the sprinkler arm around its long axis while water is active, matching the spray side.
 */
public class SprinklerRenderer implements BlockEntityRenderer<SprinklerBlockEntity> {

    public static final ModelResourceLocation ARM_MODEL =
            ModelResourceLocation.standalone(OCConstants.id("block/sprinkler_arm"));

    private final BlockEntityRendererProvider.Context context;

    public SprinklerRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(SprinklerBlockEntity sprinkler, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int light, int overlay) {
        BakedModel arm = model(ARM_MODEL);
        if (arm == null) {
            return;
        }

        BlockState state = sprinkler.getBlockState();
        float tilt = sprinkler.getArmTilt(partialTick);

        poseStack.pushPose();
        poseStack.translate(0.5D, SprinklerBlockEntity.PIVOT_Y, 0.5D);
        poseStack.mulPose(SprinklerBlockEntity.armRotation(state.getValue(SprinklerBlock.FACING), tilt));
        poseStack.translate(-0.5D, -SprinklerBlockEntity.PIVOT_Y, -0.5D);
        draw(poseStack, buffers, state, arm, light, overlay);
        poseStack.popPose();
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
