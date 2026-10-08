package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.opencubes.OCConstants;
import dev.opencubes.client.sprinkler.SprinklerRenderer;
import dev.opencubes.content.fan.FanBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jspecify.annotations.Nullable;

/**
 * Draws the head of the fan: the hoop and the blades inside it.
 *
 * <p>The yaw lives on the block entity rather than in a block state, so neither piece can be
 * baked into the blockstate file. Both are drawn here off the same hub so they cannot drift
 * apart; only the base plate and its post stay behind as a static block model, and those are
 * rotationally symmetric so they do not need the yaw.
 *
 * <p>Register both models on {@code ModelEvent.RegisterStandalone}:
 * {@code event.register(BLADES_MODEL, BLADES_BAKER)} and the same for the frame.
 */
public class FanRenderer implements BlockEntityRenderer<FanBlockEntity, FanRenderState> {

    public static final Identifier BLADES_LOCATION = OCConstants.id("block/fan_blades");
    public static final Identifier FRAME_LOCATION = OCConstants.id("block/fan_frame");
    public static final StandaloneModelKey<BlockStateModel> BLADES_MODEL =
            new StandaloneModelKey<>(BLADES_LOCATION::toString);
    public static final StandaloneModelKey<BlockStateModel> FRAME_MODEL =
            new StandaloneModelKey<>(FRAME_LOCATION::toString);
    public static final SimpleUnbakedStandaloneModel<BlockStateModel> BLADES_BAKER =
            SimpleUnbakedStandaloneModel.blockStateModel(BLADES_LOCATION);
    public static final SimpleUnbakedStandaloneModel<BlockStateModel> FRAME_BAKER =
            SimpleUnbakedStandaloneModel.blockStateModel(FRAME_LOCATION);

    /** Height of the blade axle above the block floor, matching the models. */
    private static final double HUB_HEIGHT = 10.0D / 16.0D;

    public FanRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public FanRenderState createRenderState() {
        return new FanRenderState();
    }

    @Override
    public void extractRenderState(
            FanBlockEntity blockEntity,
            FanRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.blockState = blockEntity.getBlockState();
        state.yaw = blockEntity.getYaw();
        state.bladeRotation = blockEntity.getBladeRotation(partialTicks);
    }

    @Override
    public void submit(FanRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        BlockStateModel frame = Minecraft.getInstance().getModelManager().getStandaloneModel(FRAME_MODEL);
        BlockStateModel blades = Minecraft.getInstance().getModelManager().getStandaloneModel(BLADES_MODEL);
        if (frame == null || blades == null) {
            return;
        }

        poseStack.pushPose();
        hub(poseStack, state.yaw);
        SprinklerRenderer.submitBlockModel(poseStack, submitNodeCollector, state.blockState, frame, state.lightCoords);
        poseStack.popPose();

        poseStack.pushPose();
        hub(poseStack, state.yaw);
        poseStack.translate(0.5D, HUB_HEIGHT, 0.5D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.bladeRotation));
        poseStack.translate(-0.5D, -HUB_HEIGHT, -0.5D);
        SprinklerRenderer.submitBlockModel(poseStack, submitNodeCollector, state.blockState, blades, state.lightCoords);
        poseStack.popPose();
    }

    private static void hub(PoseStack poseStack, float yaw) {
        poseStack.translate(0.5D, HUB_HEIGHT, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.translate(-0.5D, -HUB_HEIGHT, -0.5D);
    }
}
