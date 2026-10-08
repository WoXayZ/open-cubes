package dev.opencubes.content.heightmap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.opencubes.registry.OCItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Shapeless: filled height map + empty map at the same scale → two copies of the filled map. */
public class MapCloneRecipe extends CustomRecipe {

    public static final MapCloneRecipe INSTANCE = new MapCloneRecipe();
    public static final MapCodec<MapCloneRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC)
                    .forGetter(recipe -> CraftingBookCategory.MISC)
    ).apply(instance, category -> INSTANCE));
    public static final StreamCodec<RegistryFriendlyByteBuf, MapCloneRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<MapCloneRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private MapCloneRecipe() {}

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack heightMap = ItemStack.EMPTY;
        ItemStack emptyMap = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(OCItems.HEIGHT_MAP.get())) {
                if (!heightMap.isEmpty()) {
                    return false;
                }
                heightMap = stack;
            } else if (stack.is(OCItems.EMPTY_MAP.get())) {
                if (!emptyMap.isEmpty()) {
                    return false;
                }
                emptyMap = stack;
            } else {
                return false;
            }
        }

        if (heightMap.isEmpty() || emptyMap.isEmpty()) {
            return false;
        }

        int mapId = HeightMapItem.getMapId(heightMap);
        if (mapId < 0) {
            return false;
        }
        HeightMapData data = HeightMapManager.getMapData(level, mapId);
        if (!data.isValid()) {
            return false;
        }
        return EmptyMapItem.getScale(emptyMap) == data.scale;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(OCItems.HEIGHT_MAP.get())) {
                ItemStack result = HeightMapItem.create(HeightMapItem.getMapId(stack));
                result.setCount(2);
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }
}
