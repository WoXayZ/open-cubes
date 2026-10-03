package dev.opencubes.client.village;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.content.village.VillageHighlighterBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

public class VillageHighlighterRenderer implements BlockEntityRenderer<VillageHighlighterBlockEntity> {

    public VillageHighlighterRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(VillageHighlighterBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!be.shouldRenderMarkers()) {
            return;
        }
        AABB box = be.renderBox();
        VertexConsumer consumer = buffers.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(poseStack, consumer, box, 0.2F, 0.9F, 0.3F, 0.8F);
    }

    @Override
    public boolean shouldRenderOffScreen(VillageHighlighterBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
