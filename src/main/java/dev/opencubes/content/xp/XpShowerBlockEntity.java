package dev.opencubes.content.xp;

import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.util.XpFluidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * When powered, pulls XP juice from the block behind it and drips it as no-fly experience orbs.
 */
public class XpShowerBlockEntity extends BlockEntity {

    private static final int BUFFER = 1000;
    private static final int PULL_PER_CYCLE = 100;
    private static final int ORB_EVERY_N_TICKS = 3;

    private final FluidTank buffer = new FluidTank(BUFFER, XpFluidUtil::isXpJuice);

    public XpShowerBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.XP_SHOWER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, XpShowerBlockEntity shower) {
        if (!state.getValue(XpShowerBlock.POWERED)) {
            return;
        }
        if (level.getGameTime() % ORB_EVERY_N_TICKS != 0) {
            return;
        }

        Direction back = state.getValue(XpShowerBlock.FACING).getOpposite();
        IFluidHandler source = level.getCapability(Capabilities.FluidHandler.BLOCK, pos.relative(back), back.getOpposite());
        if (source != null) {
            FluidStack drained = source.drain(PULL_PER_CYCLE, IFluidHandler.FluidAction.SIMULATE);
            if (XpFluidUtil.isXpJuice(drained)) {
                int filled = shower.buffer.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                if (filled > 0) {
                    source.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }

        FluidStack contents = shower.buffer.getFluid();
        if (!XpFluidUtil.isXpJuice(contents)) {
            return;
        }

        int xpInTank = XpFluidUtil.toXp(contents.getAmount());
        if (xpInTank <= 0) {
            return;
        }
        int xpInOrb = ExperienceOrb.getExperienceValue(xpInTank);
        int toDrain = XpFluidUtil.toMillibuckets(xpInOrb);
        if (toDrain <= 0 || toDrain > contents.getAmount()) {
            return;
        }

        shower.buffer.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);

        Direction facing = state.getValue(XpShowerBlock.FACING);
        // Nozzle is the open end (away from the wall). Throw along facing so east/west
        // do not collapse onto north after a collision with the pipe.
        double x = pos.getX() + 0.5D + facing.getStepX() * 0.35D;
        double y = pos.getY() + 0.12D;
        double z = pos.getZ() + 0.5D + facing.getStepZ() * 0.35D;
        double speed = 0.22D;
        XpOrbNoFly.spawn(level, x, y, z, xpInOrb,
                new Vec3(facing.getStepX() * speed, -0.06D, facing.getStepZ() * speed));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Buffer")) {
            buffer.readFromNBT(registries, tag.getCompound("Buffer"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Buffer", buffer.writeToNBT(registries, new CompoundTag()));
    }
}
