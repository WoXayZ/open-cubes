package dev.opencubes.content.heightmap;

import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class HeightMapProjectorMenu extends AbstractContainerMenu {

    public static final int BUTTON_ROTATE_LEFT = 0;
    public static final int BUTTON_ROTATE_RIGHT = 1;

    private final HeightMapProjectorBlockEntity projector;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public HeightMapProjectorMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (HeightMapProjectorBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public HeightMapProjectorMenu(int id, Inventory inv, HeightMapProjectorBlockEntity projector) {
        super(OCMenus.HEIGHT_MAP_PROJECTOR.get(), id);
        this.projector = projector;
        this.access = ContainerLevelAccess.create(projector.getLevel(), projector.getBlockPos());
        this.data = new SimpleContainerData(2) {
            @Override
            public int get(int index) {
                return index == 0 ? projector.mapId() : projector.rotation();
            }

            @Override
            public void set(int index, int value) {}
        };

        addSlot(new SlotItemHandler(projector.getItems(), 0, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof HeightMapItem || stack.getItem() instanceof EmptyMapItem;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
        addDataSlots(data);
    }

    public int mapId() {
        return data.get(0);
    }

    public int rotation() {
        return data.get(1);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_ROTATE_LEFT) {
            projector.rotate(-1);
            return true;
        }
        if (id == BUTTON_ROTATE_RIGHT) {
            projector.rotate(1);
            return true;
        }
        return false;
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
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, OCBlocks.HEIGHT_MAP_PROJECTOR.get());
    }
}
