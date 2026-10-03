package dev.opencubes.client.flight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.OCConstants;
import dev.opencubes.content.flight.GliderPaint;
import dev.opencubes.content.flight.ThermalElytraItem;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;

/**
 * Vanilla {@link ElytraLayer} only renders for {@code Items.ELYTRA} and cannot tint. This variant
 * renders the same wing model for the Thermal Elytra, with our greyscale canvas multiplied by the
 * paint colour. Capes and skin elytras still take over, untinted, like on vanilla elytras.
 */
public class ThermalElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends ElytraLayer<T, M> {

    private static final ResourceLocation TEXTURE = OCConstants.id("textures/entity/thermal_elytra.png");

    private final ElytraModel<T> wings;

    public ThermalElytraLayer(RenderLayerParent<T, M> renderer, EntityModelSet modelSet) {
        super(renderer, modelSet);
        this.wings = new ElytraModel<>(modelSet.bakeLayer(ModelLayers.ELYTRA));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (!shouldRender(stack, entity)) {
            return;
        }
        ResourceLocation texture = skinTexture(entity);
        int colour = 0xFFFFFFFF;
        if (texture == null) {
            texture = TEXTURE;
            colour = 0xFF000000 | GliderPaint.colour(stack);
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);
        getParentModel().copyPropertiesTo(wings);
        wings.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(texture),
                stack.hasFoil());
        wings.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, colour);
        poseStack.popPose();
    }

    private static ResourceLocation skinTexture(LivingEntity entity) {
        if (entity instanceof AbstractClientPlayer player) {
            PlayerSkin skin = player.getSkin();
            if (skin.elytraTexture() != null) {
                return skin.elytraTexture();
            }
            if (skin.capeTexture() != null && player.isModelPartShown(PlayerModelPart.CAPE)) {
                return skin.capeTexture();
            }
        }
        return null;
    }

    @Override
    public boolean shouldRender(ItemStack stack, T entity) {
        return stack.getItem() instanceof ThermalElytraItem;
    }

    @Override
    public ResourceLocation getElytraTexture(ItemStack stack, T entity) {
        return TEXTURE;
    }
}
