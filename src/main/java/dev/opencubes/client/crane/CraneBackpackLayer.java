package dev.opencubes.client.crane;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.opencubes.client.PoseRender;
import dev.opencubes.content.crane.CraneBackpackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/** Draws the worn crane backpack; the boom turns with the head. */
public class CraneBackpackLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private final ModelPart body;
    private final ModelPart arm;

    public CraneBackpackLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
        this.body = CraneModels.createBackpack().getChild("body");
        this.arm = body.getChild("arm");
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light,
                       AvatarRenderState state, float yRot, float xRot) {
        if (!(Minecraft.getInstance().level.getEntity(state.id) instanceof Player player)
                || !CraneBackpackItem.isWearing(player)) {
            return;
        }
        body.loadPose(getParentModel().body.storePose());
        arm.yRot = Mth.PI + getParentModel().head.yRot;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(CraneModels.BACKPACK_TEXTURE),
                (pose, buffer) -> body.render(PoseRender.stack(pose), buffer, light, OverlayTexture.NO_OVERLAY));
    }
}
