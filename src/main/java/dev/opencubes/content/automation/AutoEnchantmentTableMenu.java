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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class AutoEnchantmentTableMenu extends AbstractContainerMenu {

    public static final int BUTTON_CYCLE_LEVEL = 0;
    public static final int BUTTON_POWER_DOWN = 1;
    public static final int BUTTON_POWER_UP = 2;
    public static final int ITEM_INPUT_BUTTONS = 3;
    public static final int ITEM_OUTPUT_BUTTONS = 9;
    public static final int XP_SIDE_BUTTONS = 15;
    public static final int BUTTON_AUTO_PULL = 21;
    public static final int BUTTON_AUTO_PUSH = 22;
    public static final int BUTTON_AUTO_XP = 23;
    /** Client encodes Shift as baseId + this offset (server never sees shift state on button clicks). */
    public static final int SHIFT_OFFSET = 100;

    private final AutoEnchantmentTableBlockEntity table;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public AutoEnchantmentTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, getTable(playerInventory, buffer.readBlockPos()),
                new SimpleContainerData(10));
    }

    public AutoEnchantmentTableMenu(int containerId, Inventory playerInventory,
                                    AutoEnchantmentTableBlockEntity table, ContainerData data) {
        super(OCMenus.AUTO_ENCHANTMENT_TABLE.get(), containerId);
        this.table = table;
        this.access = ContainerLevelAccess.create(table.getLevel(), table.getBlockPos());
        this.data = data;

        addSlot(new SlotItemHandler(table.getItems(), AutoEnchantmentTableBlockEntity.SLOT_TOOL, 27, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.isEnchantable();
            }
        });
        addSlot(new SlotItemHandler(table.getItems(), AutoEnchantmentTableBlockEntity.SLOT_LAPIS, 63, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.LAPIS_LAZULI);
            }
        });
        addSlot(new SlotItemHandler(table.getItems(), AutoEnchantmentTableBlockEntity.SLOT_OUTPUT, 135, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

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

    private static AutoEnchantmentTableBlockEntity getTable(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof AutoEnchantmentTableBlockEntity table) {
            return table;
        }
        throw new IllegalStateException("Auto enchantment table missing at " + pos);
    }

    public int getFluidAmount() {
        return data.get(0);
    }

    public int getFluidCapacity() {
        return Math.max(1, data.get(1));
    }

    public int getSelectedLevel() {
        return data.get(2) + 1;
    }

    public int getPowerLimit() {
        return data.get(3);
    }

    public int getAvailablePower() {
        return data.get(4);
    }

    public boolean isItemInputSide(Direction side) {
        return SideBitmask.has(data.get(5), side);
    }

    public boolean isItemOutputSide(Direction side) {
        return SideBitmask.has(data.get(6), side);
    }

    public boolean isXpSide(Direction side) {
        return SideBitmask.has(data.get(7), side);
    }

    public boolean isAutoPull() {
        return (data.get(8) & 1) != 0;
    }

    public boolean isAutoPush() {
        return (data.get(8) & 2) != 0;
    }

    public boolean isAutoXp() {
        return (data.get(8) & 4) != 0;
    }

    public int getProgress() {
        return data.get(9);
    }

    public int getMaxProgress() {
        return AutoEnchantmentTableBlockEntity.WORK_TICKS;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (table.getLevel() == null || table.getLevel().isClientSide()) {
            return false;
        }
        boolean shift = id >= SHIFT_OFFSET;
        int base = shift ? id - SHIFT_OFFSET : id;
        if (base >= ITEM_INPUT_BUTTONS && base < ITEM_INPUT_BUTTONS + 6) {
            table.toggleItemInputSide(Direction.from3DDataValue(base - ITEM_INPUT_BUTTONS));
            return true;
        }
        if (base >= ITEM_OUTPUT_BUTTONS && base < ITEM_OUTPUT_BUTTONS + 6) {
            table.toggleItemOutputSide(Direction.from3DDataValue(base - ITEM_OUTPUT_BUTTONS));
            return true;
        }
        if (base >= XP_SIDE_BUTTONS && base < XP_SIDE_BUTTONS + 6) {
            table.toggleXpSide(Direction.from3DDataValue(base - XP_SIDE_BUTTONS));
            return true;
        }
        return switch (base) {
            case BUTTON_CYCLE_LEVEL -> {
                table.cycleLevel();
                yield true;
            }
            case BUTTON_POWER_DOWN -> {
                table.adjustPowerLimit(shift ? -10 : -1);
                yield true;
            }
            case BUTTON_POWER_UP -> {
                table.adjustPowerLimit(shift ? 10 : 1);
                yield true;
            }
            case BUTTON_AUTO_PULL -> {
                table.toggleAutoPull();
                yield true;
            }
            case BUTTON_AUTO_PUSH -> {
                table.toggleAutoPush();
                yield true;
            }
            case BUTTON_AUTO_XP -> {
                table.toggleAutoXp();
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            // Slots: 0 tool, 1 lapis, 2 output.
            if (index < 3) {
                if (!moveItemStackTo(stack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.isEnchantable()) {
                if (!moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.is(Items.LAPIS_LAZULI)) {
                if (!moveItemStackTo(stack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
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
        return stillValid(access, player, OCBlocks.AUTO_ENCHANTMENT_TABLE.get());
    }
}
