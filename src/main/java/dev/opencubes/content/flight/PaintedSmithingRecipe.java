package dev.opencubes.content.flight;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.registry.OCRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

/**
 * Smithing transform that also carries the paint colour of the addition over, so a painted hang
 * glider gives a thermal elytra of the same colour. Vanilla only copies the base's components.
 */
public class PaintedSmithingRecipe extends SmithingTransformRecipe {

    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final ItemStack result;

    public PaintedSmithingRecipe(Ingredient template, Ingredient base, Ingredient addition, ItemStack result) {
        super(template, base, addition, result);
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        ItemStack stack = super.assemble(input, registries);
        Integer colour = input.addition().get(OCDataComponents.PAINT_COLOR.get());
        if (colour != null) {
            stack.set(OCDataComponents.PAINT_COLOR.get(), colour);
        }
        return stack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return OCRecipeSerializers.PAINTED_SMITHING.get();
    }

    public static final class Serializer implements RecipeSerializer<PaintedSmithingRecipe> {

        private static final MapCodec<PaintedSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("template").forGetter(recipe -> recipe.template),
                Ingredient.CODEC.fieldOf("base").forGetter(recipe -> recipe.base),
                Ingredient.CODEC.fieldOf("addition").forGetter(recipe -> recipe.addition),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
        ).apply(instance, PaintedSmithingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, PaintedSmithingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.template,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.base,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.addition,
                ItemStack.STREAM_CODEC, recipe -> recipe.result,
                PaintedSmithingRecipe::new);

        @Override
        public MapCodec<PaintedSmithingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PaintedSmithingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
