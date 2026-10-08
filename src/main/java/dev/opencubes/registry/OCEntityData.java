package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Synced data types that used to live on the vanilla serializer list. */
public final class OCEntityData {

    public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID =
            EntityDataSerializer.forValueType(ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC));

    private static final DeferredRegister<EntityDataSerializer<?>> REGISTRY =
            DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, OCConstants.MOD_ID);

    static {
        REGISTRY.register("optional_uuid", () -> OPTIONAL_UUID);
    }

    private OCEntityData() {}

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
