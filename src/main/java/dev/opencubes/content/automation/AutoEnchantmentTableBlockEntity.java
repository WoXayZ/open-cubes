package dev.opencubes.content.automation;

import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.util.AutomationHandlers;
import dev.opencubes.util.EnchantmentPowerUtil;
import dev.opencubes.util.ExperienceUtil;
import dev.opencubes.util.SideBitmask;
import dev.opencubes.util.SideIoAutomation;
import dev.opencubes.util.VanillaEnchantLogic;
import dev.opencubes.util.XpFluidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
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
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * Enchanting table that spends liquid XP. Per-side item and XP I/O with optional auto transfer.
 */
public class AutoEnchantmentTableBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_TOOL = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_LAPIS = 2;

    private static final int POWER_CHECK_PERIOD = 20;
    private static final int FLAG_AUTO_PULL = 1;
    private static final int FLAG_AUTO_PUSH = 2;
    private static final int FLAG_AUTO_XP = 4;

    public static int maxStoredLevels() {
        return dev.opencubes.config.OCCommonConfig.AUTO_ENCHANTMENT_TABLE_MAX_LEVELS.get();
    }

    private final ItemStackHandler items = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case SLOT_TOOL -> stack.isEnchantable();
                case SLOT_LAPIS -> stack.is(Items.LAPIS_LAZULI);
                default -> false;
            };
        }
    };

    private final FluidTank tank = new FluidTank(tankCapacity(), XpFluidUtil::isXpJuice) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    public static final int WORK_TICKS = 40;

    private long seed = java.util.concurrent.ThreadLocalRandom.current().nextLong();
    /** Default to full bookshelf power so L3 offers work without raising the cap first. */
    private int powerLimit = 15;
    private int availablePower;
    private int selectedLevelOrdinal;
    private int powerCheckCountdown;
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
                case 2 -> selectedLevelOrdinal;
                case 3 -> powerLimit;
                case 4 -> availablePower;
                case 5 -> itemInputSides;
                case 6 -> itemOutputSides;
                case 7 -> xpSides;
                case 8 -> autoFlags;
                case 9 -> progress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 2 -> selectedLevelOrdinal = Math.floorMod(value, 3);
                case 3 -> powerLimit = Math.max(1, Math.min(30, value));
                case 5 -> itemInputSides = value;
                case 6 -> itemOutputSides = value;
                case 7 -> xpSides = value;
                case 8 -> autoFlags = value;
                case 9 -> progress = value;
            }
            setChanged();
        }

        @Override
        public int getCount() {
            return 10;
        }
    };

    public AutoEnchantmentTableBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.AUTO_ENCHANTMENT_TABLE.get(), pos, state);
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

    private IItemHandlerModifiable getToolSlots() {
        return new RangedWrapper(items, SLOT_TOOL, SLOT_TOOL + 1) {
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }
        };
    }

    private IItemHandlerModifiable getLapisSlots() {
        return new RangedWrapper(items, SLOT_LAPIS, SLOT_LAPIS + 1) {
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
        return new AutomationHandlers.InsertOnlyHandler(
                new CombinedInvWrapper(getToolSlots(), getLapisSlots()));
    }

    private IItemHandler getOutputHandler() {
        return new AutomationHandlers.ExtractOnlyHandler(getOutputSlots());
    }

    @Nullable
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return new CombinedInvWrapper(getToolSlots(), getOutputSlots(), getLapisSlots());
        }
        boolean input = SideBitmask.has(itemInputSides, side);
        boolean output = SideBitmask.has(itemOutputSides, side);
        if (!input && !output) {
            return null;
        }
        if (input && output) {
            return new CombinedInvWrapper(getToolSlots(), getOutputSlots(), getLapisSlots());
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

    public void cycleLevel() {
        selectedLevelOrdinal = (selectedLevelOrdinal + 1) % 3;
        setChanged();
    }

    public void adjustPowerLimit(int delta) {
        powerLimit = Math.max(1, Math.min(30, powerLimit + delta));
        setChanged();
    }

    public int getProgress() {
        return progress;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  AutoEnchantmentTableBlockEntity table) {
        table.runAutoIo();
        if (--table.powerCheckCountdown <= 0) {
            table.powerCheckCountdown = POWER_CHECK_PERIOD;
            table.availablePower = (int) EnchantmentPowerUtil.getPower(level, pos);
            table.setChanged();
        }
        if (table.canEnchant()) {
            table.progress++;
            if (table.progress >= WORK_TICKS) {
                table.progress = 0;
                table.tryEnchantItem();
            }
            table.setChanged();
        } else if (table.progress != 0) {
            table.progress = 0;
            table.setChanged();
        }
    }

    private void runAutoIo() {
        if (level == null || level.isClientSide) {
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

    private boolean canEnchant() {
        return prepareEnchant() != null;
    }

    @Nullable
    private EnchantJob prepareEnchant() {
        if (level == null || level.isClientSide) {
            return null;
        }
        ItemStack tool = items.getStackInSlot(SLOT_TOOL);
        if (tool.isEmpty() || !tool.isEnchantable()) {
            return null;
        }
        ItemStack lapis = items.getStackInSlot(SLOT_LAPIS);
        if (lapis.isEmpty() || !items.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
            return null;
        }
        int power = Math.min(availablePower, powerLimit);
        if (power <= 0) {
            return null;
        }
        VanillaEnchantLogic.Level slot = VanillaEnchantLogic.Level.values()[selectedLevelOrdinal];
        VanillaEnchantLogic logic = new VanillaEnchantLogic(seed);
        if (!logic.setup(tool, slot, power) || logic.getLevelRequirement() <= 0) {
            return null;
        }
        if (lapis.getCount() < logic.getLapisCost()) {
            return null;
        }
        int availableXp = XpFluidUtil.toXp(tank.getFluidAmount());
        int availableLevels = ExperienceUtil.levelForExperience(availableXp);
        if (availableLevels < logic.getLevelRequirement()) {
            return null;
        }
        int requirement = logic.getLevelRequirement();
        int levelsPaid = logic.getLevelCost();
        int fromLevel = Math.max(0, requirement - levelsPaid);
        int xpCost = ExperienceUtil.experienceAtLevel(requirement)
                - ExperienceUtil.experienceAtLevel(fromLevel);
        int liquidCost = XpFluidUtil.toMillibuckets(Math.max(0, xpCost));
        if (tank.getFluidAmount() < liquidCost) {
            return null;
        }
        return new EnchantJob(logic, liquidCost);
    }

    private void tryEnchantItem() {
        EnchantJob job = prepareEnchant();
        if (job == null) {
            return;
        }
        ItemStack lapis = items.getStackInSlot(SLOT_LAPIS);
        ItemStack enchanted = job.logic().enchant(level.registryAccess());
        if (enchanted.isEmpty()) {
            return;
        }
        items.setStackInSlot(SLOT_TOOL, ItemStack.EMPTY);
        items.setStackInSlot(SLOT_OUTPUT, enchanted);
        lapis.shrink(job.logic().getLapisCost());
        items.setStackInSlot(SLOT_LAPIS, lapis);
        tank.drain(job.liquidCost(), IFluidHandler.FluidAction.EXECUTE);
        seed = level.random.nextLong();
        level.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS,
                1.0F, level.random.nextFloat() * 0.1F + 0.9F);
        setChanged();
    }

    private record EnchantJob(VanillaEnchantLogic logic, int liquidCost) {}

    private void sync() {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.getChunkSource().blockChanged(worldPosition);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.opencubes.auto_enchanting_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AutoEnchantmentTableMenu(containerId, playerInventory, this, data);
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
        seed = tag.getLong("Seed");
        powerLimit = tag.contains("PowerLimit")
                ? Math.max(1, Math.min(30, tag.getInt("PowerLimit")))
                : 15;
        selectedLevelOrdinal = Math.floorMod(tag.getInt("SelectedLevel"), 3);
        availablePower = tag.getInt("AvailablePower");
        progress = tag.getInt("Progress");
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
        tag.putLong("Seed", seed);
        tag.putInt("PowerLimit", powerLimit);
        tag.putInt("SelectedLevel", selectedLevelOrdinal);
        tag.putInt("AvailablePower", availablePower);
        tag.putInt("Progress", progress);
        tag.putInt("ItemInputs", itemInputSides);
        tag.putInt("ItemOutputs", itemOutputSides);
        tag.putInt("XpInputs", xpSides);
        tag.putInt("AutoFlags", autoFlags);
    }
}
