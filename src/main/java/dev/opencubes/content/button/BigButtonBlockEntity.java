package dev.opencubes.content.button;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds the items that set how long the button stays pressed: one item, one tick.
 *
 * <p>The container is a plain nine-slot grid so it can borrow vanilla's dispenser menu and
 * screen wholesale - no menu type, no screen class and no GUI texture of our own. Upstream
 * used eight slots in a four-by-two grid only because it was drawing the interface itself.
 */
public class BigButtonBlockEntity extends BaseContainerBlockEntity {

    public static final int SLOTS = 9;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

    public BigButtonBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.BIG_BUTTON.get(), pos, state);
    }

    /** How long a press lasts, in ticks. Never zero, or the button could not be pressed at all. */
    public int pressDuration() {
        int total = 0;
        for (ItemStack stack : items) {
            total += stack.getCount();
        }
        return total > 0 ? total : OCCommonConfig.BIG_BUTTON_EMPTY_DURATION.get();
    }

    @Override
    public int getContainerSize() {
        return SLOTS;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.opencubes.big_button");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new DispenserMenu(containerId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }
}
