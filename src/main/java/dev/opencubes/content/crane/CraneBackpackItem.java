package dev.opencubes.content.crane;

import dev.opencubes.compat.curios.CuriosCompat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CraneBackpackItem extends Item implements Equipable {

    public CraneBackpackItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.CHEST;
    }

    public static boolean isWearing(LivingEntity entity) {
        if (entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof CraneBackpackItem) {
            return true;
        }
        return CuriosCompat.isLoaded()
                && CuriosCompat.isWearing(entity, stack -> stack.getItem() instanceof CraneBackpackItem);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return swapWithEquipmentSlot(this, level, player, hand);
    }

    public static void onArmorTick(Player player) {
        if (!player.level().isClientSide && isWearing(player)) {
            CraneRegistry.INSTANCE.ensureMagnet(player);
        }
    }
}
