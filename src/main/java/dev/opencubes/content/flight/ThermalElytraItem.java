package dev.opencubes.content.flight;

import dev.opencubes.config.OCCommonConfig;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
/**
 * Elytra that also samples {@link ThermalField}. Sustained flight without rockets when you
 * can read lift. No Mixin - uses NeoForge {@code canElytraFly}/{@code elytraFlightTick}.
 */
public class ThermalElytraItem extends ElytraItem {

    public ThermalElytraItem(Properties properties) {
        super(properties.durability(432).stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        GliderPaint.appendTooltip(stack, tooltip);
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return ElytraItem.isFlyEnabled(stack);
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        Level level = entity.level();
        if (!level.isClientSide) {
            int interval = entity instanceof Player player && player.getAbilities().instabuild ? 0 : 20;
            if (interval > 0 && flightTicks % interval == 0) {
                stack.hurtAndBreak(1, entity, EquipmentSlot.CHEST);
            }
        }

        if (entity instanceof Player player && OCCommonConfig.HANG_GLIDER_THERMAL.get()) {
            double noise = ThermalField.lift(level, player);
            if (noise != 0.0D) {
                Vec3 motion = player.getDeltaMovement();
                // Gentle thermal nudge on top of vanilla elytra glide.
                double boost = noise * 0.08D;
                player.setDeltaMovement(motion.x, motion.y + boost, motion.z);
                if (boost > 0) {
                    player.fallDistance = 0.0F;
                }
                GliderState state = player.getData(dev.opencubes.registry.OCAttachments.GLIDER.get());
                state.setLastVerticalSpeed(player.getDeltaMovement().y);
            }
        }
        return true;
    }
}
