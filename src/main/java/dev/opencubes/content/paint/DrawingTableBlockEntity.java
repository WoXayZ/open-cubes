package dev.opencubes.content.paint;

import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.registry.OCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Cuts an unprepared stencil into whatever is selected: selections {@code 0..14} are the stencil
 * patterns in enum order, the following ones are the glyph letters of {@link GlyphItem#CHARACTERS}.
 */
public class DrawingTableBlockEntity extends BlockEntity implements MenuProvider {

    public static final int PATTERN_COUNT = StencilPattern.values().length;
    public static final int SELECTION_COUNT = PATTERN_COUNT + GlyphItem.CHARACTERS.length();

    private final ItemStackHandler items = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    /** Synced to open menus so the selection updates without a block-entity packet. */
    private final ContainerData data = new SimpleContainerData(1);

    private int selection;

    public DrawingTableBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.DRAWING_TABLE.get(), pos, state);
        writeData();
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ContainerData getData() {
        return data;
    }

    public void select(int selection) {
        if (selection < 0 || selection >= SELECTION_COUNT) {
            return;
        }
        this.selection = selection;
        writeData();
        setChanged();
    }

    public boolean cut() {
        ItemStack input = items.getStackInSlot(0);
        ItemStack output = items.getStackInSlot(1);
        if (!input.is(OCItems.UNPREPARED_STENCIL.get()) || !output.isEmpty()) {
            return false;
        }
        input.shrink(1);
        items.setStackInSlot(0, input.isEmpty() ? ItemStack.EMPTY : input);
        items.setStackInSlot(1, result(selection));
        setChanged();
        return true;
    }

    public static boolean isStencil(int selection) {
        return selection < PATTERN_COUNT;
    }

    public static ItemStack result(int selection) {
        if (isStencil(selection)) {
            return StencilItem.create(StencilPattern.values()[selection]);
        }
        return GlyphItem.create(GlyphItem.CHARACTERS.charAt(selection - PATTERN_COUNT));
    }

    private void writeData() {
        data.set(0, selection);
    }

    public static int selectionFromData(ContainerData data) {
        int value = data.get(0);
        return value < 0 || value >= SELECTION_COUNT ? 0 : value;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.opencubes.drawing_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new DrawingTableMenu(id, inv, this);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        items.serialize(tag.child("Items"));
        if (isStencil(selection)) {
            tag.putString("Pattern", StencilPattern.values()[selection].id());
        } else {
            tag.putString("Glyph", String.valueOf(GlyphItem.CHARACTERS.charAt(selection - PATTERN_COUNT)));
        }
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        if (tag.keySet().contains("Items")) {
            tag.child("Items").ifPresent(items::deserialize);
        }
        String glyph = tag.getStringOr("Glyph", "");
        if (!glyph.isEmpty() && !tag.keySet().contains("Pattern")) {
            selection = PATTERN_COUNT + GlyphItem.index(glyph.charAt(0));
        } else {
            selection = StencilPattern.byId(tag.getStringOr("Pattern", "")).ordinal();
        }
        writeData();
    }
}
