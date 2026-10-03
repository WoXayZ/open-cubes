package dev.opencubes.content.automation;

import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.util.AutomationHandlers;
import dev.opencubes.util.ExperienceUtil;
import dev.opencubes.util.SideBitmask;
import dev.opencubes.util.SideIoAutomation;
import dev.opencubes.util.VanillaAnvilLogic;
import dev.opencubes.util.XpFluidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * Anvil that burns liquid XP instead of player levels. Per-side item and XP I/O with optional
 * auto pull/push from configured faces.
 */
public class AutoAnvilBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_TOOL = 0;
    public static final int SLOT_MODIFIER = 1;
    public static final int SLOT_OUTPUT = 2;

    private static final int FLAG_AUTO_PULL = 1;
    private static final int FLAG_AUTO_PUSH = 2;
    private static final int FLAG_AUTO_XP = 4;

    public static int cooldownTicks() {
        return dev.opencubes.config.OCCommonConfig.AUTO_ANVIL_COOLDOWN.get();
    }

    public static int maxStoredLevels() {
        return dev.opencubes.config.OCCommonConfig.AUTO_ANVIL_MAX_LEVELS.get();
    }

    private final ItemStackHandler items = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot != SLOT_OUTPUT;
        }
    };

    private final FluidTank tank = new FluidTank(tankCapacity(), XpFluidUtil::isXpJuice) {
        @Override
        protected void onContentsChanged() {
            setChanged();
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
                case 0 -> tank.getFluidAmount();
                case 1 -> tank.getCapacity();
                case 2 -> itemInputSides;
                case 3 -> itemOutputSides;
                case 4 -> xpSides;
                case 5 -> autoFlags;
                case 6 -> progress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 2 -> itemInputSides = value;
                case 3 -> itemOutputSides = value;
                case 4 -> xpSides = value;
                case 5 -> autoFlags = value;
                case 6 -> progress = value;
            }
        }

        @Override
        public int getCount() {
            return 7;
        }
    };

    public AutoAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.AUTO_ANVIL.get(), pos, state);
    }

    private static int tankCapacity() {
        return Math.max(1000, XpFluidUtil.toMillibuckets(ExperienceUtil.experienceAtLevel(maxStoredLevels())));
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public FluidTank getTank() {
        return tank;
    }

    public ContainerData getData() {
        return data;
    }

    public int getItemInputSides() {
        return itemInputSides;
    }

    public int getItemOutputSides() {
        return itemOutputSides;
    }

    public int getXpSides() {
        return xpSides;
    }

    public boolean isAutoPull() {
        return (autoFlags & FLAG_AUTO_PULL) != 0;
    }

    public boolean isAutoPush() {
        return (autoFlags & FLAG_AUTO_PUSH) != 0;
    }

    public boolean isAutoXp() {
        return (autoFlags & FLAG_AUTO_XP) != 0;
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
        return new RangedWrapper(items, SLOT_TOOL, SLOT_OUTPUT) {
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

    public int getProgress() {
        return progress;
    }

    public static int workTicks() {
        return cooldownTicks();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AutoAnvilBlockEntity anvil) {
        anvil.runAutoIo();
        if (anvil.items.getStackInSlot(SLOT_OUTPUT).isEmpty() && anvil.canRepair()) {
            anvil.progress++;
            if (anvil.progress >= workTicks()) {
                anvil.progress = 0;
                anvil.repairItem();
            }
            anvil.setChanged();
        } else if (anvil.progress != 0) {
            anvil.progress = 0;
            anvil.setChanged();
        }
    }

    private boolean canRepair() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        ItemStack tool = items.getStackInSlot(SLOT_TOOL);
        ItemStack modifier = items.getStackInSlot(SLOT_MODIFIER);
        if (tool.isEmpty()) {
            return false;
        }
        VanillaAnvilLogic logic = new VanillaAnvilLogic(serverLevel, tool, modifier);
        ItemStack output = logic.getOutputStack();
        if (output.isEmpty()) {
            return false;
        }
        int levelCost = Math.max(0, logic.getLevelCost());
        int xpCost = levelCost > 0 ? ExperienceUtil.experienceAtLevel(levelCost) : 0;
        int liquidCost = XpFluidUtil.toMillibuckets(xpCost);
        return liquidCost <= 0 || tank.getFluidAmount() >= liquidCost;
    }

    private void runAutoIo() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (isAutoPull()) {
            SideIoAutomation.pullItems(level, worldPosition, itemInputSides, getInputHandler());
        }
        if (isAutoPush()) {
            SideIoAutomation.pushItems(level, worldPosition, itemOutputSides, getOutputHandler());
        }
        if (isAutoXp()) {
            SideIoAutomation.pullXpFluid(level, worldPosition, xpSides, tank);
        }
    }

    private void repairItem() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!items.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
            return;
        }

        ItemStack tool = items.getStackInSlot(SLOT_TOOL);
        ItemStack modifier = items.getStackInSlot(SLOT_MODIFIER);
        if (tool.isEmpty()) {
            return;
        }

        VanillaAnvilLogic logic = new VanillaAnvilLogic(serverLevel, tool, modifier);
        ItemStack output = logic.getOutputStack().copy();
        if (output.isEmpty()) {
            return;
        }

        int levelCost = Math.max(0, logic.getLevelCost());
        int xpCost = levelCost > 0 ? ExperienceUtil.experienceAtLevel(levelCost) : 0;
        int liquidCost = XpFluidUtil.toMillibuckets(xpCost);
        if (liquidCost > 0 && tank.getFluidAmount() < liquidCost) {
            return;
        }

        if (liquidCost > 0) {
            tank.drain(liquidCost, IFluidHandler.FluidAction.EXECUTE);
        }
        consumeModifier(logic.getModifierCost());
        // Clear input first so auto-pull cannot refill the tool before the output is written.
        items.setStackInSlot(SLOT_TOOL, ItemStack.EMPTY);
        items.setStackInSlot(SLOT_OUTPUT, output);
        level.playSound(null, worldPosition, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.3F, 1.0F);
        setChanged();
        sync();
    }

    private void consumeModifier(int materialCost) {
        if (materialCost > 0) {
            ItemStack modifier = items.getStackInSlot(SLOT_MODIFIER);
            modifier.shrink(materialCost);
            items.setStackInSlot(SLOT_MODIFIER, modifier);
        } else {
            items.setStackInSlot(SLOT_MODIFIER, ItemStack.EMPTY);
        }
    }

    private void sync() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().blockChanged(worldPosition);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.opencubes.auto_anvil");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AutoAnvilMenu(containerId, playerInventory, this, data);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items")) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
        }
        if (tag.contains("Tank")) {
            tank.readFromNBT(registries, tag.getCompound("Tank"));
        }
        progress = tag.contains("Progress") ? tag.getInt("Progress") : tag.getInt("Cooldown");
        itemInputSides = tag.getInt("ItemInputs");
        itemOutputSides = tag.getInt("ItemOutputs");
        xpSides = tag.getInt("XpInputs");
        autoFlags = tag.getInt("AutoFlags");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Progress", progress);
        tag.putInt("ItemInputs", itemInputSides);
        tag.putInt("ItemOutputs", itemOutputSides);
        tag.putInt("XpInputs", xpSides);
        tag.putInt("AutoFlags", autoFlags);
    }
}
