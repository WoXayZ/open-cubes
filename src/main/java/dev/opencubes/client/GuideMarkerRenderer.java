package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.config.OCClientConfig;
import dev.opencubes.content.guide.BuildingGuideBlockEntity;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A small translucent cube at each shape marker, morphing across when the shape changes.
 *
 * <p>These are untextured coloured boxes, so they go through the debug quad render type rather
 * than the block translucent one. The latter expects atlas coordinates and smears the whole
 * block atlas over each quad. Markers past the configured range are dropped, which keeps a
 * sixty-four block sphere from queueing thousands of boxes for cells nobody can see.
 */
public class GuideMarkerRenderer<T extends BuildingGuideBlockEntity> implements BlockEntityRenderer<T, GuideMarkerRenderState> {

    private static final float MARKER = 0.35F;
    private static final float HALF = MARKER / 2.0F;
    private static final float ALPHA = 0.45F;

    public GuideMarkerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public GuideMarkerRenderState createRenderState() {
        return new GuideMarkerRenderState();
    }

    @Override
    public void extractRenderState(
            T blockEntity,
            GuideMarkerRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.draw = blockEntity.shouldRenderMarkers();
        if (!state.draw) {
            state.current = List.of();
            state.previous = List.of();
            return;
        }
        int colour = blockEntity.markerColor();
        state.red = ((colour >> 16) & 0xFF) / 255.0F;
        state.green = ((colour >> 8) & 0xFF) / 255.0F;
        state.blue = (colour & 0xFF) / 255.0F;
        state.morph = blockEntity.timeSinceChange();
        state.current = List.copyOf(blockEntity.shapeCoords());
        state.previous = List.copyOf(blockEntity.previousShapeCoords());
    }

    @Override
    public void submit(GuideMarkerRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        if (!state.draw) {
            return;
        }
        double range = OCClientConfig.GUIDE_RENDER_RANGE.get();
        double rangeSquared = range * range;
        Vec3 cameraPos = camera.pos;
        BlockPos origin = state.blockPos;
        List<BlockPos> current = state.current;
        List<BlockPos> previous = state.previous;
        float red = state.red;
        float green = state.green;
        float blue = state.blue;
        float morph = state.morph;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, buffer) -> {
            renderShape(current, buffer, pose, origin, cameraPos, rangeSquared, red, green, blue, ALPHA * morph);
            if (morph < 1.0F) {
                renderShape(previous, buffer, pose, origin, cameraPos, rangeSquared, red, green, blue, ALPHA * (1.0F - morph));
            }
        });
    }

    private static void renderShape(List<BlockPos> coords, VertexConsumer consumer, PoseStack.Pose pose,
                                    BlockPos origin, Vec3 camera, double rangeSquared,
                                    float r, float g, float b, float a) {
        if (a <= 0.01F) {
            return;
        }
        for (BlockPos rel : coords) {
            double cx = origin.getX() + rel.getX() + 0.5D;
            double cy = origin.getY() + rel.getY() + 0.5D;
            double cz = origin.getZ() + rel.getZ() + 0.5D;
            if (camera.distanceToSqr(cx, cy, cz) > rangeSquared) {
                continue;
            }
            float x = rel.getX() + 0.5F;
            float y = rel.getY() + 0.5F;
            float z = rel.getZ() + 0.5F;
            filledBox(consumer, pose, x - HALF, y - HALF, z - HALF, x + HALF, y + HALF, z + HALF, r, g, b, a);
        }
    }

    public static void filledBox(VertexConsumer buffer, PoseStack.Pose pose,
                          float x0, float y0, float z0, float x1, float y1, float z1,
                          float r, float g, float b, float a) {
        quad(buffer, pose, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, r, g, b, a);
        quad(buffer, pose, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, r, g, b, a);
        quad(buffer, pose, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, r, g, b, a);
        quad(buffer, pose, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, a);
        quad(buffer, pose, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, a);
        quad(buffer, pose, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, r, g, b, a);
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose,
                             float x0, float y0, float z0,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float r, float g, float b, float a) {
        buffer.addVertex(pose, x0, y0, z0).setColor(r, g, b, a);
        buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a);
        buffer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a);
        buffer.addVertex(pose, x3, y3, z3).setColor(r, g, b, a);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return OCClientConfig.GUIDE_RENDER_RANGE.get();
    }

    @Override
    public AABB getRenderBoundingBox(T guide) {
        return guide.renderBoundingBox();
    }
}
