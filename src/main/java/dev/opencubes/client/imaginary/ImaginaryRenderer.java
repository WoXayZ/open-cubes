package dev.opencubes.client.imaginary;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.content.imaginary.ImaginaryBlockEntity;
import dev.opencubes.content.imaginary.ImaginaryProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

public class ImaginaryRenderer implements BlockEntityRenderer<ImaginaryBlockEntity> {

    public ImaginaryRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(ImaginaryBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !be.is(ImaginaryProperty.VISIBLE, player)) {
            return;
        }

        int rgb = be.isPencil() ? 0x404040 : (be.colour() == null ? 0xFFFFFF : be.colour());
        float r = ((rgb >> 16) & 0xFF) / 255.0F;
        float g = ((rgb >> 8) & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;
        float a = be.isPencil() ? 0.35F : 0.45F;

        VoxelShape shape = be.voxelShape();
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/misc/white.png")));
        Matrix4f matrix = poseStack.last().pose();
        for (AABB box : shape.toAabbs()) {
            drawBox(consumer, matrix, box, r, g, b, a, packedLight);
        }
    }

    private static void drawBox(VertexConsumer consumer, Matrix4f matrix, AABB box,
                                float r, float g, float b, float a, int light) {
        float x0 = (float) box.minX;
        float y0 = (float) box.minY;
        float z0 = (float) box.minZ;
        float x1 = (float) box.maxX;
        float y1 = (float) box.maxY;
        float z1 = (float) box.maxZ;

        quad(consumer, matrix, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, r, g, b, a, light, Direction.DOWN);
        quad(consumer, matrix, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, r, g, b, a, light, Direction.UP);
        quad(consumer, matrix, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, a, light, Direction.SOUTH);
        quad(consumer, matrix, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, r, g, b, a, light, Direction.NORTH);
        quad(consumer, matrix, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, a, light, Direction.WEST);
        quad(consumer, matrix, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, r, g, b, a, light, Direction.EAST);
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix,
                             float x0, float y0, float z0,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float r, float g, float b, float a, int light, Direction face) {
        float nx = face.getStepX();
        float ny = face.getStepY();
        float nz = face.getStepZ();
        consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a).setUv(0, 0).setOverlay(0)
                .setLight(light).setNormal(nx, ny, nz);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setUv(1, 0).setOverlay(0)
                .setLight(light).setNormal(nx, ny, nz);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setUv(1, 1).setOverlay(0)
                .setLight(light).setNormal(nx, ny, nz);
        consumer.addVertex(matrix, x3, y3, z3).setColor(r, g, b, a).setUv(0, 1).setOverlay(0)
                .setLight(light).setNormal(nx, ny, nz);
    }
}
