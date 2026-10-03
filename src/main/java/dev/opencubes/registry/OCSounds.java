package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class OCSounds {

    private static final DeferredRegister<SoundEvent> REGISTRY =
            DeferredRegister.create(Registries.SOUND_EVENT, OCConstants.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ELEVATOR_ACTIVATE = register("block.elevator.activate");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_TRAP_OPEN = register("block.bear_trap.open");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_TRAP_CLOSE = register("block.bear_trap.close");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOTTLER_DONE = register("block.bottler.done");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVE_ROB = register("block.grave.rob");
    public static final DeferredHolder<SoundEvent, SoundEvent> SLIMALYZER_PING = register("item.slimalyzer.ping");
    public static final DeferredHolder<SoundEvent, SoundEvent> PEDOMETER_USE = register("item.pedometer.use");

    private OCSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return REGISTRY.register(name, () -> SoundEvent.createVariableRangeEvent(OCConstants.id(name)));
    }

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
