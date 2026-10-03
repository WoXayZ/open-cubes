package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.OCConstants;
import dev.opencubes.content.paint.CanvasBlockEntity;
import dev.opencubes.content.paint.CanvasFaceData;
import dev.opencubes.content.paint.GlassCanvasBlock;
import dev.opencubes.content.paint.StencilPattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

/**
 * Canvas paint, face by face: the background colour, then every stencilled layer through the
 * holes of its pattern, then the stencil sheet itself while one is laid on the face. Each step
 * sits a little further out than the previous one so they never z-fight.
 *
 * <p>Holes are drawn as one quad per horizontal run of pixels, tinted copies of a white atlas
 * sprite: the block atlas render types read the atlas at the coordinates given, so untextured
 * corners would stretch the whole atlas across the face.
 *
 * <p>Opaque canvas is a full cube, so the BE light sampled at its centre is near-zero. Face light
 * is taken from the neighbouring cell, and the entity cutout pipeline is used so block-chunk shade
 * is not applied a second time on top of the tint.
 */
public class CanvasRenderer implements BlockEntityRenderer<CanvasBlockEntity> {

    private static final float OUTSET = 0.002F;
    private static final float LAYER_STEP = 0.0015F;
    private static final int COVER_COLOUR = 0xFFDCDBDB;

    @FunctionalInterface
    private interface PixelMask {
        boolean test(int x, int y);
    }

    public CanvasRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(CanvasBlockEntity canvas, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int light, int overlay) {
        boolean glass = canvas.getBlockState().getBlock() instanceof GlassCanvasBlock;
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(OCConstants.id("block/canvas"));
        VertexConsumer consumer = buffers.getBuffer(glass
                ? RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS)
                : RenderType.entityCutout(InventoryMenu.BLOCK_ATLAS));
        Painter painter = new Painter(consumer, poseStack.last().pose(), sprite, overlay, glass);

        Level level = canvas.getLevel();
        BlockPos pos = canvas.getBlockPos();
        for (Direction direction : Direction.values()) {
            CanvasFaceData face = canvas.face(direction);
            if (face.isEmpty()) {
                continue;
            }
            painter.face = direction;
            painter.light = faceLight(level, pos, direction, light);
            float depth = OUTSET;
            if (face.background() != 0) {
                painter.rect(0, 0, StencilPattern.SIZE, StencilPattern.SIZE, depth, face.background());
            }
            for (CanvasFaceData.Layer layer : face.layers()) {
                depth += LAYER_STEP;
                StencilPattern pattern = layer.pattern();
                int rotation = layer.rotation();
                painter.mask((x, y) -> pattern.isHole(x, y, rotation), depth, layer.color());
            }
            CanvasFaceData.Cover cover = face.cover();
            if (cover != null) {
                depth += LAYER_STEP;
                painter.mask((x, y) -> !cover.pattern().isHole(x, y, cover.rotation()), depth, COVER_COLOUR);
            }
        }
    }

    private static int faceLight(Level level, BlockPos pos, Direction direction, int fallback) {
        if (level == null) {
            return fallback;
        }
        int outside = LevelRenderer.getLightColor(level, pos.relative(direction));
        // Prefer the brighter of centre vs neighbour so a face buried against a wall is not black.
        int block = Math.max(LightTexture.block(fallback), LightTexture.block(outside));
        int sky = Math.max(LightTexture.sky(fallback), LightTexture.sky(outside));
        return LightTexture.pack(block, sky);
    }

    private static final class Painter {
        private final VertexConsumer consumer;
        private final Matrix4f matrix;
        private final TextureAtlasSprite sprite;
        private final int overlay;
        private final boolean glass;
        private Direction face = Direction.UP;
        private int light;

        private Painter(VertexConsumer consumer, Matrix4f matrix, TextureAtlasSprite sprite,
                        int overlay, boolean glass) {
            this.consumer = consumer;
            this.matrix = matrix;
            this.sprite = sprite;
            this.overlay = overlay;
            this.glass = glass;
        }

        private void mask(PixelMask mask, float depth, int colour) {
            for (int y = 0; y < StencilPattern.SIZE; y++) {
                int x = 0;
                while (x < StencilPattern.SIZE) {
                    if (!mask.test(x, y)) {
                        x++;
                        continue;
                    }
                    int start = x;
                    while (x < StencilPattern.SIZE && mask.test(x, y)) {
                        x++;
                    }
                    rect(start, y, x, y + 1, depth, colour);
                }
            }
        }

        /** Pixel rectangle in face texture space, 0 to 16 left to right and top to bottom. */
        private void rect(int u0, int v0, int u1, int v1, float depth, int colour) {
            float a = ((colour >> 24) & 0xFF) / 255.0F;
            if (a == 0.0F) {
                a = glass ? 0.55F : 1.0F;
            } else if (glass) {
                a = Math.min(a, 0.7F);
            }
            float r = ((colour >> 16) & 0xFF) / 255.0F;
            float g = ((colour >> 8) & 0xFF) / 255.0F;
            float b = (colour & 0xFF) / 255.0F;
            float left = u0 / (float) StencilPattern.SIZE;
            float right = u1 / (float) StencilPattern.SIZE;
            float top = v0 / (float) StencilPattern.SIZE;
            float bottom = v1 / (float) StencilPattern.SIZE;
            // Counter-clockwise seen from outside: top left, bottom left, bottom right, top right.
            vertex(left, top, depth, sprite.getU0(), sprite.getV0(), r, g, b, a);
            vertex(left, bottom, depth, sprite.getU0(), sprite.getV1(), r, g, b, a);
            vertex(right, bottom, depth, sprite.getU1(), sprite.getV1(), r, g, b, a);
            vertex(right, top, depth, sprite.getU1(), sprite.getV0(), r, g, b, a);
        }

        private void vertex(float u, float v, float depth, float tu, float tv,
                            float r, float g, float b, float a) {
            float lo = -depth;
            float hi = 1.0F + depth;
            float x;
            float y;
            float z;
            switch (face) {
                case UP -> { x = u; y = hi; z = v; }
                case DOWN -> { x = u; y = lo; z = 1.0F - v; }
                case NORTH -> { x = 1.0F - u; y = 1.0F - v; z = lo; }
                case SOUTH -> { x = u; y = 1.0F - v; z = hi; }
                case EAST -> { x = hi; y = 1.0F - v; z = 1.0F - u; }
                default -> { x = lo; y = 1.0F - v; z = u; }
            }
            consumer.addVertex(matrix, x, y, z)
                    .setColor(r, g, b, a)
                    .setUv(tu, tv)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(face.getStepX(), face.getStepY(), face.getStepZ());
        }
    }
}
