package dev.opencubes.content.paint;

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

public class DrawingTableMenu extends AbstractContainerMenu {

    public static final int BUTTON_CUT = 0;
    /** Button ids from here on select {@code id - BUTTON_SELECT} as the table selection. */
    public static final int BUTTON_SELECT = 1;

    private final DrawingTableBlockEntity table;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public DrawingTableMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, getTable(inv, buf.readBlockPos()), new SimpleContainerData(1));
    }

    public DrawingTableMenu(int id, Inventory inv, DrawingTableBlockEntity table) {
        this(id, inv, table, table.getData());
    }

    private DrawingTableMenu(int id, Inventory inv, DrawingTableBlockEntity table, ContainerData data) {
        super(OCMenus.DRAWING_TABLE.get(), id);
        this.table = table;
        this.data = data;
        this.access = ContainerLevelAccess.create(table.getLevel(), table.getBlockPos());

        addSlot(new SlotItemHandler(table.getItems(), 0, 44, 35));
        addSlot(new SlotItemHandler(table.getItems(), 1, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 104 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 162));
        }

        addDataSlots(data);
    }

    private static DrawingTableBlockEntity getTable(Inventory inv, BlockPos pos) {
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        if (be instanceof DrawingTableBlockEntity table) {
            return table;
        }
        throw new IllegalStateException("Drawing table missing at " + pos);
    }

    public DrawingTableBlockEntity table() {
        return table;
    }

    public int selection() {
        return DrawingTableBlockEntity.selectionFromData(data);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (table.getLevel() == null || table.getLevel().isClientSide) {
            return false;
        }
        if (id == BUTTON_CUT) {
            return table.cut();
        }
        int selection = id - BUTTON_SELECT;
        if (selection < 0 || selection >= DrawingTableBlockEntity.SELECTION_COUNT) {
            return false;
        }
        table.select(selection);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < 2) {
                if (!moveItemStackTo(stack, 2, slots.size(), true)) {
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
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, OCBlocks.DRAWING_TABLE.get());
    }
}
