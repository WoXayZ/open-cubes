package dev.opencubes.client.imaginary;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.content.imaginary.ImaginaryBlockEntity;
import dev.opencubes.content.imaginary.ImaginaryProperty;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import dev.opencubes.OCConstants;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ImaginaryRenderer implements BlockEntityRenderer<ImaginaryBlockEntity, ImaginaryRenderState> {

    /** White strokes. Pencil is tinted grey, crayon uses its colour. */
    private static final Identifier SKETCH = OCConstants.id("textures/block/pencil_block.png");

    public ImaginaryRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public ImaginaryRenderState createRenderState() {
        return new ImaginaryRenderState();
    }

    @Override
    public void extractRenderState(
            ImaginaryBlockEntity blockEntity,
            ImaginaryRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.boxes.clear();
        LocalPlayer player = Minecraft.getInstance().player;
        state.visible = player != null && blockEntity.is(ImaginaryProperty.VISIBLE, player);
        if (!state.visible) {
            return;
        }
        int rgb = blockEntity.isPencil() ? 0x2A2A2A : (blockEntity.colour() == null ? 0xFFFFFF : blockEntity.colour());
        state.red = ((rgb >> 16) & 0xFF) / 255.0F;
        state.green = ((rgb >> 8) & 0xFF) / 255.0F;
        state.blue = (rgb & 0xFF) / 255.0F;
        state.alpha = 1.0F;
        state.boxes.addAll(blockEntity.voxelShape().toAabbs());
    }

    @Override
    public void submit(ImaginaryRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        if (!state.visible || state.boxes.isEmpty()) {
            return;
        }
        List<AABB> boxes = List.copyOf(state.boxes);
        float red = state.red;
        float green = state.green;
        float blue = state.blue;
        float alpha = state.alpha;
        int light = state.lightCoords;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(SKETCH), (pose, buffer) -> {
            for (AABB box : boxes) {
                drawBox(buffer, pose, box, red, green, blue, alpha, light);
            }
        });
    }

    private static void drawBox(VertexConsumer consumer, PoseStack.Pose pose, AABB box,
                                float r, float g, float b, float a, int light) {
        float x0 = (float) box.minX;
        float y0 = (float) box.minY;
        float z0 = (float) box.minZ;
        float x1 = (float) box.maxX;
        float y1 = (float) box.maxY;
        float z1 = (float) box.maxZ;

        quad(consumer, pose, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, r, g, b, a, light, Direction.DOWN);
        quad(consumer, pose, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, r, g, b, a, light, Direction.UP);
        quad(consumer, pose, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, a, light, Direction.SOUTH);
        quad(consumer, pose, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, r, g, b, a, light, Direction.NORTH);
        quad(consumer, pose, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, a, light, Direction.WEST);
        quad(consumer, pose, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, r, g, b, a, light, Direction.EAST);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x0, float y0, float z0,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float r, float g, float b, float a, int light, Direction face) {
        float nx = face.getStepX();
        float ny = face.getStepY();
        float nz = face.getStepZ();
        consumer.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x3, y3, z3).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, nx, ny, nz);
    }
}
