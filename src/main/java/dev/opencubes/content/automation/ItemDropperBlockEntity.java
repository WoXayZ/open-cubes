package dev.opencubes.content.automation;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.util.OCFakePlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class ItemDropperBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity
        implements MenuProvider {

    public static final int SLOTS = 9;

    private final ItemStackHandler items = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private boolean redstonePowered;
    private double itemSpeedBase;
    private boolean useRedstoneStrength;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) Math.round(itemSpeedBase * 100.0D);
                case 1 -> useRedstoneStrength ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> itemSpeedBase = Math.max(0.0D,
                        Math.min(OCCommonConfig.ITEM_DROPPER_MAX_SPEED.get(), value / 100.0D));
                case 1 -> useRedstoneStrength = value != 0;
            }
            setChanged();
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public ItemDropperBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.ITEM_DROPPER.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ContainerData getData() {
        return data;
    }

    public double getItemSpeedBase() {
        return itemSpeedBase;
    }

    public boolean usesRedstoneStrength() {
        return useRedstoneStrength;
    }

    public void onRedstoneChanged(int signal) {
        boolean powered = signal > 0;
        if (powered == redstonePowered) {
            return;
        }
        redstonePowered = powered;
        if (powered) {
            float multiplier = (float) (itemSpeedBase * (useRedstoneStrength ? signal / 15.0F : 1.0F));
            dropItem(multiplier);
        }
    }

    private void dropItem(float speedMultiplier) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }

            ItemStack dropped = stack.split(1);
            items.setStackInSlot(i, stack);

            Direction facing = getBlockState().getValue(ItemDropperBlock.FACING);
            // Local drop: centre of the front face, velocity along facing.
            double x = worldPosition.getX() + 0.5D + facing.getStepX() * 0.7D;
            double y = worldPosition.getY() + 0.5D + facing.getStepY() * 0.7D;
            double z = worldPosition.getZ() + 0.5D + facing.getStepZ() * 0.7D;
            double vx = facing.getStepX() * speedMultiplier;
            double vy = facing.getStepY() * speedMultiplier;
            double vz = facing.getStepZ() * speedMultiplier;

            OCFakePlayers.dropItem(serverLevel, dropped, x, y, z, vx, vy, vz);
            setChanged();
            break;
        }
    }

    public void adjustSpeed(double delta) {
        itemSpeedBase = Math.max(0.0D,
                Math.min(OCCommonConfig.ITEM_DROPPER_MAX_SPEED.get(), itemSpeedBase + delta));
        setChanged();
    }

    public void toggleRedstoneScaling() {
        useRedstoneStrength = !useRedstoneStrength;
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.opencubes.item_dropper");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ItemDropperMenu(containerId, playerInventory, this, data);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        if (tag.keySet().contains("Items")) {
            tag.child("Items").ifPresent(items::deserialize);
        }
        itemSpeedBase = tag.getDoubleOr("ItemSpeed", 0.0D);
        useRedstoneStrength = tag.getBooleanOr("UseRedstoneStrength", false);
        redstonePowered = tag.getBooleanOr("RedstonePowered", false);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        items.serialize(tag.child("Items"));
        tag.putDouble("ItemSpeed", itemSpeedBase);
        tag.putBoolean("UseRedstoneStrength", useRedstoneStrength);
        tag.putBoolean("RedstonePowered", redstonePowered);
    }
}
