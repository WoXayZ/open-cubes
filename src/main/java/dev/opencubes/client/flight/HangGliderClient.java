package dev.opencubes.client.flight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.opencubes.OCConstants;
import dev.opencubes.client.PoseRender;
import dev.opencubes.content.flight.GliderPaint;
import dev.opencubes.content.flight.HangGliderItem;
import dev.opencubes.content.flight.HangGliderPhysics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import org.joml.Quaternionf;

@EventBusSubscriber(modid = OCConstants.MOD_ID, value = Dist.CLIENT)
public final class HangGliderClient {

    /** Face-down under the wing. Applied in body yaw space so it is not a world-X roll. */
    private static final float PRONE_PITCH = -90.0F;
    /** Prone model space to camera space: x mirrored, model up (+z) to screen up, model back (+y) to behind. */
    private static final Quaternionf MODEL_TO_VIEW =
            new Quaternionf().rotationAxis(Mth.PI, 0.0F, Mth.SQRT_OF_TWO / 2, Mth.SQRT_OF_TWO / 2);
    /** Pilot eye in prone model pixels with the head raised by {@link GliderArmPose}. */
    private static final float EYE_Y = -4.0F;
    private static final float EYE_Z = 4.0F;

    private HangGliderClient() {}

    private static boolean deployed(AbstractClientPlayer player) {
        return HangGliderItem.isHoldingEngaged(player) && HangGliderPhysics.canDeploy(player);
    }

    @SubscribeEvent
    public static void poseGlider(RenderPlayerEvent.Pre<?> event) {
        AvatarRenderState state = event.getRenderState();
        if (!(Minecraft.getInstance().level.getEntity(state.id) instanceof AbstractClientPlayer player) || !deployed(player)) {
            return;
        }
        state.isCrouching = false;
        HumanoidArm gliderArm = HangGliderItem.engagedHand(player) == InteractionHand.MAIN_HAND
                ? player.getMainArm() : player.getMainArm().getOpposite();
        if (gliderArm == HumanoidArm.RIGHT) {
            state.rightArmPose = GliderArmPose.get();
            state.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        } else {
            state.leftArmPose = GliderArmPose.get();
            state.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        }

        float bodyYaw = Mth.rotLerp(event.getPartialTick(), player.yBodyRotO, player.yBodyRot);
        float yaw = 180.0F - bodyYaw;
        PoseStack pose = event.getPoseStack();
        float mid = player.getBbHeight() * 0.5F;
        pose.translate(0.0F, mid, 0.0F);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.mulPose(Axis.XP.rotationDegrees(PRONE_PITCH));
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.translate(0.0F, -mid, 0.0F);
    }

    /** First person: the wing overhead and both hands on the control bar, level with the horizon. */
    @SubscribeEvent
    public static void renderFirstPerson(RenderHandEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !deployed(player)) {
            return;
        }
        event.setCanceled(true);
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(player.getViewXRot(event.getPartialTick())));
        pose.mulPose(MODEL_TO_VIEW);
        pose.translate(0.0F, -EYE_Y / 16.0F, -EYE_Z / 16.0F);

        SubmitNodeCollector collector = event.getSubmitNodeCollector();
        int light = event.getPackedLight();
        HangGliderRenderer.render(pose, collector, light, GliderPaint.colour(HangGliderItem.engagedStack(player)));
        renderArms(pose, collector, light, player);
        pose.popPose();
    }

    private static void renderArms(PoseStack pose, SubmitNodeCollector collector, int light, LocalPlayer player) {
        if (!(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player) instanceof AvatarRenderer<?> renderer)) {
            return;
        }
        PlayerModel model = (PlayerModel) renderer.getModel();
        Identifier skin = player.getSkin().body().texturePath();
        model.rightArm.resetPose();
        model.leftArm.resetPose();
        GliderArmPose.poseArms(model.rightArm, model.leftArm);
        renderArm(pose, collector, light, skin, model.rightArm, model.rightSleeve, false);
        renderArm(pose, collector, light, skin, model.leftArm, model.leftSleeve, true);
    }

    private static void renderArm(PoseStack pose, SubmitNodeCollector collector, int light, Identifier skin,
                                  ModelPart arm, ModelPart sleeve, boolean translucentSleeve) {
        arm.visible = true;
        collector.submitCustomGeometry(pose, RenderTypes.entitySolid(skin),
                (modelPose, buffer) -> arm.render(PoseRender.stack(modelPose), buffer, light, OverlayTexture.NO_OVERLAY));
        sleeve.loadPose(arm.storePose());
        if (sleeve.visible) {
            collector.submitCustomGeometry(pose,
                    translucentSleeve ? RenderTypes.entityTranslucent(skin) : RenderTypes.entitySolid(skin),
                    (modelPose, buffer) -> sleeve.render(PoseRender.stack(modelPose), buffer, light, OverlayTexture.NO_OVERLAY));
        }
    }
}
