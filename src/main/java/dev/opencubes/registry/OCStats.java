package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class OCStats {

    public static final ResourceLocation BRICKS_DROPPED_ID = OCConstants.id("bricks_dropped");

    private static final DeferredRegister<ResourceLocation> CUSTOM_STATS =
            DeferredRegister.create(Registries.CUSTOM_STAT, OCConstants.MOD_ID);

    static {
        CUSTOM_STATS.register("bricks_dropped", () -> BRICKS_DROPPED_ID);
    }

    private OCStats() {}

    public static void register(IEventBus modBus) {
        CUSTOM_STATS.register(modBus);
    }

    /** Call after registries are ready so {@link Stats#CUSTOM} knows the formatter. */
    public static void bindFormatters() {
        Stats.CUSTOM.get(BRICKS_DROPPED_ID, StatFormatter.DEFAULT);
    }
}
