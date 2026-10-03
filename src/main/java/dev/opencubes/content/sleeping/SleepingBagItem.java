package dev.opencubes.content.sleeping;

import dev.opencubes.registry.OCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Wearable chestpiece that puts the player to sleep in the wild without changing their
 * respawn point ({@link SleepingBagEvents} cancels spawn-set while worn).
 */
public class SleepingBagItem extends Item implements Equipable {

    private final DyeColor colour;

    public SleepingBagItem(Properties properties, DyeColor colour) {
        super(properties.stacksTo(1));
        this.colour = colour;
    }

    public DyeColor colour() {
        return colour;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.CHEST;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(held);
        }
        if (!level.isClientSide) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST).copy();
            ItemStack bag = held.copy();
            bag.set(OCDataComponents.SLEEPING_BAG_SLOT.get(), player.getInventory().selected);
            bag.remove(OCDataComponents.SLEEPING_BAG_ASLEEP.get());
            player.setItemSlot(EquipmentSlot.CHEST, bag);
            if (!chest.isEmpty()) {
                return InteractionResultHolder.success(chest);
            }
            held.setCount(0);
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // Armor tick path: only when worn on the chest.
    }

    public static void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || level.isClientSide) {
            return;
        }
        boolean wasSleeping = Boolean.TRUE.equals(stack.get(OCDataComponents.SLEEPING_BAG_ASLEEP.get()));
        if (player.isSleeping()) {
            return;
        }
        if (wasSleeping) {
            stack.remove(OCDataComponents.SLEEPING_BAG_ASLEEP.get());
            unequip(serverPlayer, stack);
        } else if (!trySleep(level, serverPlayer)) {
            unequip(serverPlayer, stack);
        } else {
            stack.set(OCDataComponents.SLEEPING_BAG_ASLEEP.get(), true);
        }
    }

    private static boolean trySleep(Level level, ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        if (!isSafe(level, pos) || !isSolidEnough(level, pos.below())) {
            player.sendSystemMessage(Component.translatable("opencubes.misc.oh_no_ground"));
            return false;
        }

        // Remember spawn so we can restore if anything still tries to overwrite it.
        player.startSleepInBed(pos).ifLeft(problem -> {
            if (problem != null) {
                player.displayClientMessage(problem.getMessage(), true);
            }
        });
        return player.isSleeping();
    }

    private static boolean isSafe(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos, CollisionContext.empty()).isEmpty();
    }

    private static boolean isSolidEnough(Level level, BlockPos pos) {
        return level.getBlockState(pos).isSolidRender(level, pos)
                || level.getBlockState(pos).isCollisionShapeFullBlock(level, pos);
    }

    public static void unequip(ServerPlayer player, ItemStack bag) {
        Integer slot = bag.get(OCDataComponents.SLEEPING_BAG_SLOT.get());
        bag.remove(OCDataComponents.SLEEPING_BAG_ASLEEP.get());
        bag.remove(OCDataComponents.SLEEPING_BAG_SLOT.get());
        player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        if (slot != null && slot >= 0 && slot < 9 && player.getInventory().getItem(slot).isEmpty()) {
            player.getInventory().setItem(slot, bag);
        } else if (!player.getInventory().add(bag)) {
            player.drop(bag, false);
        }
    }
}
