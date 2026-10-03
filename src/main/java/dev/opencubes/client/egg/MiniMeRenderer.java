package dev.opencubes.client.egg;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.opencubes.content.egg.MiniMeEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

public class MiniMeRenderer extends LivingEntityRenderer<MiniMeEntity, PlayerModel<MiniMeEntity>> {

    public MiniMeRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(MiniMeEntity entity) {
        GameProfile profile = entity.ownerProfile();
        if (profile == null || profile.getId() == null) {
            return DefaultPlayerSkin.getDefaultTexture();
        }
        return DefaultPlayerSkin.get(profile.getId()).texture();
    }

    @Override
    public void render(MiniMeEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    protected boolean shouldShowName(MiniMeEntity entity) {
        return entity.hasCustomName();
    }
}
