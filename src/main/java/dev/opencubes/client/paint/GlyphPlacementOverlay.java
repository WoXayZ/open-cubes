package dev.opencubes.client.paint;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.OCConstants;
import dev.opencubes.content.paint.GlyphEntity;
import dev.opencubes.content.paint.GlyphItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * While a glyph is held, outlines on the targeted wall the 8×8 pixel square the letter will
 * cover, over a faint pixel grid.
 */
@EventBusSubscriber(modid = OCConstants.MOD_ID, value = Dist.CLIENT)
public final class GlyphPlacementOverlay {

    private static final float EPSILON = 0.002F;

    private GlyphPlacementOverlay() {}

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) {
            return;
        }
        if (!(mc.player.getMainHandItem().getItem() instanceof GlyphItem)
                && !(mc.player.getOffhandItem().getItem() instanceof GlyphItem)) {
            return;
        }
        if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        Direction face = hit.getDirection();
        if (!face.getAxis().isHorizontal()) {
            return;
        }

        BlockPos pos = hit.getBlockPos();
        int[] offsets = GlyphItem.pixelOffsets(pos, face, hit.getLocation());
        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = poseStack.last().pose();
        Face plane = new Face(pos, face);

        for (int i = 0; i <= 16; i += 2) {
            plane.line(consumer, matrix, i, 0, i, 16, 0x30FFFFFF);
            plane.line(consumer, matrix, 0, i, 16, i, 0x30FFFFFF);
        }
        int left = offsets[0] - GlyphEntity.HALF;
        int top = offsets[1] - GlyphEntity.HALF;
        int right = left + GlyphEntity.SIZE;
        int bottom = top + GlyphEntity.SIZE;
        int colour = 0xE0FFD040;
        plane.line(consumer, matrix, left, top, right, top, colour);
        plane.line(consumer, matrix, left, bottom, right, bottom, colour);
        plane.line(consumer, matrix, left, top, left, bottom, colour);
        plane.line(consumer, matrix, right, top, right, bottom, colour);

        poseStack.popPose();
        buffers.endBatch(RenderType.lines());
    }

    /** Maps face pixels (x right, y down, as seen facing the wall) to world coordinates. */
    private record Face(BlockPos pos, Direction face) {

        Vec3 point(float px, float py) {
            Direction right = face.getCounterClockWise();
            double u = px / 16.0D - 0.5D;
            return Vec3.atCenterOf(pos)
                    .relative(face, 0.5D + EPSILON)
                    .relative(right, u)
                    .add(0.0D, 0.5D - py / 16.0D, 0.0D);
        }

        void line(VertexConsumer consumer, Matrix4f matrix, float x1, float y1, float x2, float y2, int argb) {
            Vec3 a = point(x1, y1);
            Vec3 b = point(x2, y2);
            float nx = (float) (b.x - a.x);
            float ny = (float) (b.y - a.y);
            float nz = (float) (b.z - a.z);
            float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            nx /= length;
            ny /= length;
            nz /= length;
            consumer.addVertex(matrix, (float) a.x, (float) a.y, (float) a.z).setColor(argb).setNormal(nx, ny, nz);
            consumer.addVertex(matrix, (float) b.x, (float) b.y, (float) b.z).setColor(argb).setNormal(nx, ny, nz);
        }
    }
}
