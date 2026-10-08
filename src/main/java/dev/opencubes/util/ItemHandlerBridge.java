package dev.opencubes.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Both directions between the deprecated slot handler and the 26.1 resource handler.
 * Transactions are applied immediately. Callers that pass a live transaction do not roll back.
 */
public final class ItemHandlerBridge {

    private ItemHandlerBridge() {}

    public static ResourceHandler<ItemResource> asResource(IItemHandler items) {
        return new ResourceView(items);
    }

    public static IItemHandler asSlots(ResourceHandler<ItemResource> resources) {
        return new SlotView(resources);
    }

    private static final class ResourceView implements ResourceHandler<ItemResource> {
        private final IItemHandler items;

        private ResourceView(IItemHandler items) {
            this.items = items;
        }

        @Override
        public int size() {
            return items.getSlots();
        }

        @Override
        public ItemResource getResource(int index) {
            return ItemResource.of(items.getStackInSlot(index));
        }

        @Override
        public long getAmountAsLong(int index) {
            return items.getStackInSlot(index).getCount();
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return items.getSlotLimit(index);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return items.isItemValid(index, resource.toStack());
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (amount <= 0) {
                return 0;
            }
            ItemStack remaining = items.insertItem(index, resource.toStack(amount), false);
            return amount - remaining.getCount();
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (amount <= 0 || resource.isEmpty()) {
                return 0;
            }
            ItemStack extracted = items.extractItem(index, amount, true);
            if (extracted.isEmpty() || !ItemResource.of(extracted).equals(resource)) {
                return 0;
            }
            return items.extractItem(index, amount, false).getCount();
        }
    }

    private static final class SlotView implements IItemHandler {
        private final ResourceHandler<ItemResource> resources;

        private SlotView(ResourceHandler<ItemResource> resources) {
            this.resources = resources;
        }

        @Override
        public int getSlots() {
            return resources.size();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            ItemResource resource = resources.getResource(slot);
            int count = resources.getAmountAsInt(slot);
            return resource.isEmpty() || count <= 0 ? ItemStack.EMPTY : resource.toStack(count);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || !isItemValid(slot, stack)) {
                return stack;
            }
            ItemStack current = getStackInSlot(slot);
            if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) {
                return stack;
            }
            int room = Math.max(0, getSlotLimit(slot) - current.getCount());
            int accepted = Math.min(room, stack.getCount());
            if (!simulate && accepted > 0) {
                resources.insert(slot, ItemResource.of(stack), accepted, null);
            }
            if (accepted >= stack.getCount()) {
                return ItemStack.EMPTY;
            }
            ItemStack remaining = stack.copy();
            remaining.shrink(accepted);
            return remaining;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemResource resource = resources.getResource(slot);
            if (resource.isEmpty() || amount <= 0) {
                return ItemStack.EMPTY;
            }
            int take = Math.min(amount, resources.getAmountAsInt(slot));
            if (take <= 0) {
                return ItemStack.EMPTY;
            }
            if (!simulate) {
                resources.extract(slot, resource, take, null);
            }
            return resource.toStack(take);
        }

        @Override
        public int getSlotLimit(int slot) {
            return resources.getCapacityAsInt(slot, resources.getResource(slot));
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.isEmpty() || resources.isValid(slot, ItemResource.of(stack));
        }
    }
}
