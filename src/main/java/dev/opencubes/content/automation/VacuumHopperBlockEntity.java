package dev.opencubes.content.automation;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.util.ExperienceUtil;
import dev.opencubes.util.FluidHandlerBridge;
import dev.opencubes.util.ItemHandlerBridge;
import dev.opencubes.util.SideBitmask;
import dev.opencubes.util.XpFluidUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Pulls nearby items and XP orbs, then pushes them out configured sides. Sneak-click with an
 * empty hand toggles the vacuum. Side masks are the menu-framework proof of concept.
 */
public class VacuumHopperBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOTS = 10;

    private final ItemStackHandler items = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final FluidTank tank = new FluidTank(tankCapacity(), XpFluidUtil::isXpJuice) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    /** Enough juice for five levels of experience, matching the original. */
    private static int tankCapacity() {
        return Math.max(1000, XpFluidUtil.toMillibuckets(ExperienceUtil.experienceAtLevel(5)));
    }

    private int itemOutputSides;
    private int xpOutputSides;
    private boolean vacuumDisabled;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> tank.getFluidAmount();
                case 1 -> tank.getCapacity();
                case 2 -> itemOutputSides;
                case 3 -> xpOutputSides;
                case 4 -> vacuumDisabled ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 2 -> itemOutputSides = value;
                case 3 -> xpOutputSides = value;
                case 4 -> vacuumDisabled = value != 0;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public VacuumHopperBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.VACUUM_HOPPER.get(), pos, state);
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

    public int getItemOutputSides() {
        return itemOutputSides;
    }

    public int getXpOutputSides() {
        return xpOutputSides;
    }

    public boolean isVacuumDisabled() {
        return vacuumDisabled;
    }

    public void toggleVacuum() {
        vacuumDisabled = !vacuumDisabled;
        setChanged();
        sync();
    }

    public void toggleItemSide(Direction side) {
        itemOutputSides = SideBitmask.toggle(itemOutputSides, side);
        setChanged();
        sync();
    }

    public void toggleXpSide(Direction side) {
        xpOutputSides = SideBitmask.toggle(xpOutputSides, side);
        setChanged();
        sync();
    }

    @Nullable
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return items;
        }
        return SideBitmask.has(itemOutputSides, side) ? new ExtractOnlyHandler(items) : null;
    }

    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (side == null) {
            return tank;
        }
        return SideBitmask.has(xpOutputSides, side) ? tank : null;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VacuumHopperBlockEntity vacuum) {
        if (vacuum.vacuumDisabled) {
            return;
        }

        if (level.isClientSide()) {
            level.addParticle(ParticleTypes.PORTAL,
                    pos.getX() + 0.5D + (level.getRandom().nextDouble() - 0.5D),
                    pos.getY() + 0.5D + (level.getRandom().nextDouble() - 1.0D),
                    pos.getZ() + 0.5D + (level.getRandom().nextDouble() - 0.5D),
                    (level.getRandom().nextDouble() - 0.5D) * 2.0D,
                    -level.getRandom().nextDouble(),
                    (level.getRandom().nextDouble() - 0.5D) * 2.0D);
            return;
        }

        double range = OCCommonConfig.VACUUM_HOPPER_RANGE.get();
        AABB area = new AABB(pos).inflate(range);
        List<Entity> entities = level.getEntities((Entity) null, area, vacuum::isInteresting);

        for (Entity entity : entities) {
            double dx = pos.getX() + 0.5D - entity.getX();
            double dy = pos.getY() + 0.5D - entity.getY();
            double dz = pos.getZ() + 0.5D - entity.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance < 1.1D) {
                vacuum.onEntityCollided(entity);
            } else {
                double pull = 1.0D - distance / Math.max(1.0D, range * 5.0D);
                if (pull > 0.0D) {
                    pull *= pull;
                    entity.setDeltaMovement(entity.getDeltaMovement().add(
                            dx / distance * pull * 0.05D,
                            dy / distance * pull * 0.2D,
                            dz / distance * pull * 0.05D));
                }
            }
        }

        if (level.getGameTime() % OCCommonConfig.VACUUM_HOPPER_TICK_INTERVAL.get() == 0) {
            vacuum.outputToNeighbors();
        }
    }

    private boolean isInteresting(Entity entity) {
        if (!entity.isAlive()) {
            return false;
        }
        if (entity instanceof ItemEntity item) {
            return ItemHandlerHelper.insertItem(items, item.getItem(), true).getCount()
                    < item.getItem().getCount();
        }
        if (entity instanceof ExperienceOrb) {
            return tank.getSpace() > 0;
        }
        return false;
    }

    public void onEntityCollided(Entity entity) {
        if (level == null || level.isClientSide() || vacuumDisabled) {
            return;
        }
        if (entity instanceof ItemEntity item && entity.isAlive()) {
            ItemStack leftover = ItemHandlerHelper.insertItem(items, item.getItem().copy(), false);
            item.setItem(leftover);
            if (leftover.isEmpty()) {
                item.discard();
            }
        } else if (entity instanceof ExperienceOrb orb && tank.getSpace() > 0) {
            int mb = XpFluidUtil.toMillibuckets(orb.getValue());
            int filled = tank.fill(XpFluidUtil.juice(mb), IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                orb.discard();
            }
        }
    }

    private void outputToNeighbors() {
        if (level == null) {
            return;
        }
        pushFluid();
        pushItems();
    }

    private void pushFluid() {
        if (SideBitmask.isEmpty(xpOutputSides) || tank.getFluidAmount() <= 0) {
            return;
        }
        for (Direction side : Direction.values()) {
            if (!SideBitmask.has(xpOutputSides, side)) {
                continue;
            }
            var found = level.getCapability(
                    Capabilities.Fluid.BLOCK, worldPosition.relative(side), side.getOpposite());
            if (found == null) {
                continue;
            }
            IFluidHandler neighbour = FluidHandlerBridge.asTanks(found);
            FluidStack drained = tank.drain(50, IFluidHandler.FluidAction.SIMULATE);
            if (drained.isEmpty()) {
                return;
            }
            int filled = neighbour.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    private void pushItems() {
        if (SideBitmask.isEmpty(itemOutputSides)) {
            return;
        }
        Direction[] sides = Direction.values();
        int start = level.getRandom().nextInt(sides.length);
        for (int n = 0; n < sides.length; n++) {
            Direction side = sides[(start + n) % sides.length];
            if (!SideBitmask.has(itemOutputSides, side)) {
                continue;
            }
            var found = level.getCapability(
                    Capabilities.Item.BLOCK, worldPosition.relative(side), side.getOpposite());
            if (found == null) {
                continue;
            }
            IItemHandler neighbour = ItemHandlerBridge.asSlots(found);
            for (int slot = 0; slot < items.getSlots(); slot++) {
                ItemStack stack = items.extractItem(slot, 1, true);
                if (stack.isEmpty()) {
                    continue;
                }
                ItemStack leftover = ItemHandlerHelper.insertItem(neighbour, stack, false);
                if (leftover.getCount() < stack.getCount()) {
                    items.extractItem(slot, stack.getCount() - leftover.getCount(), false);
                    return;
                }
            }
        }
    }

    private void sync() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().blockChanged(worldPosition);
        }
    }

    /**
     * Pushes the item/xp output masks onto the blockstate's per-side {@link FaceMode} properties
     * so the multipart model can show the right overlay. A no-op when nothing actually changed.
     */
    private void syncFaces() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof VacuumHopperBlock)) {
            return;
        }
        BlockState updated = state;
        for (Direction side : Direction.values()) {
            FaceMode mode = FaceMode.of(SideBitmask.has(itemOutputSides, side), SideBitmask.has(xpOutputSides, side));
            EnumProperty<FaceMode> property = VacuumHopperBlock.propertyForSide(side);
            if (updated.getValue(property) != mode) {
                updated = updated.setValue(property, mode);
            }
        }
        if (updated != state) {
            level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        syncFaces();
        requestModelDataUpdate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        syncFaces();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.opencubes.vacuum_hopper");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new VacuumHopperMenu(containerId, playerInventory, this, data);
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
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        if (tag.keySet().contains("Items")) {
            tag.child("Items").ifPresent(items::deserialize);
        }
        if (tag.keySet().contains("Tank")) {
            tag.child("Tank").ifPresent(tank::deserialize);
        }
        itemOutputSides = tag.getIntOr("ItemOutputs", 0);
        xpOutputSides = tag.getIntOr("XpOutputs", 0);
        vacuumDisabled = tag.getBooleanOr("VacuumDisabled", false);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        items.serialize(tag.child("Items"));
        tag.putChild("Tank", tank);
        tag.putInt("ItemOutputs", itemOutputSides);
        tag.putInt("XpOutputs", xpOutputSides);
        tag.putBoolean("VacuumDisabled", vacuumDisabled);
    }

    /** Extract-only view so hoppers can pull from configured sides but never push in that way. */
    private static final class ExtractOnlyHandler implements IItemHandler {
        private final IItemHandler inner;

        private ExtractOnlyHandler(IItemHandler inner) {
            this.inner = inner;
        }

        @Override
        public int getSlots() {
            return inner.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inner.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return inner.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inner.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    }
}
