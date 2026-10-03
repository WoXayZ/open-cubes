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
import net.minecraft.world.level.block.state.BlockState;

/**
 * Plants brown mushrooms on replaceable blocks around the pedestal (same Y). Always applies
 * cooldown so the click never feels dead when the area is already full.
 */
public final class MooshroomTrophyBehavior implements TrophyBehavior {

    @Override
    public int onActivate(TrophyBlockEntity trophy, Player player, TrophyDefinition definition) {
        Level level = trophy.getLevel();
        if (level == null) {
            return 0;
        }
        BlockPos base = trophy.getBlockPos();
        BlockState mushroom = Blocks.BROWN_MUSHROOM.defaultBlockState();
        int placed = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }
                BlockPos pos = base.offset(x, 0, z);
                if (!level.getBlockState(pos).canBeReplaced()) {
                    continue;
                }
                level.setBlock(pos, mushroom, 3);
                placed++;
            }
        }
        level.playSound(null, base,
                placed > 0 ? SoundEvents.MOOSHROOM_SHEAR : SoundEvents.MOOSHROOM_MILK,
                SoundSource.BLOCKS, 1.0F, 1.0F);
        return 100;
    }
}
