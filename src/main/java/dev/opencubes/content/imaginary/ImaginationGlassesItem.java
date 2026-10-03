package dev.opencubes.content.imaginary;

import dev.opencubes.registry.OCArmorMaterials;
import dev.opencubes.registry.OCDataComponents;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

public class ImaginationGlassesItem extends ArmorItem {

    private final ImaginationGlassesKind kind;

    public ImaginationGlassesItem(Properties properties, ImaginationGlassesKind kind) {
        super(OCArmorMaterials.GLASSES, Type.HELMET, properties.stacksTo(1));
        this.kind = kind;
    }

    public ImaginationGlassesKind kind() {
        return kind;
    }

    @Nullable
    public static Integer getCrayonColour(ItemStack stack) {
        return stack.get(OCDataComponents.PAINT_COLOR.get());
    }

    public static ItemStack createCrayonGlasses(Item item, int colour) {
        ItemStack stack = new ItemStack(item);
        stack.set(OCDataComponents.PAINT_COLOR.get(), colour & 0xFFFFFF);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (kind == ImaginationGlassesKind.CRAYON) {
            Integer colour = getCrayonColour(stack);
            if (colour != null) {
                tooltip.add(Component.translatable("opencubes.misc.color", String.format("#%06X", colour)));
            }
        }
    }
}
