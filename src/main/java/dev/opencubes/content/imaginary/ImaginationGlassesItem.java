package dev.opencubes.content.imaginary;

import dev.opencubes.registry.OCArmorMaterials;
import dev.opencubes.registry.OCDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;

public class ImaginationGlassesItem extends Item {

    private final ImaginationGlassesKind kind;

    public ImaginationGlassesItem(Properties properties, ImaginationGlassesKind kind) {
        super(properties.stacksTo(1).humanoidArmor(OCArmorMaterials.GLASSES, ArmorType.HELMET));
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (kind == ImaginationGlassesKind.CRAYON) {
            Integer colour = getCrayonColour(stack);
            if (colour != null) {
                tooltip.accept(Component.translatable("opencubes.misc.color", String.format("#%06X", colour)));
            }
        }
    }
}
