package dev.opencubes.content.sprinkler;

import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SprinklerMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;
    private final ContainerData data;

    public SprinklerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, get(playerInventory, buffer.readBlockPos()), new SimpleContainerData(3));
    }

    public SprinklerMenu(int containerId, Inventory playerInventory, SprinklerBlockEntity sprinkler, ContainerData data) {
        super(OCMenus.SPRINKLER.get(), containerId);
        this.access = ContainerLevelAccess.create(sprinkler.getLevel(), sprinkler.getBlockPos());
        this.data = data;

        // Vanilla dispenser coordinates: 3x3 at (62,17), player inv at y=84 / hotbar y=142.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new SlotItemHandler(sprinkler.items(), col + row * 3, 62 + col * 18, 17 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
        addDataSlots(data);
    }

    private static SprinklerBlockEntity get(Inventory inventory, BlockPos pos) {
        BlockEntity be = inventory.player.level().getBlockEntity(pos);
        if (be instanceof SprinklerBlockEntity sprinkler) {
            return sprinkler;
        }
        throw new IllegalStateException("Sprinkler missing at " + pos);
    }

    public int fluidAmount() {
        return data.get(0);
    }

    public int fluidCapacity() {
        return Math.max(1, data.get(1));
    }

    public boolean enabled() {
        return data.get(2) != 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < 9) {
                if (!moveItemStackTo(stack, 9, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, 9, false)) {
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
        return stillValid(access, player, OCBlocks.SPRINKLER.get());
    }
}
