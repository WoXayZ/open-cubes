package dev.opencubes.content.luggage;

import dev.opencubes.registry.OCMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class LuggageMenu extends AbstractContainerMenu {

    private final LuggageEntity luggage;
    private final int luggageSlots;

    public LuggageMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, (LuggageEntity) playerInv.player.level().getEntity(buf.readVarInt()));
    }

    public LuggageMenu(int id, Inventory playerInv, LuggageEntity luggage) {
        super(OCMenus.LUGGAGE.get(), id);
        this.luggage = luggage;
        this.luggageSlots = luggage.getInventory().getSlots();

        int rows = luggageSlots / 9;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new SlotItemHandler(luggage.getInventory(), col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        int playerInvY = 18 + rows * 18 + 14;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, playerInvY + 58));
        }

        if (!playerInv.player.level().isClientSide()) {
            luggage.startOpen();
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide() && luggage != null) {
            luggage.stopOpen();
        }
    }

    public int luggageSlots() {
        return luggageSlots;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < luggageSlots) {
                if (!moveItemStackTo(stack, luggageSlots, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, luggageSlots, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return luggage != null && luggage.isAlive() && player.distanceToSqr(luggage) < 64.0D;
    }
}
