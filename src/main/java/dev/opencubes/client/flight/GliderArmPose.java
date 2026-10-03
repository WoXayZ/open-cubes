package dev.opencubes.client.flight;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Prone pilot gripping the control bar with both hands, head raised to look ahead. Registered as
 * an {@link HumanoidModel.ArmPose} through {@code META-INF/enumextensions.json}, so armour and
 * other layers follow the same arms.
 */
public final class GliderArmPose {

    public static final EnumProxy<HumanoidModel.ArmPose> POSE = new EnumProxy<>(
            HumanoidModel.ArmPose.class, true, (IArmPoseTransformer) GliderArmPose::transform);

    public static final float ARM_PITCH = -2.35F;
    public static final float ARM_SPREAD = 0.22F;
    private static final float SHOULDER_X = 5.0F;
    private static final float SHOULDER_Y = 2.0F;

    /** Left hand grip in model pixels; the right one is mirrored on x. */
    public static final Vector3f GRIP = new Vector3f(1.0F, 9.5F, 0.0F)
            .rotate(new Quaternionf().rotationZYX(ARM_SPREAD, 0.0F, ARM_PITCH))
            .add(SHOULDER_X, SHOULDER_Y, 0.0F);

    private GliderArmPose() {}

    public static HumanoidModel.ArmPose get() {
        return POSE.getValue();
    }

    private static void transform(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        poseArms(model.rightArm, model.leftArm);
        model.head.xRot = Mth.clamp(model.head.xRot, -0.5F, 1.0F) - Mth.HALF_PI;
        model.rightLeg.xRot = 0.0F;
        model.leftLeg.xRot = 0.0F;
        model.rightLeg.zRot = 0.03F;
        model.leftLeg.zRot = -0.03F;
    }

    public static void poseArms(ModelPart right, ModelPart left) {
        right.xRot = ARM_PITCH;
        right.yRot = 0.0F;
        right.zRot = -ARM_SPREAD;
        left.xRot = ARM_PITCH;
        left.yRot = 0.0F;
        left.zRot = ARM_SPREAD;
    }
}
