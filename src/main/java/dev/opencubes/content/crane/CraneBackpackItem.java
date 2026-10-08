package dev.opencubes.content.crane;

import dev.opencubes.compat.curios.CuriosCompat;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public class CraneBackpackItem extends Item {

    public CraneBackpackItem(Properties properties) {
        super(properties.stacksTo(1).component(
                DataComponents.EQUIPPABLE,
                Equippable.builder(EquipmentSlot.CHEST).build()));
    }

    public static boolean isWearing(LivingEntity entity) {
        if (entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof CraneBackpackItem) {
            return true;
        }
        return CuriosCompat.isLoaded()
                && CuriosCompat.isWearing(entity, stack -> stack.getItem() instanceof CraneBackpackItem);
    }

    public static void onArmorTick(Player player) {
        if (!player.level().isClientSide() && isWearing(player)) {
            CraneRegistry.INSTANCE.ensureMagnet(player);
        }
    }
}
