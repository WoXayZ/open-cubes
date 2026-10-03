package dev.opencubes.content.trophy.behavior;

import dev.opencubes.content.trophy.TrophyBehavior;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import dev.opencubes.content.trophy.TrophyDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;

public final class SnowmanTrophyBehavior implements TrophyBehavior {

    @Override
    public int onActivate(TrophyBlockEntity trophy, Player player, TrophyDefinition definition) {
        Level level = trophy.getLevel();
        if (level == null) {
            return 0;
        }
        BlockPos base = trophy.getBlockPos();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = base.offset(x, 0, z);
                if (level.getBlockState(pos).isAir()
                        && Blocks.SNOW.defaultBlockState().canSurvive(level, pos)) {
                    int layers = 1 + level.random.nextInt(SnowLayerBlock.MAX_HEIGHT);
                    level.setBlock(pos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers), 3);
                }
            }
        }
        return 10;
    }
}
