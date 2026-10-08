package dev.opencubes.content.imaginary;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.opencubes.registry.OCDataComponents;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * A shapeless recipe whose result takes the colour of its ingredients: the paint colour of a
 * coloured item (crayon into crayon glasses), else the colour of a dye (pencil into crayon).
 */
public class ColouredShapelessRecipe implements CraftingRecipe {

    public static final MapCodec<ColouredShapelessRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.resultTemplate),
            Codec.lazyInitialized(() -> Ingredient.CODEC.listOf(1, 9)).fieldOf("ingredients").forGetter(recipe -> recipe.ingredients)
    ).apply(instance, ColouredShapelessRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ColouredShapelessRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.resultTemplate,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), recipe -> recipe.ingredients,
            ColouredShapelessRecipe::new);

    public static final RecipeSerializer<ColouredShapelessRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Recipe.CommonInfo commonInfo;
    private final CraftingRecipe.CraftingBookInfo bookInfo;
    private final ItemStackTemplate resultTemplate;
    private final List<Ingredient> ingredients;
    private final ShapelessRecipe delegate;

    public ColouredShapelessRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                                   ItemStackTemplate resultTemplate, List<Ingredient> ingredients) {
        this.commonInfo = commonInfo;
        this.bookInfo = bookInfo;
        this.resultTemplate = resultTemplate;
        this.ingredients = ingredients;
        this.delegate = new ShapelessRecipe(commonInfo, bookInfo, resultTemplate, ingredients);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return this.delegate.matches(input, level);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack result = this.delegate.assemble(input);
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
    public String group() {
        return this.bookInfo.group();
    }

    @Override
    public boolean showNotification() {
        return this.commonInfo.showNotification();
    }

    @Override
    public net.minecraft.world.item.crafting.CraftingBookCategory category() {
        return this.bookInfo.category();
    }

    @Override
    public PlacementInfo placementInfo() {
        return this.delegate.placementInfo();
    }

    @Override
    public List<RecipeDisplay> display() {
        return this.delegate.display();
    }

    @Override
    public RecipeSerializer<ColouredShapelessRecipe> getSerializer() {
        return SERIALIZER;
    }
}
