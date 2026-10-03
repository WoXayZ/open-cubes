package dev.opencubes.content.guide;

import java.util.Locale;

/** Half-extents of the guide shape (positive/negative X/Y/Z from the block origin). */
public enum GuideHalfAxis {
    NEG_X,
    NEG_Y,
    NEG_Z,
    POS_X,
    POS_Y,
    POS_Z;

    public GuideHalfAxis negate() {
        return switch (this) {
            case NEG_X -> POS_X;
            case POS_X -> NEG_X;
            case NEG_Y -> POS_Y;
            case POS_Y -> NEG_Y;
            case NEG_Z -> POS_Z;
            case POS_Z -> NEG_Z;
        };
    }

    public String commandName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static GuideHalfAxis byCommandName(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }
}
