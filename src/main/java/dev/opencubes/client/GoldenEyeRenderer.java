package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.opencubes.content.goldeneye.GoldenEyeEntity;
import dev.opencubes.registry.OCItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GoldenEyeRenderer extends EntityRenderer<GoldenEyeEntity> {

    private final ItemRenderer itemRenderer;

    public GoldenEyeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(GoldenEyeEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        ItemStack stack = entity.getItem().isEmpty() ? new ItemStack(OCItems.GOLDEN_EYE.get()) : entity.getItem();
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, buffers, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GoldenEyeEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    }
}
