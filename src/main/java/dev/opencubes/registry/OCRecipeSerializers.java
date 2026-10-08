package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import dev.opencubes.content.flight.PaintedSmithingRecipe;
import dev.opencubes.content.goldeneye.GoldenEyeRechargeRecipe;
import dev.opencubes.content.heightmap.MapCloneRecipe;
import dev.opencubes.content.heightmap.MapResizeRecipe;
import dev.opencubes.content.imaginary.ColouredShapelessRecipe;
import dev.opencubes.content.tomfoolery.EpicEraserRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class OCRecipeSerializers {

    private static final DeferredRegister<RecipeSerializer<?>> REGISTRY =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, OCConstants.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GoldenEyeRechargeRecipe>> GOLDEN_EYE_RECHARGE =
            REGISTRY.register("golden_eye_recharge", () -> GoldenEyeRechargeRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EpicEraserRecipe>> EPIC_ERASER =
            REGISTRY.register("epic_eraser", () -> EpicEraserRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MapCloneRecipe>> MAP_CLONE =
            REGISTRY.register("map_clone", () -> MapCloneRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MapResizeRecipe>> MAP_RESIZE =
            REGISTRY.register("map_resize", () -> MapResizeRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ColouredShapelessRecipe>> COLOURED_SHAPELESS =
            REGISTRY.register("coloured_shapeless", () -> ColouredShapelessRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PaintedSmithingRecipe>> PAINTED_SMITHING =
            REGISTRY.register("painted_smithing_transform", () -> PaintedSmithingRecipe.SERIALIZER);

    private OCRecipeSerializers() {}

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
