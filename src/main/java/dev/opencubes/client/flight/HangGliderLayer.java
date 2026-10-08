package dev.opencubes.client.flight;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.opencubes.content.flight.GliderPaint;
import dev.opencubes.content.flight.HangGliderItem;
import dev.opencubes.content.flight.HangGliderPhysics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.player.Player;

/** Deployed glider above a prone pilot; see {@link HangGliderClient} for the body pitch. */
public class HangGliderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    public HangGliderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light,
                       AvatarRenderState state, float yRot, float xRot) {
        if (!(Minecraft.getInstance().level.getEntity(state.id) instanceof Player player)) {
            return;
        }
        if (HangGliderItem.isHoldingEngaged(player) && HangGliderPhysics.canDeploy(player)) {
            HangGliderRenderer.render(poseStack, submitNodeCollector, light,
                    GliderPaint.colour(HangGliderItem.engagedStack(player)));
        }
    }
}
