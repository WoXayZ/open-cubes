package dev.opencubes.content.devnull;

import dev.opencubes.registry.OCCriteria;
import dev.opencubes.registry.OCItems;
import dev.opencubes.registry.OCMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerCopySlot;

/**
 * Single-slot GUI pinned to a hotbar /dev/null. The handler always reads/writes the live stack in
 * {@code protectedSlot} so data-component updates stick, and {@link ItemHandlerCopySlot} keeps
 * vanilla click logic from mutating an immutable component snapshot.
 */
public class DevNullMenu extends AbstractContainerMenu {

    private final Inventory playerInv;
    private final int protectedSlot;
    private final DevNullItemHandler handler;

    public DevNullMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, buf.readVarInt());
    }

    public DevNullMenu(int id, Inventory playerInv, int protectedSlot) {
        super(OCMenus.DEV_NULL.get(), id);
        this.playerInv = playerInv;
        this.protectedSlot = protectedSlot;
        ItemStack container = playerInv.getItem(protectedSlot);
        if (!container.is(OCItems.DEV_NULL.get())) {
            throw new IllegalStateException("/dev/null missing in slot " + protectedSlot);
        }
        this.handler = new DevNullItemHandler(playerInv, protectedSlot);

        addSlot(new ItemHandlerCopySlot(handler, 0, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return handler.isItemValid(0, stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            final int index = col;
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142) {
                @Override
                public boolean mayPickup(Player player) {
                    return index != protectedSlot && super.mayPickup(player);
                }
            });
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.SWAP && button == protectedSlot) {
            return;
        }
        if (slotId >= 0 && slotId < slots.size()) {
            Slot slot = slots.get(slotId);
            if (slot.container == player.getInventory() && slot.getContainerSlot() == protectedSlot) {
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
        triggerNestAdvancement(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == 0) {
                if (!moveItemStackTo(stack, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        triggerNestAdvancement(player);
        return result;
    }

    private void triggerNestAdvancement(Player player) {
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemStack container = playerInv.getItem(protectedSlot);
        if (!container.is(OCItems.DEV_NULL.get())) {
            return;
        }
        ItemStack contained = DevNullItem.getContained(container);
        if (contained.getItem() instanceof DevNullItem) {
            OCCriteria.STACK_OVERFLOW.get().trigger(serverPlayer);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getInventory().getItem(protectedSlot).is(OCItems.DEV_NULL.get());
    }

    /** Live view of the held /dev/null filter slot backed by its data component. */
    static final class DevNullItemHandler implements IItemHandlerModifiable {
        private final Inventory playerInv;
        private final int protectedSlot;

        DevNullItemHandler(Inventory playerInv, int protectedSlot) {
            this.playerInv = playerInv;
            this.protectedSlot = protectedSlot;
        }

        private ItemStack container() {
            return playerInv.getItem(protectedSlot);
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return DevNullItem.getContained(container()).copy();
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            DevNullItem.setContained(container(), stack == null ? ItemStack.EMPTY : stack);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || !isItemValid(slot, stack)) {
                return stack;
            }
            ItemStack current = DevNullItem.getContained(container());
            if (current.isEmpty()) {
                if (!simulate) {
                    setStackInSlot(0, stack.copy());
                }
                return ItemStack.EMPTY;
            }
            if (!ItemStack.isSameItemSameComponents(current, stack)) {
                return stack;
            }
            int space = current.getMaxStackSize() - current.getCount();
            if (space <= 0) {
                return stack;
            }
            int moved = Math.min(space, stack.getCount());
            if (!simulate) {
                ItemStack grown = current.copy();
                grown.grow(moved);
                setStackInSlot(0, grown);
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(moved);
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack current = DevNullItem.getContained(container());
            if (current.isEmpty() || amount <= 0) {
                return ItemStack.EMPTY;
            }
            int taken = Math.min(amount, current.getCount());
            ItemStack result = current.copy();
            result.setCount(taken);
            if (!simulate) {
                ItemStack left = current.copy();
                left.shrink(taken);
                setStackInSlot(0, left);
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return !stack.isEmpty() && DevNullItem.nest(stack).depth() < DevNullItem.STACK_LIMIT;
        }
    }
}
