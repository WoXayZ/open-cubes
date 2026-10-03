package dev.opencubes.content.crane;

import dev.opencubes.OCConstants;
import dev.opencubes.network.CraneTogglePayload;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class CraneEvents {

    private CraneEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (CraneBackpackItem.isWearing(player)) {
            CraneBackpackItem.onArmorTick(player);
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getSlot() != EquipmentSlot.CHEST) {
            return;
        }
        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();
        boolean had = from.getItem() instanceof CraneBackpackItem;
        boolean has = to.getItem() instanceof CraneBackpackItem;
        if (had && !has) {
            CraneRegistry.INSTANCE.clearPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity().getMainHandItem().getItem() instanceof CraneControlItem)) {
            return;
        }
        event.setCanceled(true);
        // Looking at an entity never fires LeftClickEmpty — send the grab packet here.
        if (event.getEntity().level().isClientSide) {
            PacketDistributor.sendToServer(CraneTogglePayload.INSTANCE);
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getItemStack().getItem() instanceof CraneControlItem) {
            PacketDistributor.sendToServer(CraneTogglePayload.INSTANCE);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getItemStack().getItem() instanceof CraneControlItem) {
            event.setCanceled(true);
            if (event.getLevel().isClientSide) {
                PacketDistributor.sendToServer(CraneTogglePayload.INSTANCE);
            }
        }
    }
}
