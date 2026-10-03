package dev.opencubes.content.trophy.behavior;

import dev.opencubes.content.trophy.TrophyBehavior;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import dev.opencubes.content.trophy.TrophyDefinition;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public final class RabbitTrophyBehavior implements TrophyBehavior {

    @Override
    public int onActivate(TrophyBlockEntity trophy, Player player, TrophyDefinition definition) {
        if (trophy.getLevel() != null) {
            trophy.getLevel().playSound(null, trophy.getBlockPos(), SoundEvents.RABBIT_AMBIENT,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 0));
        return 80;
    }
}
