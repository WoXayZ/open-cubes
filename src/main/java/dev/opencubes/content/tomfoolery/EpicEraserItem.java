package dev.opencubes.content.tomfoolery;

import dev.opencubes.registry.OCDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EpicEraserItem extends Item {

    public EpicEraserItem(Properties properties) {
        super(properties.stacksTo(1).durability(15));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public static boolean hasEpicLore(ItemStack stack) {
        if (stack.has(OCDataComponents.EPIC_LORE.get())) {
            return true;
        }
        return stack.has(DataComponents.LORE);
    }

    public static void stripEpicLore(ItemStack stack) {
        stack.remove(OCDataComponents.EPIC_LORE.get());
        stack.remove(DataComponents.LORE);
    }
}
