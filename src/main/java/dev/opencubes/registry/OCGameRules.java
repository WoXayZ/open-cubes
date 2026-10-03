package dev.opencubes.registry;

import net.minecraft.world.level.GameRules;

/**
 * World-level toggles that map owners expect (gamerule command / datapacks).
 * Only {@code opencubes:spawn_graves} is registered - mirroring OpenBlocks - rather than
 * duplicating every toml switch.
 */
public final class OCGameRules {

    public static final GameRules.Key<GameRules.BooleanValue> SPAWN_GRAVES =
            GameRules.register("opencubes:spawn_graves", GameRules.Category.PLAYER,
                    GameRules.BooleanValue.create(true));

    private OCGameRules() {}

    /** Force class init so the rule is registered during mod construction. */
    public static void bootstrap() {}
}
