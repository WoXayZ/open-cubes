package dev.opencubes.client.crane;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.opencubes.content.crane.CraneBackpackItem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

/** Draws the worn crane backpack; the boom turns with the head. */
public class CraneBackpackLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private final ModelPart body;
    private final ModelPart arm;

    public CraneBackpackLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
        this.body = CraneModels.createBackpack().getChild("body");
        this.arm = body.getChild("arm");
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!CraneBackpackItem.isWearing(player)) {
            return;
        }
        body.copyFrom(getParentModel().body);
        arm.yRot = Mth.PI + netHeadYaw * Mth.DEG_TO_RAD;
        body.render(poseStack, buffers.getBuffer(RenderType.entityCutout(CraneModels.BACKPACK_TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY);
    }
}
