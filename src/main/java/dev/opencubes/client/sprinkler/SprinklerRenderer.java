package dev.opencubes.client.sprinkler;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.opencubes.OCConstants;
import dev.opencubes.content.sprinkler.SprinklerBlock;
import dev.opencubes.content.sprinkler.SprinklerBlockEntity;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.block.model.BlockStateModelWrapper;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

/**
 * Rocks the sprinkler arm around its long axis while water is active, matching the spray side.
 *
 * <p>The arm is a standalone model. Register it on {@code ModelEvent.RegisterStandalone} with
 * {@link #ARM_MODEL} and {@link #ARM_BAKER}.
 */
public class SprinklerRenderer implements BlockEntityRenderer<SprinklerBlockEntity, SprinklerRenderState> {

    public static final Identifier ARM_LOCATION = OCConstants.id("block/sprinkler_arm");
    public static final StandaloneModelKey<BlockStateModel> ARM_MODEL =
            new StandaloneModelKey<>(ARM_LOCATION::toString);
    public static final SimpleUnbakedStandaloneModel<BlockStateModel> ARM_BAKER =
            SimpleUnbakedStandaloneModel.blockStateModel(ARM_LOCATION);

    @Override
    public SprinklerRenderState createRenderState() {
        return new SprinklerRenderState();
    }

    public SprinklerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void extractRenderState(
            SprinklerBlockEntity blockEntity,
            SprinklerRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.blockState = blockEntity.getBlockState();
        state.facing = state.blockState.getValue(SprinklerBlock.FACING);
        state.tilt = blockEntity.getArmTilt(partialTicks);
    }

    @Override
    public void submit(SprinklerRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        BlockStateModel arm = Minecraft.getInstance().getModelManager().getStandaloneModel(ARM_MODEL);
        if (arm == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5D, SprinklerBlockEntity.PIVOT_Y, 0.5D);
        poseStack.mulPose(SprinklerBlockEntity.armRotation(state.facing, state.tilt));
        poseStack.translate(-0.5D, -SprinklerBlockEntity.PIVOT_Y, -0.5D);
        submitBlockModel(poseStack, submitNodeCollector, state.blockState, arm, state.lightCoords);
        poseStack.popPose();
    }

    public static void submitBlockModel(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, BlockState state,
                                  BlockStateModel model, int light) {
        BlockModelRenderState renderState = new BlockModelRenderState();
        new BlockStateModelWrapper(model, List.<BlockTintSource>of(), new Matrix4f())
                .update(renderState, state, BlockDisplayContext.create(), 0L);
        renderState.submitMultiLayer(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, 0);
    }
}
