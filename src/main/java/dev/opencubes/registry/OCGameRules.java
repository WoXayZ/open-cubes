package dev.opencubes.registry;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import dev.opencubes.OCConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * World-level toggles that map owners expect (gamerule command / datapacks).
 * Only {@code opencubes:spawn_graves} is registered - mirroring OpenBlocks - rather than
 * duplicating every toml switch. The game rule registry is frozen by the time the mod
 * constructor runs, so the rule is added from {@link RegisterEvent}.
 */
@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class OCGameRules {

    public static GameRule<Boolean> SPAWN_GRAVES;

    private OCGameRules() {}

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.GAME_RULE, OCConstants.id("spawn_graves"), () -> {
            SPAWN_GRAVES = new GameRule<>(
                    GameRuleCategory.PLAYER,
                    GameRuleType.BOOL,
                    BoolArgumentType.bool(),
                    GameRuleTypeVisitor::visitBoolean,
                    Codec.BOOL,
                    value -> value ? 1 : 0,
                    true,
                    FeatureFlagSet.of());
            return SPAWN_GRAVES;
        });
    }
}
