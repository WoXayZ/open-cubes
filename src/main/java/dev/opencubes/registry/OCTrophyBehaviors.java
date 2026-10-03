package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import dev.opencubes.content.trophy.TrophyBehavior;
import dev.opencubes.content.trophy.behavior.BlazeTrophyBehavior;
import dev.opencubes.content.trophy.behavior.BreezeTrophyBehavior;
import dev.opencubes.content.trophy.behavior.CaveSpiderTrophyBehavior;
import dev.opencubes.content.trophy.behavior.CreeperTrophyBehavior;
import dev.opencubes.content.trophy.behavior.EndermanTrophyBehavior;
import dev.opencubes.content.trophy.behavior.EvokerTrophyBehavior;
import dev.opencubes.content.trophy.behavior.GuardianSpikeTrophyBehavior;
import dev.opencubes.content.trophy.behavior.GuardianTrophyBehavior;
import dev.opencubes.content.trophy.behavior.IronGolemTrophyBehavior;
import dev.opencubes.content.trophy.behavior.ItemDropTrophyBehavior;
import dev.opencubes.content.trophy.behavior.LlamaTrophyBehavior;
import dev.opencubes.content.trophy.behavior.MooshroomTrophyBehavior;
import dev.opencubes.content.trophy.behavior.NoneTrophyBehavior;
import dev.opencubes.content.trophy.behavior.PufferfishTrophyBehavior;
import dev.opencubes.content.trophy.behavior.RabbitTrophyBehavior;
import dev.opencubes.content.trophy.behavior.ShulkerTrophyBehavior;
import dev.opencubes.content.trophy.behavior.SkeletonTrophyBehavior;
import dev.opencubes.content.trophy.behavior.SnowmanTrophyBehavior;
import dev.opencubes.content.trophy.behavior.SquidTrophyBehavior;
import dev.opencubes.content.trophy.behavior.VillagerTrophyBehavior;
import dev.opencubes.content.trophy.behavior.WitchTrophyBehavior;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class OCTrophyBehaviors {

    private static final DeferredRegister<TrophyBehavior> REGISTRY =
            DeferredRegister.create(OCRegistries.TROPHY_BEHAVIOR, OCConstants.MOD_ID);

    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> NONE =
            REGISTRY.register("none", NoneTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> ITEM_DROP =
            REGISTRY.register("item_drop", ItemDropTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> CREEPER =
            REGISTRY.register("creeper", CreeperTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> BLAZE =
            REGISTRY.register("blaze", BlazeTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> ENDERMAN =
            REGISTRY.register("enderman", EndermanTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> SKELETON =
            REGISTRY.register("skeleton", SkeletonTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> WITCH =
            REGISTRY.register("witch", WitchTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> SQUID =
            REGISTRY.register("squid", SquidTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> MOOSHROOM =
            REGISTRY.register("mooshroom", MooshroomTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> SNOWMAN =
            REGISTRY.register("snowman", SnowmanTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> CAVE_SPIDER =
            REGISTRY.register("cave_spider", CaveSpiderTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> SHULKER =
            REGISTRY.register("shulker", ShulkerTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> GUARDIAN =
            REGISTRY.register("guardian", GuardianTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> GUARDIAN_SPIKE =
            REGISTRY.register("guardian_spike", GuardianSpikeTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> LLAMA =
            REGISTRY.register("llama", LlamaTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> EVOKER =
            REGISTRY.register("evoker", EvokerTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> BREEZE =
            REGISTRY.register("breeze", BreezeTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> IRON_GOLEM =
            REGISTRY.register("iron_golem", IronGolemTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> PUFFERFISH =
            REGISTRY.register("pufferfish", PufferfishTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> RABBIT =
            REGISTRY.register("rabbit", RabbitTrophyBehavior::new);
    public static final DeferredHolder<TrophyBehavior, TrophyBehavior> VILLAGER =
            REGISTRY.register("villager", VillagerTrophyBehavior::new);

    private OCTrophyBehaviors() {}

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
