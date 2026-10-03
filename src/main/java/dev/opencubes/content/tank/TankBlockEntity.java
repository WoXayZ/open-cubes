package dev.opencubes.content.tank;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.util.XpFluidUtil;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/**
 * A 16-bucket glass tank. Fluids drain downward into a tank below; neighbouring tanks of the
 * same fluid equalise horizontally. XP juice can be drunk from it with an empty hand.
 *
 * <p>Multi-tank continuous rendering comes later. For now each tank draws its own fluid level.
 */
public class TankBlockEntity extends BlockEntity {

    public static int capacityMb() {
        return OCCommonConfig.TANK_BUCKETS_PER_TANK.get() * 1000;
    }

    private final FluidTank tank = new FluidTank(capacityMb()) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
                level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            }
        }
    };

    private int ticks;

    public TankBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.TANK.get(), pos, state);
    }

    public FluidTank getTank() {
        return tank;
    }

    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return tank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TankBlockEntity tankEntity) {
        tankEntity.ticks++;
        int capacity = capacityMb();
        if (tankEntity.tank.getCapacity() != capacity) {
            tankEntity.tank.setCapacity(capacity);
        }
        if (!OCCommonConfig.TANK_UPDATE.get()) {
            return;
        }
        // Gravity first, then horizontal equalise - same order as the original.
        tankEntity.drainDown();
        if (tankEntity.ticks % 10 == 0) {
            tankEntity.equaliseHorizontally();
        }
    }

    private void drainDown() {
        if (tank.isEmpty() || level == null) {
            return;
        }
        BlockPos below = worldPosition.below();
        if (!(level.getBlockEntity(below) instanceof TankBlockEntity belowTank)) {
            return;
        }
        FluidStack drained = tank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (drained.isEmpty()) {
            return;
        }
        int filled = belowTank.tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        if (filled > 0) {
            tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private void equaliseHorizontally() {
        if (tank.isEmpty() || level == null) {
            return;
        }
        FluidStack own = tank.getFluid();
        int threshold = OCCommonConfig.TANK_BALANCE_THRESHOLD.get();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbourPos = worldPosition.relative(direction);
            if (!(level.getBlockEntity(neighbourPos) instanceof TankBlockEntity neighbour)) {
                continue;
            }
            FluidStack theirs = neighbour.tank.getFluid();
            if (!theirs.isEmpty() && !FluidStack.isSameFluidSameComponents(own, theirs)) {
                continue;
            }
            if (theirs.isEmpty() && neighbour.tank.getSpace() == 0) {
                continue;
            }

            int total = own.getAmount() + theirs.getAmount();
            int target = total / 2;
            int diff = own.getAmount() - target;
            if (Math.abs(diff) <= threshold) {
                continue;
            }
            if (diff > 0) {
                FluidStack moved = tank.drain(diff, IFluidHandler.FluidAction.EXECUTE);
                neighbour.tank.fill(moved, IFluidHandler.FluidAction.EXECUTE);
            } else {
                FluidStack moved = neighbour.tank.drain(-diff, IFluidHandler.FluidAction.EXECUTE);
                tank.fill(moved, IFluidHandler.FluidAction.EXECUTE);
            }
            // Refresh own after a transfer so the next neighbour sees the new amount.
            own = tank.getFluid();
            if (own.isEmpty()) {
                return;
            }
        }
    }

    /** Empty-handed drink: spend XP juice to fill the player's current experience bar. */
    public boolean drink(Player player) {
        if (level == null || level.isClientSide) {
            return false;
        }
        if (!XpFluidUtil.isXpJuice(tank.getFluid())) {
            return false;
        }
        int needed = XpFluidUtil.millibucketsToFillBar(player);
        FluidStack simulated = tank.drain(needed, IFluidHandler.FluidAction.SIMULATE);
        int xp = XpFluidUtil.toXp(simulated.getAmount());
        if (xp <= 0) {
            return false;
        }
        // Only pull millibuckets that convert to whole XP points (no silent residue destruction).
        int actualMb = XpFluidUtil.toMillibuckets(xp);
        FluidStack drained = tank.drain(actualMb, IFluidHandler.FluidAction.EXECUTE);
        int gained = XpFluidUtil.toXp(drained.getAmount());
        if (gained <= 0) {
            return false;
        }
        player.giveExperiencePoints(gained);
        return true;
    }

    public int comparatorSignal() {
        if (tank.isEmpty()) {
            return 0;
        }
        return Math.max(1, Mth.floor(tank.getFluidAmount() / (float) Math.max(1, tank.getCapacity()) * 15.0F));
    }

    public float fillRatio() {
        return tank.getFluidAmount() / (float) Math.max(1, tank.getCapacity());
    }

    /**
     * Totals fluid amount and capacity across every tank reachable through shared faces
     * (same rules as render merging / equalise). Empty neighbours still join the network.
     */
    public NetworkContents networkContents() {
        if (level == null) {
            FluidStack own = tank.getFluid().copy();
            return new NetworkContents(own, tank.getCapacity(), 1);
        }
        Set<BlockPos> seen = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(worldPosition);
        seen.add(worldPosition);

        FluidStack representative = FluidStack.EMPTY;
        long amount = 0L;
        long capacity = 0L;
        int count = 0;

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            if (!(level.getBlockEntity(pos) instanceof TankBlockEntity other)) {
                continue;
            }
            count++;
            capacity += other.tank.getCapacity();
            FluidStack fluid = other.tank.getFluid();
            if (!fluid.isEmpty()) {
                if (representative.isEmpty()) {
                    representative = fluid.copy();
                    amount = fluid.getAmount();
                } else if (FluidStack.isSameFluidSameComponents(representative, fluid)) {
                    amount += fluid.getAmount();
                }
            }
            for (Direction side : Direction.values()) {
                BlockPos next = pos.relative(side);
                if (!seen.add(next)) {
                    continue;
                }
                if (level.getBlockEntity(next) instanceof TankBlockEntity) {
                    queue.add(next);
                }
            }
        }

        FluidStack total = FluidStack.EMPTY;
        if (!representative.isEmpty()) {
            total = representative.copy();
            total.setAmount((int) Math.min(Integer.MAX_VALUE, amount));
        }
        return new NetworkContents(total, (int) Math.min(Integer.MAX_VALUE, capacity), count);
    }

    public record NetworkContents(FluidStack fluid, int capacityMb, int tankCount) {}

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Tank")) {
            tank.readFromNBT(registries, tag.getCompound("Tank"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        SimpleFluidContent content = components.getOrDefault(OCDataComponents.TANK_FLUID.get(), SimpleFluidContent.EMPTY);
        tank.setFluid(content.copy());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (!tank.isEmpty()) {
            builder.set(OCDataComponents.TANK_FLUID.get(), SimpleFluidContent.copyOf(tank.getFluid()));
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove("Tank");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
