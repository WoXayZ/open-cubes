package dev.opencubes.client.paint;

import dev.opencubes.registry.OCDataComponents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

/**
 * Draws three 2x2 channel swatches (R, G, B) in the top-left corner of paint can icons.
 */
public enum PaintCanItemDecoration implements IItemDecorator {
    INSTANCE;

    private static final int GRAY = 0xFF808080;
    private static final int SQUARE = 2;

    @Override
    public boolean render(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        Integer rgb = stack.get(OCDataComponents.PAINT_COLOR.get());
        drawChannel(graphics, xOffset, yOffset, rgb, 0);
        drawChannel(graphics, xOffset + SQUARE, yOffset, rgb, 1);
        drawChannel(graphics, xOffset + SQUARE * 2, yOffset, rgb, 2);
        return false;
    }

    private static void drawChannel(GuiGraphicsExtractor graphics, int x, int y, Integer rgb, int channel) {
        graphics.fill(x, y, x + SQUARE, y + SQUARE, channelArgb(rgb, channel));
    }

    private static int channelArgb(Integer rgb, int channel) {
        if (rgb == null) {
            return GRAY;
        }
        int value = switch (channel) {
            case 0 -> (rgb >> 16) & 0xFF;
            case 1 -> (rgb >> 8) & 0xFF;
            case 2 -> rgb & 0xFF;
            default -> 0x80;
        };
        return switch (channel) {
            case 0 -> 0xFF000000 | (value << 16);
            case 1 -> 0xFF000000 | (value << 8);
            case 2 -> 0xFF000000 | value;
            default -> GRAY;
        };
    }
}
