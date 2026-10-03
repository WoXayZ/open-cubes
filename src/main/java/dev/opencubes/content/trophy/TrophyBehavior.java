package dev.opencubes.content.trophy;

import net.minecraft.world.entity.player.Player;

/**
 * Right-click / tick effect for a trophy. Registered under {@code opencubes:trophy_behavior}.
 */
@FunctionalInterface
public interface TrophyBehavior {

    /**
     * @return cooldown ticks before the next activate may run (0 = immediate)
     */
    int onActivate(TrophyBlockEntity trophy, Player player, TrophyDefinition definition);

    default void onTick(TrophyBlockEntity trophy, TrophyDefinition definition) {}
}
