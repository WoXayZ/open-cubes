package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import dev.opencubes.advancement.BrickDroppedTrigger;
import dev.opencubes.advancement.StackOverflowTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class OCCriteria {

    private static final DeferredRegister<CriterionTrigger<?>> REGISTRY =
            DeferredRegister.create(Registries.TRIGGER_TYPE, OCConstants.MOD_ID);

    public static final DeferredHolder<CriterionTrigger<?>, BrickDroppedTrigger> BRICK_DROPPED =
            REGISTRY.register("brick_dropped", BrickDroppedTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, StackOverflowTrigger> STACK_OVERFLOW =
            REGISTRY.register("stack_overflow", StackOverflowTrigger::new);

    private OCCriteria() {}

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
