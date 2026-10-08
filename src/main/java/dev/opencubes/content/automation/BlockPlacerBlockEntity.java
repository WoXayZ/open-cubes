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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class BlockPlacerBlockEntity extends BlockManipulatorBlockEntity implements MenuProvider {

    public static final int SLOTS = 9;

    private boolean skipInventoryTrigger;

    private final ItemStackHandler items = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!skipInventoryTrigger && level != null && !level.isClientSide()
                    && getBlockState().getValue(BlockManipulatorBlock.POWERED)
                    && !getStackInSlot(slot).isEmpty()) {
                triggerAction();
            }
        }
    };

    public BlockPlacerBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.BLOCK_PLACER.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    @Override
    protected int getActionLimit() {
        return OCCommonConfig.BLOCK_PLACER_ACTION_LIMIT.get();
    }

    @Override
    protected boolean canWork(BlockState targetState, BlockPos target, Direction facing) {
        if (isInventoryEmpty()) {
            return false;
        }
        return targetState.canBeReplaced();
    }

    @Override
    protected void doWork(BlockState targetState, BlockPos target, Direction facing) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int slot = -1;
        ItemStack stack = ItemStack.EMPTY;
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack candidate = items.getStackInSlot(i);
            if (!candidate.isEmpty()) {
                slot = i;
                stack = candidate;
                break;
            }
        }
        if (slot < 0 || stack.isEmpty()) {
            return;
        }

        BlockPos playerPos = target.relative(facing, 2);
        ItemStack before = stack.copy();
        ItemStack after = OCFakePlayers.useItemOn(serverLevel, stack.copy(), playerPos, target,
                facing.getOpposite());

        if (!ItemStack.matches(before, after)) {
            skipInventoryTrigger = true;
            try {
                items.setStackInSlot(slot, after);
            } finally {
                skipInventoryTrigger = false;
            }
        }
    }

    private boolean isInventoryEmpty() {
        for (int i = 0; i < items.getSlots(); i++) {
            if (!items.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.opencubes.block_placer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BlockPlacerMenu(containerId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        if (tag.keySet().contains("Items")) {
            tag.child("Items").ifPresent(items::deserialize);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        items.serialize(tag.child("Items"));
    }
}
