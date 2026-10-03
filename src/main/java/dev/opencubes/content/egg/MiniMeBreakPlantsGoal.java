package dev.opencubes.content.egg;

import dev.opencubes.registry.OCTags;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Occasionally breaks nearby decorative blocks tagged as mini-me breakable. */
public class MiniMeBreakPlantsGoal extends Goal {

    private final MiniMeEntity miniMe;
    private final int tickOffset;
    @Nullable
    private BlockPos blockPos;

    public MiniMeBreakPlantsGoal(MiniMeEntity miniMe) {
        this.miniMe = miniMe;
        this.tickOffset = miniMe.getRandom().nextInt(10);
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!miniMe.getNavigation().isDone()) {
            return false;
        }
        if (miniMe.level().isClientSide) {
            return false;
        }
        if ((miniMe.tickCount + tickOffset) % 4 != 0) {
            return false;
        }
        for (int attempt = 0; attempt < 20; attempt++) {
            BlockPos candidate = miniMe.blockPosition().offset(
                    miniMe.getRandom().nextInt(16) - 8,
                    miniMe.getRandom().nextInt(3) - 1,
                    miniMe.getRandom().nextInt(16) - 8);
            if (canBreak(candidate)) {
                blockPos = candidate.immutable();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return miniMe.isAlive()
                && blockPos != null
                && canBreak(blockPos)
                && miniMe.distanceToSqr(Vec3.atCenterOf(blockPos)) < 16.0D;
    }

    @Override
    public void start() {
        if (blockPos != null) {
            miniMe.getNavigation().moveTo(
                    blockPos.getX() + 0.5D,
                    blockPos.getY(),
                    blockPos.getZ() + 0.5D,
                    1.0D);
        }
    }

    @Override
    public void stop() {
        miniMe.getNavigation().stop();
        blockPos = null;
    }

    @Override
    public void tick() {
        if (blockPos == null || !canBreak(blockPos)) {
            return;
        }
        if (miniMe.distanceToSqr(Vec3.atCenterOf(blockPos)) >= 1.0D) {
            miniMe.getNavigation().moveTo(
                    blockPos.getX() + 0.5D,
                    blockPos.getY(),
                    blockPos.getZ() + 0.5D,
                    1.0D);
            return;
        }
        miniMe.level().destroyBlock(blockPos, true);
        blockPos = null;
    }

    private boolean canBreak(BlockPos pos) {
        BlockState state = miniMe.level().getBlockState(pos);
        return state.is(OCTags.Blocks.MINI_ME_BREAKABLE);
    }
}
