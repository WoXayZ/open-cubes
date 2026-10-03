package dev.opencubes.mixin;

import dev.opencubes.content.sleeping.SleepingBagItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The right-click that starts the sleep also swings the arm, and sleep makes the player immobile
 * so the swing never finishes. Put both arms back along the body for the lying pose.
 */
@Mixin(HumanoidModel.class)
public class HumanoidModelSleepMixin {

    @Shadow
    public ModelPart body;
    @Shadow
    public ModelPart rightArm;
    @Shadow
    public ModelPart leftArm;

    @Inject(method = "setupAnim", at = @At("RETURN"))
    private void opencubes$armsDown(LivingEntity entity, float limbSwing, float limbSwingAmount,
                                    float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!entity.isSleeping()
                || !(entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof SleepingBagItem)) {
            return;
        }
        this.body.yRot = 0.0F;
        rest(this.rightArm, -5.0F);
        rest(this.leftArm, 5.0F);
    }

    private static void rest(ModelPart arm, float x) {
        arm.xRot = 0.0F;
        arm.yRot = 0.0F;
        arm.zRot = 0.0F;
        arm.x = x;
        arm.z = 0.0F;
    }
}
