package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import dev.opencubes.content.crane.MagnetEntity;
import dev.opencubes.content.crane.MountedBlockEntity;
import dev.opencubes.content.goldeneye.GoldenEyeEntity;
import dev.opencubes.content.heightmap.CartographerEntity;
import dev.opencubes.content.luggage.LuggageEntity;
import dev.opencubes.content.paint.GlyphEntity;
import dev.opencubes.content.egg.MiniMeEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class OCEntities {

    private static final DeferredRegister<EntityType<?>> REGISTRY =
            DeferredRegister.create(Registries.ENTITY_TYPE, OCConstants.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<LuggageEntity>> LUGGAGE =
            REGISTRY.register("luggage", () -> EntityType.Builder.<LuggageEntity>of(LuggageEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.6F)
                    .clientTrackingRange(10)
                    .build(OCConstants.id("luggage").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<GoldenEyeEntity>> GOLDEN_EYE =
            REGISTRY.register("golden_eye", () -> EntityType.Builder.<GoldenEyeEntity>of(GoldenEyeEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(OCConstants.id("golden_eye").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<GlyphEntity>> GLYPH =
            REGISTRY.register("glyph", () -> EntityType.Builder.<GlyphEntity>of(GlyphEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .build(OCConstants.id("glyph").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<CartographerEntity>> CARTOGRAPHER =
            REGISTRY.register("cartographer", () -> EntityType.Builder.<CartographerEntity>of(
                            CartographerEntity::new, MobCategory.MISC)
                    .sized(0.75F, 0.75F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .build(OCConstants.id("cartographer").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<MagnetEntity>> MAGNET =
            REGISTRY.register("magnet", () -> EntityType.Builder.<MagnetEntity>of(
                            MagnetEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build(OCConstants.id("magnet").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<MountedBlockEntity>> MOUNTED_BLOCK =
            REGISTRY.register("mounted_block", () -> EntityType.Builder.<MountedBlockEntity>of(
                            MountedBlockEntity::new, MobCategory.MISC)
                    .sized(0.925F, 0.925F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build(OCConstants.id("mounted_block").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<MiniMeEntity>> MINI_ME =
            REGISTRY.register("mini_me", () -> EntityType.Builder.<MiniMeEntity>of(
                            MiniMeEntity::new, MobCategory.CREATURE)
                    .sized(0.4F, 0.9F)
                    .clientTrackingRange(10)
                    .build(OCConstants.id("mini_me").toString()));

    private OCEntities() {}

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(LUGGAGE.get(), LuggageEntity.createAttributes().build());
        event.put(MINI_ME.get(), MiniMeEntity.createAttributes().build());
    }

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
