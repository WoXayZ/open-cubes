package dev.opencubes.client.heightmap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.opencubes.content.heightmap.CartographerEntity;
import dev.opencubes.registry.OCItems;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Floating item model for the cartographer drone. */
public class CartographerRenderer extends EntityRenderer<CartographerEntity, CartographerRenderState> {

    private final ItemModelResolver itemModelResolver;
    private ItemStack stack;

    public CartographerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
        this.shadowRadius = 0.2F;
    }

    @Override
    public CartographerRenderState createRenderState() {
        return new CartographerRenderState();
    }

    @Override
    public void extractRenderState(CartographerEntity entity, CartographerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yaw = entity.getYRot(partialTicks);
        if (this.stack == null) {
            this.stack = new ItemStack(OCItems.CARTOGRAPHER.get());
        }
        this.itemModelResolver.updateForNonLiving(state.item, this.stack, ItemDisplayContext.GROUND, entity);
    }

    @Override
    public void submit(CartographerRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yaw));
        poseStack.scale(0.8F, 0.8F, 0.8F);
        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
