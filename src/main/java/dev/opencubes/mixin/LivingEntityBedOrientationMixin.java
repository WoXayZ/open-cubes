package dev.opencubes.mixin;

import dev.opencubes.content.sleeping.SleepingBagItem;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A sleeping bag has no bed block, so vanilla reports {@link Direction#UP}. The sleep camera and
 * the lying-down rotation both follow that and end up looking at the player from the wrong end,
 * which puts an arm in front of the view. Use the direction the player was facing instead.
 */
@Mixin(LivingEntity.class)
public class LivingEntityBedOrientationMixin {

    @Inject(method = "getBedOrientation", at = @At("RETURN"), cancellable = true)
    private void opencubes$sleepingBagFacing(CallbackInfoReturnable<Direction> cir) {
        if (!((Object) this instanceof Player player) || !player.isSleeping()) {
            return;
        }
        if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof SleepingBagItem) {
            cir.setReturnValue(player.getDirection());
        }
    }
}
