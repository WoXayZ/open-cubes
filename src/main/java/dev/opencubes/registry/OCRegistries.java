package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import dev.opencubes.content.trophy.TrophyBehavior;
import dev.opencubes.content.trophy.TrophyDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = OCConstants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class OCRegistries {

    public static final ResourceKey<Registry<TrophyDefinition>> TROPHY =
            ResourceKey.createRegistryKey(OCConstants.id("trophy"));

    public static final ResourceKey<Registry<TrophyBehavior>> TROPHY_BEHAVIOR =
            ResourceKey.createRegistryKey(OCConstants.id("trophy_behavior"));

    /** Populated in {@link NewRegistryEvent}. */
    public static Registry<TrophyBehavior> TROPHY_BEHAVIORS;

    private OCRegistries() {}

    @SubscribeEvent
    public static void newRegistries(NewRegistryEvent event) {
        TROPHY_BEHAVIORS = event.create(new RegistryBuilder<>(TROPHY_BEHAVIOR));
    }

    @SubscribeEvent
    public static void dataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(TROPHY, TrophyDefinition.CODEC, TrophyDefinition.CODEC);
    }
}
