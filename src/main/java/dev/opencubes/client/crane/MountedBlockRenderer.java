package dev.opencubes.client.crane;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.opencubes.content.crane.MountedBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/** Renders the carried block state as a block model. */
public class MountedBlockRenderer extends EntityRenderer<MountedBlockEntity, MountedBlockRenderState> {

    public MountedBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public MountedBlockRenderState createRenderState() {
        return new MountedBlockRenderState();
    }

    @Override
    public void extractRenderState(MountedBlockEntity entity, MountedBlockRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        BlockState blockState = entity.getCarried();
        state.draw = blockState.getRenderShape() == RenderShape.MODEL;
        state.movingBlock.blockState = blockState;
        BlockPos pos = entity.blockPosition();
        state.movingBlock.blockPos = pos;
        state.movingBlock.randomSeedPos = pos;
        if (entity.level() instanceof ClientLevel clientLevel) {
            state.movingBlock.biome = clientLevel.getBiome(pos);
            state.movingBlock.cardinalLighting = clientLevel.cardinalLighting();
            state.movingBlock.lightEngine = clientLevel.getLightEngine();
        }
    }

    @Override
    public void submit(MountedBlockRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        if (state.draw) {
            poseStack.pushPose();
            poseStack.translate(-0.5D, 0.0D, -0.5D);
            submitNodeCollector.submitMovingBlock(poseStack, state.movingBlock);
            poseStack.popPose();
        }
        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
