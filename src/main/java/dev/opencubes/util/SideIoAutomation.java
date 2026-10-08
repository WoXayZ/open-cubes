package dev.opencubes.util;

import dev.opencubes.util.FluidHandlerBridge;
import dev.opencubes.util.ItemHandlerBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/** Neighbor pull/push helpers for machines with per-side I/O masks. */
public final class SideIoAutomation {

    private SideIoAutomation() {}

    public static void pullItems(Level level, BlockPos pos, int inputSides, IItemHandler insertHandler) {
        if (SideBitmask.isEmpty(inputSides)) {
            return;
        }
        Direction[] sides = Direction.values();
        int start = level.getRandom().nextInt(sides.length);
        for (int n = 0; n < sides.length; n++) {
            Direction side = sides[(start + n) % sides.length];
            if (!SideBitmask.has(inputSides, side)) {
                continue;
            }
            var found = level.getCapability(
                    Capabilities.Item.BLOCK, pos.relative(side), side.getOpposite());
            if (found == null) {
                continue;
            }
            IItemHandler neighbour = ItemHandlerBridge.asSlots(found);
            for (int slot = 0; slot < neighbour.getSlots(); slot++) {
                ItemStack stack = neighbour.extractItem(slot, 64, true);
                if (stack.isEmpty()) {
                    continue;
                }
                ItemStack leftover = ItemHandlerHelper.insertItem(insertHandler, stack, false);
                int moved = stack.getCount() - leftover.getCount();
                if (moved > 0) {
                    neighbour.extractItem(slot, moved, false);
                    return;
                }
            }
        }
    }

    public static void pushItems(Level level, BlockPos pos, int outputSides, IItemHandler extractHandler) {
        if (SideBitmask.isEmpty(outputSides)) {
            return;
        }
        Direction[] sides = Direction.values();
        int start = level.getRandom().nextInt(sides.length);
        for (int n = 0; n < sides.length; n++) {
            Direction side = sides[(start + n) % sides.length];
            if (!SideBitmask.has(outputSides, side)) {
                continue;
            }
            var found = level.getCapability(
                    Capabilities.Item.BLOCK, pos.relative(side), side.getOpposite());
            if (found == null) {
                continue;
            }
            IItemHandler neighbour = ItemHandlerBridge.asSlots(found);
            for (int slot = 0; slot < extractHandler.getSlots(); slot++) {
                ItemStack stack = extractHandler.extractItem(slot, 64, true);
                if (stack.isEmpty()) {
                    continue;
                }
                ItemStack leftover = ItemHandlerHelper.insertItem(neighbour, stack, false);
                if (leftover.getCount() < stack.getCount()) {
                    extractHandler.extractItem(slot, stack.getCount() - leftover.getCount(), false);
                    return;
                }
            }
        }
    }

    public static void pullXpFluid(Level level, BlockPos pos, int xpSides, FluidTank tank) {
        if (SideBitmask.isEmpty(xpSides) || tank.getSpace() <= 0) {
            return;
        }
        for (Direction side : Direction.values()) {
            if (!SideBitmask.has(xpSides, side)) {
                continue;
            }
            var found = level.getCapability(
                    Capabilities.Fluid.BLOCK, pos.relative(side), side.getOpposite());
            if (found == null) {
                continue;
            }
            IFluidHandler neighbour = FluidHandlerBridge.asTanks(found);
            FluidStack simulated = neighbour.drain(tank.getSpace(), IFluidHandler.FluidAction.SIMULATE);
            if (simulated.isEmpty() || !XpFluidUtil.isXpJuice(simulated)) {
                continue;
            }
            int filled = tank.fill(simulated, IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                neighbour.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                return;
            }
        }
    }
}
