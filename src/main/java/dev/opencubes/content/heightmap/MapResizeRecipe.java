package dev.opencubes.content.heightmap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.opencubes.registry.OCItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Shapeless: empty map + exactly four gold nuggets → empty map at the next scale. */
public class MapResizeRecipe extends CustomRecipe {

    public static final MapResizeRecipe INSTANCE = new MapResizeRecipe();
    public static final MapCodec<MapResizeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC)
                    .forGetter(recipe -> CraftingBookCategory.MISC)
    ).apply(instance, category -> INSTANCE));
    public static final StreamCodec<RegistryFriendlyByteBuf, MapResizeRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<MapResizeRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private MapResizeRecipe() {}

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack emptyMap = ItemStack.EMPTY;
        int memory = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(OCItems.EMPTY_MAP.get())) {
                if (!emptyMap.isEmpty()) {
                    return false;
                }
                emptyMap = stack;
            } else if (stack.is(Items.GOLD_NUGGET)) {
                memory += stack.getCount();
            } else {
                return false;
            }
        }

        if (emptyMap.isEmpty() || memory != 4) {
            return false;
        }
        return EmptyMapItem.getScale(emptyMap) < EmptyMapItem.MAX_SCALE;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(OCItems.EMPTY_MAP.get())) {
                return EmptyMapItem.create(EmptyMapItem.getScale(stack) + 1);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }
}
