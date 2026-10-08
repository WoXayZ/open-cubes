package dev.opencubes.client.heightmap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.opencubes.content.heightmap.HeightMapData;
import dev.opencubes.content.heightmap.HeightMapProjectorBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Coloured height sheet above the projector. Height 255 is about four blocks. */
public class HeightMapProjectorRenderer implements BlockEntityRenderer<HeightMapProjectorBlockEntity, HeightMapProjectorRenderState> {

    private static final float CELL = 1.0F / HeightMapData.SIZE;
    private static final float HEIGHT_SCALE = 1.0F / 64.0F;

    public HeightMapProjectorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public HeightMapProjectorRenderState createRenderState() {
        return new HeightMapProjectorRenderState();
    }

    @Override
    public void extractRenderState(
            HeightMapProjectorBlockEntity blockEntity,
            HeightMapProjectorRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        HeightMapData map = blockEntity.getMap();
        state.map = map != null && map.isValid() && map.layers.length > 0 ? map : null;
        state.rotation = blockEntity.rotation() * 90.0F;
    }

    @Override
    public void submit(HeightMapProjectorRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        HeightMapData map = state.map;
        if (map == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5D, 1.01D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        submitNodeCollector.submitCustomGeometry(poseStack, HeightMapShaders.QUADS, (pose, buffer) -> {
            boolean liquid = false;
            for (HeightMapData.LayerData layer : map.layers) {
                float lift = liquid ? 0.02F : 0.0F;
                liquid = true;
                float[] height = new float[HeightMapData.SIZE * HeightMapData.SIZE];
                boolean[] filled = new boolean[height.length];
                float[] red = new float[height.length];
                float[] green = new float[height.length];
                float[] blue = new float[height.length];
                for (int i = 0; i < height.length; i++) {
                    int colorId = layer.colorMap[i] & 0xFF;
                    if (colorId <= 0 || colorId >= 64) {
                        continue;
                    }
                    MapColor mapColor = MapColor.byId(colorId);
                    if (mapColor == MapColor.NONE) {
                        continue;
                    }
                    filled[i] = true;
                    height[i] = (layer.heightMap[i] & 0xFF) * HEIGHT_SCALE + lift;
                    int rgb = mapColor.col;
                    red[i] = ((rgb >> 16) & 0xFF) / 255.0F;
                    green[i] = ((rgb >> 8) & 0xFF) / 255.0F;
                    blue[i] = (rgb & 0xFF) / 255.0F;
                }
                for (int z = 0; z < HeightMapData.SIZE; z++) {
                    for (int x = 0; x < HeightMapData.SIZE; x++) {
                        int index = z * HeightMapData.SIZE + x;
                        if (!filled[index]) {
                            continue;
                        }
                        float x0 = x * CELL;
                        float z0 = z * CELL;
                        float y = height[index];
                        top(buffer, pose, x0, y, z0, x0 + CELL, z0 + CELL, red[index], green[index], blue[index]);
                        wall(buffer, pose, filled, height, x, z, 1, 0, red[index], green[index], blue[index]);
                        wall(buffer, pose, filled, height, x, z, 0, 1, red[index], green[index], blue[index]);
                    }
                }
            }
        });
        poseStack.popPose();
    }

    private static void top(VertexConsumer buffer, PoseStack.Pose pose,
                            float x0, float y, float z0, float x1, float z1,
                            float r, float g, float b) {
        buffer.addVertex(pose, x0, y, z0).setColor(r, g, b, 1.0F);
        buffer.addVertex(pose, x1, y, z0).setColor(r, g, b, 1.0F);
        buffer.addVertex(pose, x1, y, z1).setColor(r, g, b, 1.0F);
        buffer.addVertex(pose, x0, y, z1).setColor(r, g, b, 1.0F);
    }

    private static void wall(VertexConsumer buffer, PoseStack.Pose pose,
                             boolean[] filled, float[] height, int x, int z, int dx, int dz,
                             float r, float g, float b) {
        int nx = x + dx;
        int nz = z + dz;
        if (nx >= HeightMapData.SIZE || nz >= HeightMapData.SIZE) {
            return;
        }
        int neighbor = nz * HeightMapData.SIZE + nx;
        if (!filled[neighbor]) {
            return;
        }
        int here = z * HeightMapData.SIZE + x;
        float low = height[neighbor];
        float high = height[here];
        if (high <= low + 0.001F) {
            return;
        }
        float shade = 0.72F;
        float x0 = x * CELL;
        float z0 = z * CELL;
        if (dx == 1) {
            float edge = x0 + CELL;
            buffer.addVertex(pose, edge, low, z0).setColor(r * shade, g * shade, b * shade, 1.0F);
            buffer.addVertex(pose, edge, low, z0 + CELL).setColor(r * shade, g * shade, b * shade, 1.0F);
            buffer.addVertex(pose, edge, high, z0 + CELL).setColor(r * shade, g * shade, b * shade, 1.0F);
            buffer.addVertex(pose, edge, high, z0).setColor(r * shade, g * shade, b * shade, 1.0F);
        } else {
            float edge = z0 + CELL;
            buffer.addVertex(pose, x0, low, edge).setColor(r * shade, g * shade, b * shade, 1.0F);
            buffer.addVertex(pose, x0 + CELL, low, edge).setColor(r * shade, g * shade, b * shade, 1.0F);
            buffer.addVertex(pose, x0 + CELL, high, edge).setColor(r * shade, g * shade, b * shade, 1.0F);
            buffer.addVertex(pose, x0, high, edge).setColor(r * shade, g * shade, b * shade, 1.0F);
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(HeightMapProjectorBlockEntity projector) {
        return projector.renderBoundingBox();
    }
}
