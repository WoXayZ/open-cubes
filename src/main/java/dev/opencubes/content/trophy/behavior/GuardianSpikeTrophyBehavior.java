package dev.opencubes.content.trophy.behavior;

import dev.opencubes.content.trophy.TrophyBehavior;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import dev.opencubes.content.trophy.TrophyDefinition;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/** Regular guardian: a short spike jab. */
public final class GuardianSpikeTrophyBehavior implements TrophyBehavior {

    @Override
    public int onActivate(TrophyBlockEntity trophy, Player player, TrophyDefinition definition) {
        if (trophy.getLevel() == null) {
            return 0;
        }
        player.hurt(player.damageSources().magic(), 1.0F);
        trophy.getLevel().playSound(null, trophy.getBlockPos(), SoundEvents.GUARDIAN_ATTACK,
                SoundSource.BLOCKS, 1.0F, 1.2F);
        return 40;
    }
}
