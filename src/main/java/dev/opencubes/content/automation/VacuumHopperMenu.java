package dev.opencubes.content.automation;

import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCMenus;
import dev.opencubes.util.SideBitmask;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

/**
 * Ten-slot vacuum inventory. Buttons 0-5 toggle item output faces,
 * 6-11 toggle XP output faces (DOWN, UP, NORTH, SOUTH, WEST, EAST).
 */
public class VacuumHopperMenu extends AbstractContainerMenu {

    public static final int ITEM_SIDE_BUTTONS = 0;
    public static final int XP_SIDE_BUTTONS = 6;

    private final VacuumHopperBlockEntity vacuum;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public VacuumHopperMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, getVacuum(playerInventory, buffer.readBlockPos()),
                new SimpleContainerData(5));
    }

    public VacuumHopperMenu(int containerId, Inventory playerInventory, VacuumHopperBlockEntity vacuum,
                            ContainerData data) {
        super(OCMenus.VACUUM_HOPPER.get(), containerId);
        this.vacuum = vacuum;
        this.access = ContainerLevelAccess.create(vacuum.getLevel(), vacuum.getBlockPos());
        this.data = data;

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 5; col++) {
                addSlot(new SlotItemHandler(vacuum.getItems(), col + row * 5, 44 + col * 18, 20 + row * 18));
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

    private static VacuumHopperBlockEntity getVacuum(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof VacuumHopperBlockEntity vacuum) {
            return vacuum;
        }
        throw new IllegalStateException("Vacuum hopper missing at " + pos);
    }

    public int getFluidAmount() {
        return data.get(0);
    }

    public int getFluidCapacity() {
        return Math.max(1, data.get(1));
    }

    public boolean isItemSide(Direction side) {
        return SideBitmask.has(data.get(2), side);
    }

    public boolean isXpSide(Direction side) {
        return SideBitmask.has(data.get(3), side);
    }

    public boolean isVacuumDisabled() {
        return data.get(4) != 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (vacuum.getLevel() == null || vacuum.getLevel().isClientSide()) {
            return false;
        }
        if (id >= ITEM_SIDE_BUTTONS && id < ITEM_SIDE_BUTTONS + 6) {
            vacuum.toggleItemSide(Direction.from3DDataValue(id - ITEM_SIDE_BUTTONS));
            return true;
        }
        if (id >= XP_SIDE_BUTTONS && id < XP_SIDE_BUTTONS + 6) {
            vacuum.toggleXpSide(Direction.from3DDataValue(id - XP_SIDE_BUTTONS));
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < VacuumHopperBlockEntity.SLOTS) {
                if (!moveItemStackTo(stack, VacuumHopperBlockEntity.SLOTS, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, VacuumHopperBlockEntity.SLOTS, false)) {
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
        return stillValid(access, player, OCBlocks.VACUUM_HOPPER.get());
    }
}
