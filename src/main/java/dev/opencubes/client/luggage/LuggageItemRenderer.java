package dev.opencubes.client.luggage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.opencubes.OCConstants;
import dev.opencubes.registry.OCDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Inventory and hand model: the luggage entity mesh, not the flat icon. */
public final class LuggageItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final ResourceLocation NORMAL = OCConstants.id("textures/entity/luggage_normal_body.png");
    private static final ResourceLocation SPECIAL = OCConstants.id("textures/entity/luggage_special.png");

    private LuggageModel normal;
    private LuggageModel special;

    public LuggageItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource buffers, int light, int overlay) {
        if (normal == null) {
            var models = Minecraft.getInstance().getEntityModels();
            normal = new LuggageModel(models.bakeLayer(LuggageModel.NORMAL_LAYER));
            special = new LuggageModel(models.bakeLayer(LuggageModel.SPECIAL_LAYER));
        }
        boolean upgraded = Boolean.TRUE.equals(stack.get(OCDataComponents.LUGGAGE_SPECIAL.get()));
        LuggageModel model = upgraded ? special : normal;
        ResourceLocation texture = upgraded ? SPECIAL : NORMAL;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.35F, 0.5F);
        float scale = context == ItemDisplayContext.GUI ? 0.62F : 0.5F;
        poseStack.scale(scale, scale, scale);
        if (context == ItemDisplayContext.GUI) {
            poseStack.mulPose(Axis.YP.rotationDegrees(135.0F));
        }
        // Same feet convention as a living renderer: model Y grows down after the flip.
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        model.renderToBuffer(poseStack,
                ItemRenderer.getFoilBufferDirect(buffers, RenderType.entityCutoutNoCull(texture), false, stack.hasFoil()),
                light, overlay, 0xFFFFFFFF);
        poseStack.popPose();
    }
}
