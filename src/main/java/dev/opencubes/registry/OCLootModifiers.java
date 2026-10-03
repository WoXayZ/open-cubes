package dev.opencubes.registry;

import com.mojang.serialization.MapCodec;
import dev.opencubes.OCConstants;
import dev.opencubes.loot.AddItemLootModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class OCLootModifiers {

    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REGISTRY =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, OCConstants.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AddItemLootModifier>> ADD_ITEM =
            REGISTRY.register("add_item", () -> AddItemLootModifier.CODEC);

    private OCLootModifiers() {}

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
