package dev.opencubes.content.xp;

import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.registry.OCSounds;
import dev.opencubes.util.AutomationHandlers;
import dev.opencubes.util.SideBitmask;
import dev.opencubes.util.SideIoAutomation;
import dev.opencubes.util.XpFluidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Turns liquid XP and glass bottles into bottles o' enchanting. Per-side item and XP I/O.
 */
public class XpBottlerBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int PROGRESS_TICKS = 40;

    private static final int FLAG_AUTO_PULL = 1;
    private static final int FLAG_AUTO_PUSH = 2;
    private static final int FLAG_AUTO_XP = 4;

    private final FluidTank tank = new FluidTank(XpFluidUtil.millibucketsPerBottle(), XpFluidUtil::isXpJuice) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final ItemStackHandler items = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == SLOT_INPUT && stack.is(Items.GLASS_BOTTLE);
        }
    };

    private int progress;
    private int itemInputSides;
    private int itemOutputSides;
    private int xpSides;
    private int autoFlags;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> tank.getFluidAmount();
                case 2 -> tank.getCapacity();
                case 3 -> itemInputSides;
                case 4 -> itemOutputSides;
                case 5 -> xpSides;
                case 6 -> autoFlags;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 3 -> itemInputSides = value;
                case 4 -> itemOutputSides = value;
                case 5 -> xpSides = value;
                case 6 -> autoFlags = value;
            }
        }

        @Override
        public int getCount() {
            return 7;
        }
    };

    public XpBottlerBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.XP_BOTTLER.get(), pos, state);
    }

    public FluidTank getTank() {
        return tank;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ContainerData getData() {
        return data;
    }

    public void toggleItemInputSide(Direction side) {
        itemInputSides = SideBitmask.toggle(itemInputSides, side);
        setChanged();
        sync();
    }

    public void toggleItemOutputSide(Direction side) {
        itemOutputSides = SideBitmask.toggle(itemOutputSides, side);
        setChanged();
        sync();
    }

    public void toggleXpSide(Direction side) {
        xpSides = SideBitmask.toggle(xpSides, side);
        setChanged();
        sync();
    }

    public void toggleAutoPull() {
        autoFlags ^= FLAG_AUTO_PULL;
        setChanged();
        sync();
    }

    public void toggleAutoPush() {
        autoFlags ^= FLAG_AUTO_PUSH;
        setChanged();
        sync();
    }

    public void toggleAutoXp() {
        autoFlags ^= FLAG_AUTO_XP;
        setChanged();
        sync();
    }

    private IItemHandlerModifiable getInputSlots() {
        return new RangedWrapper(items, SLOT_INPUT, SLOT_INPUT + 1) {
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }
        };
    }

    private IItemHandlerModifiable getOutputSlots() {
        return new RangedWrapper(items, SLOT_OUTPUT, SLOT_OUTPUT + 1) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return stack;
            }
        };
    }

    private IItemHandler getInputHandler() {
        return new AutomationHandlers.InsertOnlyHandler(getInputSlots());
    }

    private IItemHandler getOutputHandler() {
        return new AutomationHandlers.ExtractOnlyHandler(getOutputSlots());
    }

    @Nullable
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return new CombinedInvWrapper(getInputSlots(), getOutputSlots());
        }
        boolean input = SideBitmask.has(itemInputSides, side);
        boolean output = SideBitmask.has(itemOutputSides, side);
        if (!input && !output) {
            return null;
        }
        if (input && output) {
            return new CombinedInvWrapper(getInputSlots(), getOutputSlots());
        }
        return input ? getInputHandler() : getOutputHandler();
    }

    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (side == null) {
            return tank;
        }
        return SideBitmask.has(xpSides, side) ? tank : null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, XpBottlerBlockEntity bottler) {
        bottler.runAutoIo();
        boolean lit = state.getValue(BlockStateProperties.LIT);
        if (bottler.canWork()) {
            bottler.progress++;
            if (bottler.progress >= PROGRESS_TICKS) {
                bottler.progress = 0;
                bottler.craft();
                level.playSound(null, pos, OCSounds.BOTTLER_DONE.get(), SoundSource.BLOCKS, 0.5F, 0.8F);
            }
            bottler.setChanged();
            if (!lit) {
                level.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), 3);
            }
        } else {
            if (bottler.progress > 0) {
                bottler.progress = 0;
                bottler.setChanged();
            }
            if (lit) {
                level.setBlock(pos, state.setValue(BlockStateProperties.LIT, false), 3);
            }
        }
    }

    private void runAutoIo() {
        if (level == null || level.isClientSide()) {
            return;
        }
        if ((autoFlags & FLAG_AUTO_PULL) != 0) {
            SideIoAutomation.pullItems(level, worldPosition, itemInputSides, getInputHandler());
        }
        if ((autoFlags & FLAG_AUTO_PUSH) != 0) {
            SideIoAutomation.pushItems(level, worldPosition, itemOutputSides, getOutputHandler());
        }
        if ((autoFlags & FLAG_AUTO_XP) != 0) {
            SideIoAutomation.pullXpFluid(level, worldPosition, xpSides, tank);
        }
    }

    private boolean canWork() {
        if (!items.getStackInSlot(SLOT_INPUT).is(Items.GLASS_BOTTLE)) {
            return false;
        }
        if (tank.getFluidAmount() < XpFluidUtil.millibucketsPerBottle()) {
            return false;
        }
        ItemStack output = items.getStackInSlot(SLOT_OUTPUT);
        return output.isEmpty()
                || (output.is(Items.EXPERIENCE_BOTTLE) && output.getCount() < output.getMaxStackSize());
    }

    private void craft() {
        tank.drain(XpFluidUtil.millibucketsPerBottle(), IFluidHandler.FluidAction.EXECUTE);
        items.extractItem(SLOT_INPUT, 1, false);
        ItemStack output = items.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) {
            items.setStackInSlot(SLOT_OUTPUT, new ItemStack(Items.EXPERIENCE_BOTTLE));
        } else {
            output.grow(1);
        }
    }

    private void sync() {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.getChunkSource().blockChanged(worldPosition);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.opencubes.xp_bottler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new XpBottlerMenu(containerId, playerInventory, this, data);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        if (tag.keySet().contains("Tank")) {
            tag.child("Tank").ifPresent(tank::deserialize);
        }
        if (tag.keySet().contains("Items")) {
            tag.child("Items").ifPresent(items::deserialize);
        }
        progress = tag.getIntOr("Progress", 0);
        itemInputSides = tag.getIntOr("ItemInputs", 0);
        itemOutputSides = tag.getIntOr("ItemOutputs", 0);
        xpSides = tag.getIntOr("XpInputs", 0);
        autoFlags = tag.getIntOr("AutoFlags", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putChild("Tank", tank);
        items.serialize(tag.child("Items"));
        tag.putInt("Progress", progress);
        tag.putInt("ItemInputs", itemInputSides);
        tag.putInt("ItemOutputs", itemOutputSides);
        tag.putInt("XpInputs", xpSides);
        tag.putInt("AutoFlags", autoFlags);
    }
}
