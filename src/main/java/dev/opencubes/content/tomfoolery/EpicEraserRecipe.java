package dev.opencubes.content.tomfoolery;

import dev.opencubes.registry.OCRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Shapeless: Epic Eraser + any item with epic lore → cleaned item; eraser takes durability.
 */
public class EpicEraserRecipe extends CustomRecipe {

    public EpicEraserRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack eraser = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof EpicEraserItem) {
                if (!eraser.isEmpty()) {
                    return false;
                }
                eraser = stack;
            } else {
                if (!target.isEmpty()) {
                    return false;
                }
                target = stack;
            }
        }
        return !eraser.isEmpty() && !target.isEmpty() && EpicEraserItem.hasEpicLore(target);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack target = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && !(stack.getItem() instanceof EpicEraserItem)) {
                target = stack.copy();
                break;
            }
        }
        if (target.isEmpty()) {
            return ItemStack.EMPTY;
        }
        EpicEraserItem.stripEpicLore(target);
        return target;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof EpicEraserItem) {
                ItemStack copy = stack.copy();
                copy.setDamageValue(copy.getDamageValue() + 1);
                if (copy.getDamageValue() < copy.getMaxDamage()) {
                    remaining.set(i, copy);
                }
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return OCRecipeSerializers.EPIC_ERASER.get();
    }
}
