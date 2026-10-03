package dev.opencubes.client.heightmap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.opencubes.content.heightmap.HeightMapData;
import dev.opencubes.content.heightmap.HeightMapProjectorBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;

/**
 * Holographic height map as coloured boxes (not translucent block-atlas quads —
 * those smear random atlas tiles and look like shredded noise).
 *
 * <p>Map space is 64×64 cells over one block horizontally, up to ~4 blocks tall.
 */
public class HeightMapProjectorRenderer implements BlockEntityRenderer<HeightMapProjectorBlockEntity> {

    private static final float PIXEL = 1.0F / 64.0F;
    private static final float HEIGHT_SCALE = 4.0F / 255.0F;
    private static final float SLAB = PIXEL * 0.85F;

    public HeightMapProjectorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(HeightMapProjectorBlockEntity projector, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int light, int overlay) {
        HeightMapData map = projector.getMap();
        if (map == null || !map.isValid() || map.layers.length == 0) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.01D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(projector.rotation() * 90.0F));
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        // Same family as building-guide markers: position+colour, no atlas UVs.
        VertexConsumer consumer = buffers.getBuffer(RenderType.debugFilledBox());

        for (HeightMapData.LayerData layer : map.layers) {
            float a = Math.max(0.92F, (layer.alpha & 0xFF) / 255.0F);
            for (int z = 0; z < HeightMapData.SIZE; z++) {
                for (int x = 0; x < HeightMapData.SIZE; x++) {
                    int index = z * HeightMapData.SIZE + x;
                    int colorId = layer.colorMap[index] & 0xFF;
                    if (colorId == 0) {
                        continue;
                    }
                    MapColor mapColor = MapColor.byId(colorId);
                    if (mapColor == MapColor.NONE) {
                        continue;
                    }
                    int height = layer.heightMap[index] & 0xFF;
                    int rgb = mapColor.col;
                    float r = ((rgb >> 16) & 0xFF) / 255.0F;
                    float g = ((rgb >> 8) & 0xFF) / 255.0F;
                    float b = (rgb & 0xFF) / 255.0F;
                    float y1 = height * HEIGHT_SCALE;
                    float y0 = Math.max(0.0F, y1 - SLAB);
                    float x0 = x * PIXEL;
                    float z0 = z * PIXEL;
                    LevelRenderer.addChainedFilledBoxVertices(
                            poseStack, consumer,
                            x0, y0, z0,
                            x0 + PIXEL, y1, z0 + PIXEL,
                            r, g, b, a);
                }
            }
        }

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(HeightMapProjectorBlockEntity projector) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(HeightMapProjectorBlockEntity projector) {
        return projector.renderBoundingBox();
    }
}
