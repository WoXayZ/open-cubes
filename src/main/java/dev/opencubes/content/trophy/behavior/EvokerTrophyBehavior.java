package dev.opencubes.content.trophy.behavior;

import dev.opencubes.content.trophy.TrophyBehavior;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import dev.opencubes.content.trophy.TrophyDefinition;
import net.minecraft.world.entity.player.Player;

public final class EvokerTrophyBehavior implements TrophyBehavior {

    @Override
    public int onActivate(TrophyBlockEntity trophy, Player player, TrophyDefinition definition) {
        // Same entity-status byte vanilla uses for the totem pop flourish.
        player.level().broadcastEntityEvent(player, (byte) 35);
        return 100;
    }
}
