package dev.opencubes.client.village;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.content.village.VillageHighlighterBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class VillageHighlighterRenderer implements BlockEntityRenderer<VillageHighlighterBlockEntity, VillageHighlighterRenderState> {

    public VillageHighlighterRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public VillageHighlighterRenderState createRenderState() {
        return new VillageHighlighterRenderState();
    }

    @Override
    public void extractRenderState(
            VillageHighlighterBlockEntity blockEntity,
            VillageHighlighterRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.draw = blockEntity.shouldRenderMarkers();
        state.box = blockEntity.renderBox();
    }

    @Override
    public void submit(VillageHighlighterRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        if (!state.draw) {
            return;
        }
        AABB box = state.box;
        float width = lineWidth();
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) ->
                lineBox(buffer, pose, box, 0.2F, 0.9F, 0.3F, 0.8F, width));
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    public static float lineWidth() {
        float width = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;
        return width > 0.0F ? width : 2.0F;
    }

    static void lineBox(VertexConsumer buffer, PoseStack.Pose pose, AABB box,
                        float r, float g, float b, float a, float width) {
        float x0 = (float) box.minX;
        float y0 = (float) box.minY;
        float z0 = (float) box.minZ;
        float x1 = (float) box.maxX;
        float y1 = (float) box.maxY;
        float z1 = (float) box.maxZ;
        line(buffer, pose, x0, y0, z0, x1, y0, z0, r, g, b, a, width);
        line(buffer, pose, x1, y0, z0, x1, y0, z1, r, g, b, a, width);
        line(buffer, pose, x1, y0, z1, x0, y0, z1, r, g, b, a, width);
        line(buffer, pose, x0, y0, z1, x0, y0, z0, r, g, b, a, width);
        line(buffer, pose, x0, y1, z0, x1, y1, z0, r, g, b, a, width);
        line(buffer, pose, x1, y1, z0, x1, y1, z1, r, g, b, a, width);
        line(buffer, pose, x1, y1, z1, x0, y1, z1, r, g, b, a, width);
        line(buffer, pose, x0, y1, z1, x0, y1, z0, r, g, b, a, width);
        line(buffer, pose, x0, y0, z0, x0, y1, z0, r, g, b, a, width);
        line(buffer, pose, x1, y0, z0, x1, y1, z0, r, g, b, a, width);
        line(buffer, pose, x1, y0, z1, x1, y1, z1, r, g, b, a, width);
        line(buffer, pose, x0, y0, z1, x0, y1, z1, r, g, b, a, width);
    }

    public static void line(VertexConsumer buffer, PoseStack.Pose pose,
                     float x1, float y1, float z1, float x2, float y2, float z2,
                     float r, float g, float b, float a, float width) {
        float nx = x2 - x1;
        float ny = y2 - y1;
        float nz = z2 - z1;
        float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        if (length < 1.0E-4F) {
            nx = 0.0F;
            ny = 1.0F;
            nz = 0.0F;
        } else {
            nx /= length;
            ny /= length;
            nz /= length;
        }
        int color = ARGB.colorFromFloat(a, r, g, b);
        buffer.addVertex(pose, x1, y1, z1).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(width);
        buffer.addVertex(pose, x2, y2, z2).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(width);
    }
}
