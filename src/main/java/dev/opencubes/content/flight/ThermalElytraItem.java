package dev.opencubes.content.flight;

import dev.opencubes.config.OCCommonConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item.TooltipContext;
import java.util.function.Consumer;

/**
 * Elytra that also samples {@link ThermalField}. Sustained flight without rockets when you
 * can read lift. The glide itself is the vanilla glider component; the thermal nudge is applied
 * from {@link GliderFlightHandler}.
 */
public class ThermalElytraItem extends Item {

    public ThermalElytraItem(Properties properties) {
        super(properties
                .durability(432)
                .stacksTo(1)
                .component(DataComponents.GLIDER, Unit.INSTANCE)
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST)
                        .setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA)
                        .setAsset(EquipmentAssets.ELYTRA)
                        .setDamageOnHurt(false)
                        .build()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        GliderPaint.appendTooltip(stack, tooltip);
    }

    public static void tickFlight(ItemStack stack, Player player) {
        Level level = player.level();
        if (!level.isClientSide()) {
            int interval = player.getAbilities().instabuild ? 0 : 20;
            if (interval > 0 && player.tickCount % interval == 0) {
                stack.hurtAndBreak(1, player, EquipmentSlot.CHEST);
            }
        }
        if (!OCCommonConfig.HANG_GLIDER_THERMAL.get()) {
            return;
        }
        double noise = ThermalField.lift(level, player);
        if (noise == 0.0D) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        double boost = noise * 0.08D;
        player.setDeltaMovement(motion.x, motion.y + boost, motion.z);
        if (boost > 0) {
            player.fallDistance = 0.0F;
        }
        GliderState state = player.getData(dev.opencubes.registry.OCAttachments.GLIDER.get());
        state.setLastVerticalSpeed(player.getDeltaMovement().y);
    }
}
