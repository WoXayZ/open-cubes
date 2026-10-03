package dev.opencubes.client.flight;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.opencubes.content.flight.GliderPaint;
import dev.opencubes.content.flight.HangGliderItem;
import dev.opencubes.content.flight.HangGliderPhysics;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

/** Deployed glider above a prone pilot; see {@link HangGliderClient} for the body pitch. */
public class HangGliderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public HangGliderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
                           EntityModelSet models) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (HangGliderItem.isHoldingEngaged(player) && HangGliderPhysics.canDeploy(player)) {
            HangGliderRenderer.render(poseStack, buffer, packedLight,
                    GliderPaint.colour(HangGliderItem.engagedStack(player)));
        }
    }
}
