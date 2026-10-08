package dev.opencubes.content.paint;

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
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.SlotItemHandler;

public class PaintMixerMenu extends AbstractContainerMenu {

    /** Kept for compatibility; Mix is started via {@code SetPaintColorPayload(startMix=true)}. */
    public static final int BUTTON_MIX = 0;

    private final PaintMixerBlockEntity mixer;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public PaintMixerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (PaintMixerBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public PaintMixerMenu(int id, Inventory inv, PaintMixerBlockEntity mixer) {
        super(OCMenus.PAINT_MIXER.get(), id);
        this.mixer = mixer;
        this.access = ContainerLevelAccess.create(mixer.getLevel(), mixer.getBlockPos());
        this.data = new SimpleContainerData(2) {
            @Override
            public int get(int index) {
                return index == 0 ? mixer.getProgress() : mixer.getTargetColor();
            }

            @Override
            public void set(int index, int value) {
                // Data slots travel as shorts, which would truncate the colour; the block
                // entity packet carries the real value.
            }
        };

        addSlot(new SlotItemHandler(mixer.getItems(), PaintMixerBlockEntity.SLOT_INPUT, 8, 28) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return PaintMixerBlockEntity.isValidInput(stack);
            }
        });
        addSlot(new SlotItemHandler(mixer.getItems(), PaintMixerBlockEntity.SLOT_CYAN, 44, 28) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.CYAN_DYE);
            }
        });
        addSlot(new SlotItemHandler(mixer.getItems(), PaintMixerBlockEntity.SLOT_MAGENTA, 62, 28) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.MAGENTA_DYE);
            }
        });
        addSlot(new SlotItemHandler(mixer.getItems(), PaintMixerBlockEntity.SLOT_YELLOW, 80, 28) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.YELLOW_DYE);
            }
        });
        addSlot(new SlotItemHandler(mixer.getItems(), PaintMixerBlockEntity.SLOT_BLACK, 98, 28) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.BLACK_DYE);
            }
        });
        addSlot(new SlotItemHandler(mixer.getItems(), PaintMixerBlockEntity.SLOT_OUTPUT, 152, 28) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 134 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 192));
        }
        addDataSlots(data);
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getTargetColor() {
        return data.get(1);
    }

    public net.minecraft.core.BlockPos pos() {
        return mixer.getBlockPos();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (mixer.getLevel() == null || mixer.getLevel().isClientSide()) {
            return false;
        }
        if (id == BUTTON_MIX) {
            mixer.startMix();
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
            if (index < 6) {
                if (!moveItemStackTo(stack, 6, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, 5, false)) {
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
        return stillValid(access, player, OCBlocks.PAINT_MIXER.get());
    }
}
