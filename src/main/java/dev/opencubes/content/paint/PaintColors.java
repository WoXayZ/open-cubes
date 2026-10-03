package dev.opencubes.content.paint;

import net.minecraft.world.item.DyeColor;

/** 24-bit RGB helpers and CMYK conversion for the paint mixer. */
public final class PaintColors {

    private PaintColors() {}

    public static int rgb(int r, int g, int b) {
        return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int asArgb(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    public static int red(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    public static int green(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    public static int blue(int rgb) {
        return rgb & 0xFF;
    }

    public static int fromDye(DyeColor colour) {
        return colour.getTextureDiffuseColor() & 0xFFFFFF;
    }

    /** Standard subtractive CMYK from RGB, each channel 0..1. */
    public record Cmyk(float cyan, float magenta, float yellow, float key) {}

    public static Cmyk toCmyk(int rgb) {
        float r = red(rgb) / 255.0F;
        float g = green(rgb) / 255.0F;
        float b = blue(rgb) / 255.0F;
        float k = 1.0F - Math.max(r, Math.max(g, b));
        if (k >= 1.0F - 1.0E-5F) {
            return new Cmyk(0, 0, 0, 1);
        }
        float c = (1.0F - r - k) / (1.0F - k);
        float m = (1.0F - g - k) / (1.0F - k);
        float y = (1.0F - b - k) / (1.0F - k);
        return new Cmyk(c, m, y, k);
    }
}
