package dev.opencubes.content.xp;

import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.util.ExperienceUtil;
import dev.opencubes.util.XpFluidUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Converts player experience and XP orbs standing on it into liquid XP in the fluid handler
 * directly below. Sneaking doubles the drain rate.
 */
public class XpDrainBlockEntity extends BlockEntity {

    private static final int BASE_XP_PER_TICK = 4;

    public XpDrainBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.XP_DRAIN.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, XpDrainBlockEntity drain) {
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos.below(), Direction.UP);
        if (handler == null) {
            return;
        }

        AABB feet = new AABB(pos).setMaxY(pos.getY() + 0.3D);
        List<ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class, feet);
        // Slight inflate so standing / sneaking feet still count on the 1px-tall plate.
        List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(0.05D, 0.0D, 0.05D));

        if (orbs.isEmpty() && players.isEmpty()) {
            return;
        }

        for (ExperienceOrb orb : orbs) {
            tryConsumeOrb(handler, orb);
        }
        for (Player player : players) {
            tryDrainPlayer(level, pos, handler, player);
        }
    }

    private static void tryConsumeOrb(IFluidHandler tank, ExperienceOrb orb) {
        if (!orb.isAlive()) {
            return;
        }
        FluidStack juice = XpFluidUtil.juice(XpFluidUtil.toMillibuckets(orb.getValue()));
        if (juice.isEmpty()) {
            return;
        }
        int filled = tank.fill(juice, IFluidHandler.FluidAction.SIMULATE);
        if (filled == juice.getAmount()) {
            tank.fill(juice, IFluidHandler.FluidAction.EXECUTE);
            orb.discard();
        }
    }

    private static void tryDrainPlayer(Level level, BlockPos pos, IFluidHandler tank, Player player) {
        int playerXp = ExperienceUtil.totalExperience(player);
        if (playerXp <= 0) {
            return;
        }

        int rate = player.isShiftKeyDown() ? BASE_XP_PER_TICK * 2 : BASE_XP_PER_TICK;
        int maxXp = Math.min(rate, playerXp);
        int wantedMb = XpFluidUtil.toMillibuckets(maxXp);
        int acceptedMb = tank.fill(XpFluidUtil.juice(wantedMb), IFluidHandler.FluidAction.SIMULATE);
        int acceptedXp = XpFluidUtil.toXp(acceptedMb);
        if (acceptedXp <= 0) {
            return;
        }

        int finallyMb = tank.fill(XpFluidUtil.juice(XpFluidUtil.toMillibuckets(acceptedXp)),
                IFluidHandler.FluidAction.EXECUTE);
        int finallyXp = XpFluidUtil.toXp(finallyMb);
        if (finallyXp <= 0) {
            return;
        }

        ExperienceUtil.consume(player, finallyXp);
        if (level.getGameTime() % 4 == 0) {
            level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS,
                    0.1F, 0.5F * ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.8F));
        }
    }
}
