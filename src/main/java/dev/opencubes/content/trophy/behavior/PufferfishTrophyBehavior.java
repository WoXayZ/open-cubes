package dev.opencubes.content.trophy.behavior;

import dev.opencubes.content.trophy.TrophyBehavior;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import dev.opencubes.content.trophy.TrophyDefinition;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/** Applies the pufferfish sting effects for 3 seconds. */
public final class PufferfishTrophyBehavior implements TrophyBehavior {

    private static final int DURATION = 60;

    @Override
    public int onActivate(TrophyBlockEntity trophy, Player player, TrophyDefinition definition) {
        player.addEffect(new MobEffectInstance(MobEffects.POISON, DURATION, 1));
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, DURATION, 2));
        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, DURATION, 0));
        if (trophy.getLevel() != null) {
            trophy.getLevel().playSound(null, trophy.getBlockPos(), SoundEvents.PUFFER_FISH_STING,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return 100;
    }
}
