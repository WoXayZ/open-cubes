package dev.opencubes.content.trophy.behavior;

import dev.opencubes.content.trophy.TrophyBehavior;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import dev.opencubes.content.trophy.TrophyDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;

public final class EndermanTrophyBehavior implements TrophyBehavior {

    @Override
    public int onActivate(TrophyBlockEntity trophy, Player player, TrophyDefinition definition) {
        Level level = trophy.getLevel();
        if (level == null) {
            return 0;
        }
        BlockPos pos = trophy.getBlockPos();
        ThrownEnderpearl pearl = new ThrownEnderpearl(level, player, new ItemStack(Items.ENDER_PEARL));
        pearl.setPos(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        pearl.setDeltaMovement(level.getRandom().nextGaussian(), 1.0D, level.getRandom().nextGaussian());
        level.addFreshEntity(pearl);
        return 100;
    }
}
