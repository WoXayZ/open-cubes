package dev.opencubes.client.sky;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.content.sky.SkyBlock;
import dev.opencubes.content.sky.SkyBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Active sky blocks draw their faces with the sky captured after the sky pass, sampled at the
 * fragment's screen position, so each face shows exactly the sky behind it.
 */
public class SkyBlockRenderer implements BlockEntityRenderer<SkyBlockEntity, SkyBlockRenderState> {

    private static final float OVERLAP = 0.002F;

    public SkyBlockRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public SkyBlockRenderState createRenderState() {
        return new SkyBlockRenderState();
    }

    @Override
    public void extractRenderState(
            SkyBlockEntity blockEntity,
            SkyBlockRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        Level level = blockEntity.getLevel();
        state.draw = level != null && SkyBlock.isActive(blockEntity.getBlockState());
        if (!state.draw) {
            return;
        }
        SkyBlockCapture.requestCapture();
        BlockPos pos = blockEntity.getBlockPos();
        for (Direction direction : Direction.values()) {
            state.faces[direction.ordinal()] = !hideFace(level, pos, direction);
        }
    }

    @Override
    public void submit(SkyBlockRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        if (!state.draw || !SkyBlockCapture.isReady()) {
            return;
        }
        boolean[] faces = state.faces.clone();
        submitNodeCollector.submitCustomGeometry(poseStack, SkyRenderTypes.SKY_WINDOW, (pose, buffer) -> {
            for (Direction direction : Direction.values()) {
                if (faces[direction.ordinal()]) {
                    emitFace(buffer, pose, direction);
                }
            }
        });
    }

    private static boolean hideFace(Level level, BlockPos pos, Direction direction) {
        BlockPos neighbourPos = pos.relative(direction);
        BlockState neighbour = level.getBlockState(neighbourPos);
        if (neighbour.getBlock() instanceof SkyBlock && SkyBlock.isActive(neighbour)) {
            return true;
        }
        return neighbour.isSolidRender();
    }

    private static void emitFace(VertexConsumer consumer, PoseStack.Pose pose, Direction direction) {
        // Faces sit on the block boundary and overlap their neighbours a little: every window samples
        // the same screen pixel, so overlaps are invisible while gaps would show as dark seams.
        float e = -OVERLAP;
        float n = 1.0F + OVERLAP;
        switch (direction) {
            case DOWN -> quad(consumer, pose, e, 0, n, e, 0, e, n, 0, e, n, 0, n);
            case UP -> quad(consumer, pose, e, 1, e, e, 1, n, n, 1, n, n, 1, e);
            case NORTH -> quad(consumer, pose, n, e, 0, e, e, 0, e, n, 0, n, n, 0);
            case SOUTH -> quad(consumer, pose, e, e, 1, n, e, 1, n, n, 1, e, n, 1);
            case WEST -> quad(consumer, pose, 0, e, e, 0, e, n, 0, n, n, 0, n, e);
            case EAST -> quad(consumer, pose, 1, e, n, 1, e, e, 1, n, e, 1, n, n);
        }
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3) {
        consumer.addVertex(pose, x0, y0, z0);
        consumer.addVertex(pose, x1, y1, z1);
        consumer.addVertex(pose, x2, y2, z2);
        consumer.addVertex(pose, x3, y3, z3);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
