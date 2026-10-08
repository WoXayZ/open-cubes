package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.opencubes.content.goldeneye.GoldenEyeEntity;
import dev.opencubes.registry.OCItems;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GoldenEyeRenderer extends EntityRenderer<GoldenEyeEntity, GoldenEyeRenderState> {

    private final ItemModelResolver itemModelResolver;

    public GoldenEyeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public GoldenEyeRenderState createRenderState() {
        return new GoldenEyeRenderState();
    }

    @Override
    public void extractRenderState(GoldenEyeEntity entity, GoldenEyeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        ItemStack stack = entity.getItem().isEmpty() ? new ItemStack(OCItems.GOLDEN_EYE.get()) : entity.getItem();
        this.itemModelResolver.updateForNonLiving(state.item, stack, ItemDisplayContext.GROUND, entity);
    }

    @Override
    public void submit(GoldenEyeRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
