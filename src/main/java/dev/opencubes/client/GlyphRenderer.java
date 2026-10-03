package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.OCConstants;
import dev.opencubes.content.paint.GlyphEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix4f;

/** Draws the glyph item texture flat against the wall, in front of the entity box. */
public class GlyphRenderer extends EntityRenderer<GlyphEntity> {

    public GlyphRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public static ResourceLocation sprite(char character) {
        return OCConstants.id(String.format("item/glyph/%04x", (int) character));
    }

    @Override
    public void render(GlyphEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        Direction facing = entity.getDirection();
        Direction right = facing.getCounterClockWise();
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(sprite(entity.getCharacter()));
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(InventoryMenu.BLOCK_ATLAS));
        Matrix4f matrix = poseStack.last().pose();

        float half = GlyphEntity.SIZE / 32.0F;
        float front = (float) (GlyphEntity.DEPTH / 2.0D) + 0.001F;
        float nx = facing.getStepX();
        float nz = facing.getStepZ();
        float rx = right.getStepX() * half;
        float rz = right.getStepZ() * half;
        float cx = nx * front;
        float cz = nz * front;

        vertex(consumer, matrix, cx - rx, half, cz - rz, sprite.getU0(), sprite.getV0(), nx, nz, packedLight);
        vertex(consumer, matrix, cx - rx, -half, cz - rz, sprite.getU0(), sprite.getV1(), nx, nz, packedLight);
        vertex(consumer, matrix, cx + rx, -half, cz + rz, sprite.getU1(), sprite.getV1(), nx, nz, packedLight);
        vertex(consumer, matrix, cx + rx, half, cz + rz, sprite.getU1(), sprite.getV0(), nx, nz, packedLight);
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                               float u, float v, float nx, float nz, int light) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(0xFFFFFFFF)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(nx, 0.0F, nz);
    }

    @Override
    public ResourceLocation getTextureLocation(GlyphEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
