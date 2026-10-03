package dev.opencubes.content.flight;

import dev.opencubes.registry.OCDataComponents;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Sail colour of the hang glider and thermal elytra, applied from a paint can. The fabric
 * textures are greyscale and multiplied by this colour; unpainted ones keep the mod's purple.
 */
public final class GliderPaint {

    /** Multiplied with the greyscale canvas, this gives the original purple sail. */
    public static final int DEFAULT_COLOUR = 0x7D5D9B;

    private GliderPaint() {}

    public static boolean isPaintable(ItemStack stack) {
        return stack.getItem() instanceof HangGliderItem || stack.getItem() instanceof ThermalElytraItem;
    }

    public static int colour(ItemStack stack) {
        Integer colour = stack.get(OCDataComponents.PAINT_COLOR.get());
        return colour == null ? DEFAULT_COLOUR : colour & 0xFFFFFF;
    }

    public static void appendTooltip(ItemStack stack, List<Component> tooltip) {
        Integer colour = stack.get(OCDataComponents.PAINT_COLOR.get());
        if (colour != null) {
            tooltip.add(Component.literal(String.format("#%06X", colour & 0xFFFFFF)).withStyle(ChatFormatting.GRAY));
        }
    }
}
