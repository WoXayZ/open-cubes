package dev.opencubes.content.heightmap;

import dev.opencubes.registry.OCItems;
import dev.opencubes.registry.OCRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Shapeless: empty map + exactly four gold nuggets → empty map at the next scale. */
public class MapResizeRecipe extends CustomRecipe {

    public MapResizeRecipe(CraftingBookCategory category) {
        super(category);
    }

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
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(OCItems.EMPTY_MAP.get())) {
                return EmptyMapItem.create(EmptyMapItem.getScale(stack) + 1);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return OCRecipeSerializers.MAP_RESIZE.get();
    }
}
