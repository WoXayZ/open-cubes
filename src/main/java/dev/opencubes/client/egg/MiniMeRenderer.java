package dev.opencubes.client.egg;

import com.mojang.authlib.GameProfile;
import dev.opencubes.content.egg.MiniMeEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;

public class MiniMeRenderer extends LivingEntityRenderer<MiniMeEntity, AvatarRenderState, PlayerModel> {

    public MiniMeRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.3F);
    }

    @Override
    public AvatarRenderState createRenderState() {
        return new AvatarRenderState();
    }

    @Override
    public void extractRenderState(MiniMeEntity entity, AvatarRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, this.itemModelResolver);
        GameProfile profile = entity.ownerProfile();
        state.skin = profile == null ? DefaultPlayerSkin.getDefaultSkin() : DefaultPlayerSkin.get(profile.id());
        // state.scale is what the renderer multiplies the pose by. The scale() hook did not.
        state.scale = 0.36F;
    }

    @Override
    public Identifier getTextureLocation(AvatarRenderState state) {
        return state.skin.body().texturePath();
    }

    @Override
    protected boolean shouldShowName(MiniMeEntity entity, double distanceToCameraSq) {
        return entity.hasCustomName();
    }
}
