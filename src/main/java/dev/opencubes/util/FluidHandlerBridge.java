package dev.opencubes.util;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Both directions between {@link IFluidHandler} and {@link ResourceHandler}. */
public final class FluidHandlerBridge {

    private FluidHandlerBridge() {}

    public static ResourceHandler<FluidResource> asResource(IFluidHandler fluids) {
        return new ResourceView(fluids);
    }

    public static IFluidHandler asTanks(ResourceHandler<FluidResource> resources) {
        return new TankView(resources);
    }

    /**
     * Bucket clicks open a transaction, try the move, then abort it when the item swap fails.
     * Executing the tank change immediately made one bucket count as two and left the bucket empty.
     */
    private static final class ResourceView extends SnapshotJournal<List<FluidStack>> implements ResourceHandler<FluidResource> {
        private final IFluidHandler fluids;

        private ResourceView(IFluidHandler fluids) {
            this.fluids = fluids;
        }

        private void snapshot(TransactionContext transaction) {
            if (transaction != null) {
                updateSnapshots(transaction);
            }
        }

        @Override
        protected List<FluidStack> createSnapshot() {
            List<FluidStack> copy = new ArrayList<>(fluids.getTanks());
            for (int i = 0; i < fluids.getTanks(); i++) {
                copy.add(fluids.getFluidInTank(i).copy());
            }
            return copy;
        }

        @Override
        protected void revertToSnapshot(List<FluidStack> snapshot) {
            for (int i = 0; i < snapshot.size() && i < fluids.getTanks(); i++) {
                FluidStack current = fluids.getFluidInTank(i);
                if (!current.isEmpty()) {
                    fluids.drain(current, IFluidHandler.FluidAction.EXECUTE);
                }
                FluidStack restored = snapshot.get(i);
                if (!restored.isEmpty()) {
                    fluids.fill(restored, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }

        @Override
        public int size() {
            return fluids.getTanks();
        }

        @Override
        public FluidResource getResource(int index) {
            return FluidResource.of(fluids.getFluidInTank(index));
        }

        @Override
        public long getAmountAsLong(int index) {
            return fluids.getFluidInTank(index).getAmount();
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            return fluids.getTankCapacity(index);
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return fluids.isFluidValid(index, resource.toStack(1));
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (amount <= 0 || resource.isEmpty()) {
                return 0;
            }
            snapshot(transaction);
            return fluids.fill(resource.toStack(amount), IFluidHandler.FluidAction.EXECUTE);
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (amount <= 0 || resource.isEmpty()) {
                return 0;
            }
            snapshot(transaction);
            FluidStack drained = fluids.drain(resource.toStack(amount), IFluidHandler.FluidAction.SIMULATE);
            if (drained.isEmpty() || !FluidResource.of(drained).equals(resource)) {
                return 0;
            }
            return fluids.drain(resource.toStack(amount), IFluidHandler.FluidAction.EXECUTE).getAmount();
        }
    }

    private static final class TankView implements IFluidHandler {
        private final ResourceHandler<FluidResource> resources;

        private TankView(ResourceHandler<FluidResource> resources) {
            this.resources = resources;
        }

        @Override
        public int getTanks() {
            return resources.size();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            FluidResource resource = resources.getResource(tank);
            int amount = resources.getAmountAsInt(tank);
            return resource.isEmpty() || amount <= 0 ? FluidStack.EMPTY : resource.toStack(amount);
        }

        @Override
        public int getTankCapacity(int tank) {
            return resources.getCapacityAsInt(tank, resources.getResource(tank));
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return stack.isEmpty() || resources.isValid(tank, FluidResource.of(stack));
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return 0;
            }
            FluidResource fluid = FluidResource.of(resource);
            int room = 0;
            for (int i = 0; i < resources.size(); i++) {
                FluidResource current = resources.getResource(i);
                if (!current.isEmpty() && !current.equals(fluid)) {
                    continue;
                }
                room += Math.max(0, resources.getCapacityAsInt(i, fluid) - resources.getAmountAsInt(i));
            }
            int filled = Math.min(room, resource.getAmount());
            if (action.execute() && filled > 0) {
                int left = filled;
                for (int i = 0; i < resources.size() && left > 0; i++) {
                    left -= resources.insert(i, fluid, left, null);
                }
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            return drain(FluidResource.of(resource), resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            for (int i = 0; i < resources.size(); i++) {
                FluidResource resource = resources.getResource(i);
                if (!resource.isEmpty() && resources.getAmountAsLong(i) > 0) {
                    return drain(resource, maxDrain, action);
                }
            }
            return FluidStack.EMPTY;
        }

        private FluidStack drain(FluidResource resource, int maxDrain, FluidAction action) {
            int available = 0;
            for (int i = 0; i < resources.size(); i++) {
                if (resources.getResource(i).equals(resource)) {
                    available += resources.getAmountAsInt(i);
                }
            }
            int drained = Math.min(maxDrain, available);
            if (action.execute() && drained > 0) {
                int left = drained;
                for (int i = 0; i < resources.size() && left > 0; i++) {
                    left -= resources.extract(i, resource, left, null);
                }
            }
            return drained <= 0 ? FluidStack.EMPTY : resource.toStack(drained);
        }
    }
}
