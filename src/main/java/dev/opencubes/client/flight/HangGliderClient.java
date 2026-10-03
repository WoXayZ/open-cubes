package dev.opencubes.client.flight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.opencubes.OCConstants;
import dev.opencubes.content.flight.GliderPaint;
import dev.opencubes.content.flight.HangGliderItem;
import dev.opencubes.content.flight.HangGliderPhysics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
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
    public static void poseGlider(RenderPlayerEvent.Pre event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player) || !deployed(player)) {
            return;
        }
        if (!(event.getRenderer() instanceof PlayerRenderer renderer)) {
            return;
        }

        PlayerModel<AbstractClientPlayer> model = renderer.getModel();
        model.crouching = false;
        HumanoidArm gliderArm = HangGliderItem.engagedHand(player) == InteractionHand.MAIN_HAND
                ? player.getMainArm() : player.getMainArm().getOpposite();
        if (gliderArm == HumanoidArm.RIGHT) {
            model.rightArmPose = GliderArmPose.get();
            model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        } else {
            model.leftArmPose = GliderArmPose.get();
            model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
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

        MultiBufferSource buffer = event.getMultiBufferSource();
        int light = event.getPackedLight();
        HangGliderRenderer.render(pose, buffer, light, GliderPaint.colour(HangGliderItem.engagedStack(player)));
        renderArms(pose, buffer, light, player);
        pose.popPose();
    }

    private static void renderArms(PoseStack pose, MultiBufferSource buffer, int light, LocalPlayer player) {
        if (!(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player)
                instanceof PlayerRenderer renderer)) {
            return;
        }
        PlayerModel<AbstractClientPlayer> model = renderer.getModel();
        ResourceLocation skin = player.getSkin().texture();
        model.rightArm.resetPose();
        model.leftArm.resetPose();
        GliderArmPose.poseArms(model.rightArm, model.leftArm);
        renderArm(pose, buffer, light, skin, model.rightArm, model.rightSleeve);
        renderArm(pose, buffer, light, skin, model.leftArm, model.leftSleeve);
    }

    private static void renderArm(PoseStack pose, MultiBufferSource buffer, int light, ResourceLocation skin,
                                  ModelPart arm, ModelPart sleeve) {
        arm.visible = true;
        arm.render(pose, buffer.getBuffer(RenderType.entitySolid(skin)), light, OverlayTexture.NO_OVERLAY);
        sleeve.copyFrom(arm);
        if (sleeve.visible) {
            sleeve.render(pose, buffer.getBuffer(RenderType.entityTranslucent(skin)), light,
                    OverlayTexture.NO_OVERLAY);
        }
    }
}
