package dev.opencubes.content.imaginary;

import com.mojang.serialization.MapCodec;
import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.registry.OCRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.jetbrains.annotations.Nullable;

/**
 * A shapeless recipe whose result takes the colour of its ingredients: the paint colour of a
 * coloured item (crayon into crayon glasses), else the colour of a dye (pencil into crayon).
 */
public class ColouredShapelessRecipe extends ShapelessRecipe {

    public ColouredShapelessRecipe(ShapelessRecipe base) {
        super(base.getGroup(), base.category(), base.getResultItem(RegistryAccess.EMPTY).copy(), base.getIngredients());
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack result = super.assemble(input, registries);
        Integer colour = colourOf(input);
        if (colour != null) {
            result.set(OCDataComponents.PAINT_COLOR.get(), colour & 0xFFFFFF);
        }
        return result;
    }

    @Nullable
    private static Integer colourOf(CraftingInput input) {
        for (int i = 0; i < input.size(); i++) {
            Integer colour = input.getItem(i).get(OCDataComponents.PAINT_COLOR.get());
            if (colour != null) {
                return colour;
            }
        }
        for (int i = 0; i < input.size(); i++) {
            DyeColor dye = DyeColor.getColor(input.getItem(i));
            if (dye != null) {
                return dye.getTextureDiffuseColor();
            }
        }
        return null;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return OCRecipeSerializers.COLOURED_SHAPELESS.get();
    }

    public static final class Serializer implements RecipeSerializer<ColouredShapelessRecipe> {

        private static final MapCodec<ColouredShapelessRecipe> CODEC =
                RecipeSerializer.SHAPELESS_RECIPE.codec().xmap(ColouredShapelessRecipe::new, recipe -> recipe);
        private static final StreamCodec<RegistryFriendlyByteBuf, ColouredShapelessRecipe> STREAM_CODEC =
                RecipeSerializer.SHAPELESS_RECIPE.streamCodec().map(ColouredShapelessRecipe::new, recipe -> recipe);

        @Override
        public MapCodec<ColouredShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ColouredShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
