package dev.opencubes.registry;

import com.mojang.serialization.Codec;
import dev.opencubes.OCConstants;
import dev.opencubes.content.flight.GliderState;
import dev.opencubes.content.tomfoolery.LuckState;
import dev.opencubes.content.tools.PedometerState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class OCAttachments {

    private static final DeferredRegister<AttachmentType<?>> REGISTRY =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, OCConstants.MOD_ID);

    /** Client-side pedometer session; not serialized. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PedometerState>> PEDOMETER =
            REGISTRY.register("pedometer", () -> AttachmentType.builder(PedometerState::new).build());

    /** Hang-glider engagement + last vertical speed for the variometer. Not serialized. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<GliderState>> GLIDER =
            REGISTRY.register("glider", () -> AttachmentType.builder(GliderState::new).build());

    /** Flim-flam luck. Persists across deaths. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<LuckState>> LUCK =
            REGISTRY.register("luck", () -> AttachmentType.builder(LuckState::new)
                    .serialize(LuckState.CODEC)
                    .copyOnDeath()
                    .build());

    /** Bricks owed after tasty clay. Cleared on death drop. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> BOWEL =
            REGISTRY.register("bowel", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build());

    private OCAttachments() {}

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
