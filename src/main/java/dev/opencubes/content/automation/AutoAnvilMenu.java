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

public class AutoAnvilMenu extends AbstractContainerMenu {

    public static final int ITEM_INPUT_BUTTONS = 0;
    public static final int ITEM_OUTPUT_BUTTONS = 6;
    public static final int XP_SIDE_BUTTONS = 12;
    public static final int BUTTON_AUTO_PULL = 18;
    public static final int BUTTON_AUTO_PUSH = 19;
    public static final int BUTTON_AUTO_XP = 20;

    private final AutoAnvilBlockEntity anvil;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public AutoAnvilMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, getAnvil(playerInventory, buffer.readBlockPos()),
                new SimpleContainerData(7));
    }

    public AutoAnvilMenu(int containerId, Inventory playerInventory, AutoAnvilBlockEntity anvil,
                         ContainerData data) {
        super(OCMenus.AUTO_ANVIL.get(), containerId);
        this.anvil = anvil;
        this.access = ContainerLevelAccess.create(anvil.getLevel(), anvil.getBlockPos());
        this.data = data;

        addSlot(new SlotItemHandler(anvil.getItems(), AutoAnvilBlockEntity.SLOT_TOOL, 27, 47));
        addSlot(new SlotItemHandler(anvil.getItems(), AutoAnvilBlockEntity.SLOT_MODIFIER, 76, 47));
        addSlot(new SlotItemHandler(anvil.getItems(), AutoAnvilBlockEntity.SLOT_OUTPUT, 134, 47) {
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

    private static AutoAnvilBlockEntity getAnvil(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof AutoAnvilBlockEntity anvil) {
            return anvil;
        }
        throw new IllegalStateException("Auto anvil missing at " + pos);
    }

    public int getFluidAmount() {
        return data.get(0);
    }

    public int getFluidCapacity() {
        return Math.max(1, data.get(1));
    }

    public boolean isItemInputSide(Direction side) {
        return SideBitmask.has(data.get(2), side);
    }

    public boolean isItemOutputSide(Direction side) {
        return SideBitmask.has(data.get(3), side);
    }

    public boolean isXpSide(Direction side) {
        return SideBitmask.has(data.get(4), side);
    }

    public boolean isAutoPull() {
        return (data.get(5) & 1) != 0;
    }

    public boolean isAutoPush() {
        return (data.get(5) & 2) != 0;
    }

    public boolean isAutoXp() {
        return (data.get(5) & 4) != 0;
    }

    public int getProgress() {
        return data.get(6);
    }

    public int getMaxProgress() {
        return AutoAnvilBlockEntity.workTicks();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (anvil.getLevel() == null || anvil.getLevel().isClientSide()) {
            return false;
        }
        if (id >= ITEM_INPUT_BUTTONS && id < ITEM_INPUT_BUTTONS + 6) {
            anvil.toggleItemInputSide(Direction.from3DDataValue(id - ITEM_INPUT_BUTTONS));
            return true;
        }
        if (id >= ITEM_OUTPUT_BUTTONS && id < ITEM_OUTPUT_BUTTONS + 6) {
            anvil.toggleItemOutputSide(Direction.from3DDataValue(id - ITEM_OUTPUT_BUTTONS));
            return true;
        }
        if (id >= XP_SIDE_BUTTONS && id < XP_SIDE_BUTTONS + 6) {
            anvil.toggleXpSide(Direction.from3DDataValue(id - XP_SIDE_BUTTONS));
            return true;
        }
        return switch (id) {
            case BUTTON_AUTO_PULL -> {
                anvil.toggleAutoPull();
                yield true;
            }
            case BUTTON_AUTO_PUSH -> {
                anvil.toggleAutoPush();
                yield true;
            }
            case BUTTON_AUTO_XP -> {
                anvil.toggleAutoXp();
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
            if (index == AutoAnvilBlockEntity.SLOT_OUTPUT) {
                if (!moveItemStackTo(stack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 3) {
                if (!moveItemStackTo(stack, 3, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, 2, false)) {
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
        return stillValid(access, player, OCBlocks.AUTO_ANVIL.get());
    }
}
