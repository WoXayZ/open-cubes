package dev.opencubes.client.flight;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.opencubes.OCConstants;
import dev.opencubes.client.PoseRender;
import dev.opencubes.content.flight.GliderPaint;
import dev.opencubes.content.flight.ThermalElytraItem;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Vanilla wings render through the equipment asset and cannot tint. This layer draws the same
 * wing model for the thermal elytra, with the greyscale canvas multiplied by the paint colour.
 * A skin elytra or cape still takes over, untinted.
 */
public class ThermalElytraLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private static final Identifier TEXTURE = OCConstants.id("textures/entity/thermal_elytra.png");

    private final ElytraModel wings;

    public ThermalElytraLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.wings = new ElytraModel(modelSet.bakeLayer(ModelLayers.ELYTRA));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light,
                       AvatarRenderState state, float yRot, float xRot) {
        ItemStack stack = state.chestEquipment;
        if (!(stack.getItem() instanceof ThermalElytraItem)) {
            return;
        }
        Identifier texture = skinTexture(state);
        int colour = 0xFFFFFFFF;
        if (texture == null) {
            texture = TEXTURE;
            colour = 0xFF000000 | GliderPaint.colour(stack);
        }
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);
        wings.setupAnim(state);
        Identifier wingTexture = texture;
        int tint = colour;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.armorCutoutNoCull(wingTexture),
                (pose, buffer) -> wings.renderToBuffer(PoseRender.stack(pose), buffer, light, OverlayTexture.NO_OVERLAY, tint));
        poseStack.popPose();
    }

    private static @Nullable Identifier skinTexture(AvatarRenderState state) {
        if (state.skin.elytra() != null) {
            return state.skin.elytra().texturePath();
        }
        if (state.showCape && state.skin.cape() != null) {
            return state.skin.cape().texturePath();
        }
        return null;
    }
}
