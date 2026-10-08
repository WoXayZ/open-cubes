package dev.opencubes.content.paint;

import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.registry.OCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Slots: 0 = milk / paint can input, 1-4 = C/M/Y/K dyes, 5 = output paint can.
 * Mix consumes CMYK ink for the selected RGB and writes a full can to the output.
 */
public class PaintMixerBlockEntity extends BlockEntity implements MenuProvider {

    public static final int MIX_TIME = 300;
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_CYAN = 1;
    public static final int SLOT_MAGENTA = 2;
    public static final int SLOT_YELLOW = 3;
    public static final int SLOT_BLACK = 4;
    public static final int SLOT_OUTPUT = 5;

    private final ItemStackHandler items = new ItemStackHandler(6) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case SLOT_INPUT -> isValidInput(stack);
                case SLOT_CYAN -> stack.is(Items.CYAN_DYE);
                case SLOT_MAGENTA -> stack.is(Items.MAGENTA_DYE);
                case SLOT_YELLOW -> stack.is(Items.YELLOW_DYE);
                case SLOT_BLACK -> stack.is(Items.BLACK_DYE);
                default -> false;
            };
        }
    };

    private int progress;
    private boolean mixing;
    private int targetColor = 0xFFFFFF;
    private float lvlCyan;
    private float lvlMagenta;
    private float lvlYellow;
    private float lvlBlack;

    public PaintMixerBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.PAINT_MIXER.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getProgress() {
        return progress;
    }

    public int getTargetColor() {
        return targetColor;
    }

    public void setTargetColor(int rgb) {
        int colour = rgb & 0xFFFFFF;
        if (this.targetColor == colour) {
            return;
        }
        this.targetColor = colour;
        setChanged();
        syncColour();
    }

    /** Pushes the selected colour to clients so the front overlay tint updates live. */
    private void syncColour() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /** OpenBlocks-style Mix: set colour then begin (or restart) the craft. */
    public void requestMix(int rgb) {
        int colour = rgb & 0xFFFFFF;
        if (mixing) {
            if (colour == targetColor) {
                return;
            }
            mixing = false;
            progress = 0;
        }
        targetColor = colour;
        if (canStart()) {
            // Instant craft: Mix should finish on click, not over MIX_TIME ticks.
            finishMix(PaintColors.toCmyk(targetColor));
            mixing = false;
            progress = 0;
        }
        setChanged();
        syncColour();
    }

    public void startMix() {
        requestMix(targetColor);
    }

    private boolean canStart() {
        ItemStack input = items.getStackInSlot(SLOT_INPUT);
        ItemStack output = items.getStackInSlot(SLOT_OUTPUT);
        if (!output.isEmpty() || !isValidInput(input)) {
            return false;
        }
        return hasSufficientInk(PaintColors.toCmyk(targetColor));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PaintMixerBlockEntity mixer) {
        if (!mixer.mixing) {
            return;
        }
        if (!mixer.isValidInput(mixer.items.getStackInSlot(SLOT_INPUT))
                || !mixer.items.getStackInSlot(SLOT_OUTPUT).isEmpty()
                || !mixer.hasSufficientInk(PaintColors.toCmyk(mixer.targetColor))) {
            mixer.mixing = false;
            mixer.progress = 0;
            mixer.setChanged();
            return;
        }
        mixer.progress++;
        if (mixer.progress >= MIX_TIME) {
            mixer.finishMix(PaintColors.toCmyk(mixer.targetColor));
        }
        mixer.setChanged();
    }

    private void finishMix(PaintColors.Cmyk cmyk) {
        lvlCyan -= cmyk.cyan();
        lvlMagenta -= cmyk.magenta();
        lvlYellow -= cmyk.yellow();
        lvlBlack -= cmyk.key();
        items.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
        items.setStackInSlot(SLOT_OUTPUT,
                PaintCanItem.create(OCBlocks.PAINT_CAN.get(), targetColor, PaintCanBlock.FULL_AMOUNT));
        mixing = false;
        progress = 0;
    }

    private boolean hasSufficientInk(PaintColors.Cmyk cmyk) {
        if (cmyk.cyan() > lvlCyan && !tryRefill(SLOT_CYAN, () -> lvlCyan += 1f)) {
            return false;
        }
        if (cmyk.magenta() > lvlMagenta && !tryRefill(SLOT_MAGENTA, () -> lvlMagenta += 1f)) {
            return false;
        }
        if (cmyk.yellow() > lvlYellow && !tryRefill(SLOT_YELLOW, () -> lvlYellow += 1f)) {
            return false;
        }
        if (cmyk.key() > lvlBlack && !tryRefill(SLOT_BLACK, () -> lvlBlack += 1f)) {
            return false;
        }
        // Top up tanks while there is room so dyes keep feeding between mixes.
        if (lvlCyan <= 1f) {
            tryRefill(SLOT_CYAN, () -> lvlCyan += 1f);
        }
        if (lvlMagenta <= 1f) {
            tryRefill(SLOT_MAGENTA, () -> lvlMagenta += 1f);
        }
        if (lvlYellow <= 1f) {
            tryRefill(SLOT_YELLOW, () -> lvlYellow += 1f);
        }
        if (lvlBlack <= 1f) {
            tryRefill(SLOT_BLACK, () -> lvlBlack += 1f);
        }
        return lvlCyan >= cmyk.cyan() && lvlMagenta >= cmyk.magenta()
                && lvlYellow >= cmyk.yellow() && lvlBlack >= cmyk.key();
    }

    private boolean tryRefill(int slot, Runnable addLevel) {
        ItemStack stack = items.getStackInSlot(slot);
        if (stack.isEmpty() || !items.isItemValid(slot, stack)) {
            return false;
        }
        stack.shrink(1);
        items.setStackInSlot(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
        addLevel.run();
        return true;
    }

    public static boolean isValidInput(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(Items.MILK_BUCKET)) {
            return true;
        }
        Item paintCan = OCBlocks.PAINT_CAN.get().asItem();
        return stack.is(paintCan);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.opencubes.paint_mixer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new PaintMixerMenu(id, inv, this);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        items.serialize(tag.child("Items"));
        tag.putInt("Progress", progress);
        tag.putBoolean("Mixing", mixing);
        tag.putInt("Color", targetColor);
        tag.putFloat("Cyan", lvlCyan);
        tag.putFloat("Magenta", lvlMagenta);
        tag.putFloat("Yellow", lvlYellow);
        tag.putFloat("Black", lvlBlack);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        if (tag.keySet().contains("Items")) {
            tag.child("Items").ifPresent(items::deserialize);
        }
        progress = tag.getIntOr("Progress", 0);
        mixing = tag.getBooleanOr("Mixing", false);
        targetColor = tag.getIntOr("Color", 0);
        lvlCyan = tag.getFloatOr("Cyan", 0.0F);
        lvlMagenta = tag.getFloatOr("Magenta", 0.0F);
        lvlYellow = tag.getFloatOr("Yellow", 0.0F);
        lvlBlack = tag.getFloatOr("Black", 0.0F);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ValueInput input) {
        int before = targetColor;
        loadAdditional(input);
        // The overlay tint is baked into the chunk mesh, so it needs a re-render to change.
        if (level != null && level.isClientSide() && before != targetColor) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }
}
