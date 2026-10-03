package dev.opencubes.content.healer;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.util.XpFluidUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/** Survival healer: regenerates players standing on it while burning liquid XP. */
public class HealerBlockEntity extends BlockEntity {

    private final FluidTank tank = new FluidTank(4000, XpFluidUtil::isXpJuice) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    public HealerBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.HEALER.get(), pos, state);
    }

    public FluidTank tank() {
        return tank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HealerBlockEntity healer) {
        if (level.getGameTime() % 20 != 0) {
            return;
        }

        pullXp(level, pos, healer);

        int cost = OCCommonConfig.HEALER_XP_MB_PER_PULSE.get();
        FluidStack drained = healer.tank.drain(cost, IFluidHandler.FluidAction.SIMULATE);
        if (drained.getAmount() < cost) {
            return;
        }

        List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(pos).expandTowards(0, 1, 0));
        boolean healed = false;
        for (Player player : players) {
            if (player.getAbilities().instabuild) {
                continue;
            }
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 2, 0, true, false));
            healed = true;
        }
        if (healed) {
            healer.tank.drain(cost, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private static void pullXp(Level level, BlockPos pos, HealerBlockEntity healer) {
        if (healer.tank.getSpace() <= 0) {
            return;
        }
        for (Direction dir : Direction.values()) {
            IFluidHandler neighbor = level.getCapability(
                    Capabilities.FluidHandler.BLOCK, pos.relative(dir), dir.getOpposite());
            if (neighbor == null) {
                continue;
            }
            FluidStack drained = neighbor.drain(XpFluidUtil.juice(250), IFluidHandler.FluidAction.SIMULATE);
            if (!XpFluidUtil.isXpJuice(drained) || drained.isEmpty()) {
                continue;
            }
            int filled = healer.tank.fill(drained, IFluidHandler.FluidAction.SIMULATE);
            if (filled > 0) {
                FluidStack taken = neighbor.drain(XpFluidUtil.juice(filled), IFluidHandler.FluidAction.EXECUTE);
                healer.tank.fill(taken, IFluidHandler.FluidAction.EXECUTE);
                return;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Tank")) {
            tank.readFromNBT(registries, tag.getCompound("Tank"));
        }
    }
}
