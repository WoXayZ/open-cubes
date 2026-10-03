package dev.opencubes.content.trophy.behavior;

import dev.opencubes.content.trophy.TrophyBehavior;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import dev.opencubes.content.trophy.TrophyDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

public final class SkeletonTrophyBehavior implements TrophyBehavior {

    @Override
    public int onActivate(TrophyBlockEntity trophy, Player player, TrophyDefinition definition) {
        Level level = trophy.getLevel();
        if (level == null) {
            return 0;
        }
        BlockPos pos = trophy.getBlockPos();
        Arrow arrow = new Arrow(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, player.getProjectile(player.getMainHandItem()), null);
        arrow.setBaseDamage(0.1D);
        arrow.shoot(level.random.nextInt(10) - 5, 40, level.random.nextInt(10) - 5, 1.0F, 6.0F);
        level.playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 1.0F,
                1.0F / (level.random.nextFloat() * 0.4F + 1.2F) + 0.5F);
        level.addFreshEntity(arrow);
        return 0;
    }
}
