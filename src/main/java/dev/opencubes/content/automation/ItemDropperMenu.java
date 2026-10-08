package dev.opencubes.content.automation;

import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemDropperMenu extends SimpleMachineMenu {

    public static final int BUTTON_SPEED_DOWN = 0;
    public static final int BUTTON_SPEED_UP = 1;
    public static final int BUTTON_TOGGLE_REDSTONE = 2;
    public static final int SHIFT_OFFSET = 100;

    private final ItemDropperBlockEntity dropper;
    private final ContainerData data;

    public ItemDropperMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, getDropper(playerInventory, buffer.readBlockPos()),
                new SimpleContainerData(2));
    }

    public ItemDropperMenu(int containerId, Inventory playerInventory, ItemDropperBlockEntity dropper,
                           ContainerData data) {
        super(OCMenus.ITEM_DROPPER.get(), containerId, playerInventory, dropper, dropper.getItems(),
                () -> OCBlocks.ITEM_DROPPER.get());
        this.dropper = dropper;
        this.data = data;
        addDataSlots(data);
    }

    private static ItemDropperBlockEntity getDropper(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof ItemDropperBlockEntity dropper) {
            return dropper;
        }
        throw new IllegalStateException("Item dropper missing at " + pos);
    }

    public double getItemSpeed() {
        return data.get(0) / 100.0D;
    }

    public boolean usesRedstoneStrength() {
        return data.get(1) != 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (dropper.getLevel() == null || dropper.getLevel().isClientSide()) {
            return false;
        }
        boolean shift = id >= SHIFT_OFFSET;
        int base = shift ? id - SHIFT_OFFSET : id;
        double step = shift ? 2.5D : 0.25D;
        switch (base) {
            case BUTTON_SPEED_DOWN -> dropper.adjustSpeed(-step);
            case BUTTON_SPEED_UP -> dropper.adjustSpeed(step);
            case BUTTON_TOGGLE_REDSTONE -> dropper.toggleRedstoneScaling();
            default -> {
                return false;
            }
        }
        return true;
    }
}
