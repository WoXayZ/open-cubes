package dev.opencubes.content.flight;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.opencubes.registry.OCDataComponents;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleSmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.TransmuteRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;

/**
 * Smithing transform that also carries the paint colour of the addition over, so a painted hang
 * glider gives a thermal elytra of the same colour. Vanilla only copies the base's components.
 */
public class PaintedSmithingRecipe extends SimpleSmithingRecipe {

    public static final MapCodec<PaintedSmithingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
            Ingredient.CODEC.fieldOf("template").forGetter(recipe -> recipe.template),
            Ingredient.CODEC.fieldOf("base").forGetter(recipe -> recipe.base),
            Ingredient.CODEC.fieldOf("addition").forGetter(recipe -> recipe.addition),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
    ).apply(instance, PaintedSmithingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PaintedSmithingRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
            Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.template,
            Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.base,
            Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.addition,
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
            PaintedSmithingRecipe::new);

    public static final RecipeSerializer<PaintedSmithingRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final ItemStackTemplate result;

    public PaintedSmithingRecipe(Recipe.CommonInfo commonInfo, Ingredient template, Ingredient base,
                                 Ingredient addition, ItemStackTemplate result) {
        super(commonInfo);
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input) {
        ItemStack stack = TransmuteRecipe.createWithOriginalComponents(this.result, input.base());
        Integer colour = input.addition().get(OCDataComponents.PAINT_COLOR.get());
        if (colour != null) {
            stack.set(OCDataComponents.PAINT_COLOR.get(), colour);
        }
        return stack;
    }

    @Override
    public Optional<Ingredient> templateIngredient() {
        return Optional.of(this.template);
    }

    @Override
    public Ingredient baseIngredient() {
        return this.base;
    }

    @Override
    public Optional<Ingredient> additionIngredient() {
        return Optional.of(this.addition);
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.createFromOptionals(List.of(Optional.of(this.template), Optional.of(this.base), Optional.of(this.addition)));
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new SmithingRecipeDisplay(
                Ingredient.optionalIngredientToDisplay(Optional.of(this.template)),
                this.base.display(),
                Ingredient.optionalIngredientToDisplay(Optional.of(this.addition)),
                new SlotDisplay.ItemStackSlotDisplay(this.result),
                new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)));
    }

    @Override
    public RecipeSerializer<PaintedSmithingRecipe> getSerializer() {
        return SERIALIZER;
    }
}
