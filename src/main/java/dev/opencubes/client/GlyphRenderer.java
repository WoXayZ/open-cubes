package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.OCConstants;
import dev.opencubes.content.paint.GlyphEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

/** Draws the glyph item texture flat against the wall, in front of the entity box. */
public class GlyphRenderer extends EntityRenderer<GlyphEntity, GlyphRenderState> {

    public GlyphRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public static Identifier sprite(char character) {
        return OCConstants.id(String.format("item/glyph/%04x", (int) character));
    }

    @Override
    public GlyphRenderState createRenderState() {
        return new GlyphRenderState();
    }

    @Override
    public void extractRenderState(GlyphEntity entity, GlyphRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.character = entity.getCharacter();
        state.facing = entity.getDirection();
    }

    @Override
    public void submit(GlyphRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        Direction facing = state.facing;
        Direction right = facing.getCounterClockWise();
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager()
                .get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, sprite(state.character)));
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        int light = state.lightCoords;
        float half = GlyphEntity.SIZE / 32.0F;
        float front = (float) (GlyphEntity.DEPTH / 2.0D) + 0.001F;
        float nx = facing.getStepX();
        float nz = facing.getStepZ();
        float rx = right.getStepX() * half;
        float rz = right.getStepZ() * half;
        float cx = nx * front;
        float cz = nz * front;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutoutCull(TextureAtlas.LOCATION_BLOCKS),
                (pose, buffer) -> {
                    vertex(buffer, pose, cx - rx, half, cz - rz, u0, v0, nx, nz, light);
                    vertex(buffer, pose, cx - rx, -half, cz - rz, u0, v1, nx, nz, light);
                    vertex(buffer, pose, cx + rx, -half, cz + rz, u1, v1, nx, nz, light);
                    vertex(buffer, pose, cx + rx, half, cz + rz, u1, v0, nx, nz, light);
                });
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z,
                               float u, float v, float nx, float nz, int light) {
        consumer.addVertex(pose, x, y, z)
                .setColor(0xFFFFFFFF)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, 0.0F, nz);
    }
}
