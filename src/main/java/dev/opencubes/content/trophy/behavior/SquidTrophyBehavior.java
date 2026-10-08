package dev.opencubes.content.trophy.behavior;

import dev.opencubes.content.trophy.TrophyBehavior;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import dev.opencubes.content.trophy.TrophyDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public final class SquidTrophyBehavior implements TrophyBehavior {

    @Override
    public int onActivate(TrophyBlockEntity trophy, Player player, TrophyDefinition definition) {
        Level level = trophy.getLevel();
        if (level == null) {
            return 0;
        }
        BlockPos above = trophy.getBlockPos().above();
        if (level.dimension() == Level.NETHER) {
            level.playSound(null, above, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F);
        } else if (level.getBlockState(above).isAir()) {
            level.setBlock(above, Blocks.WATER.defaultBlockState(), 3);
        }
        return 10;
    }
}
