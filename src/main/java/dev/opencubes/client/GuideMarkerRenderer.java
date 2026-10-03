package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.config.OCClientConfig;
import dev.opencubes.content.guide.BuildingGuideBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A small translucent cube at each shape marker, morphing across when the shape changes.
 *
 * <p>These are untextured coloured boxes, so they go through the debug box render type rather
 * than the block translucent one - the latter expects atlas coordinates and smears the whole
 * block atlas over each quad. Markers past the configured range are dropped, which keeps a
 * sixty-four block sphere from queueing thousands of boxes for cells nobody can see.
 */
public class GuideMarkerRenderer<T extends BuildingGuideBlockEntity> implements BlockEntityRenderer<T> {

    private static final float MARKER = 0.35F;
    private static final float HALF = MARKER / 2.0F;
    private static final float ALPHA = 0.45F;

    public GuideMarkerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(T guide, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int light, int overlay) {
        if (!guide.shouldRenderMarkers()) {
            return;
        }

        int colour = guide.markerColor();
        float r = ((colour >> 16) & 0xFF) / 255.0F;
        float g = ((colour >> 8) & 0xFF) / 255.0F;
        float b = (colour & 0xFF) / 255.0F;
        float morph = guide.timeSinceChange();

        double range = OCClientConfig.GUIDE_RENDER_RANGE.get();
        double rangeSquared = range * range;
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        BlockPos origin = guide.getBlockPos();

        VertexConsumer consumer = buffers.getBuffer(RenderType.debugFilledBox());
        renderShape(guide.shapeCoords(), poseStack, consumer, origin, camera, rangeSquared,
                r, g, b, ALPHA * morph);
        if (morph < 1.0F) {
            renderShape(guide.previousShapeCoords(), poseStack, consumer, origin, camera, rangeSquared,
                    r, g, b, ALPHA * (1.0F - morph));
        }
    }

    private static void renderShape(Iterable<BlockPos> coords, PoseStack poseStack, VertexConsumer consumer,
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
            LevelRenderer.addChainedFilledBoxVertices(poseStack, consumer,
                    x - HALF, y - HALF, z - HALF, x + HALF, y + HALF, z + HALF,
                    r, g, b, a);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(T guide) {
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
