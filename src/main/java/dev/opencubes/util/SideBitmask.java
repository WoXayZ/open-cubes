package dev.opencubes.util;

import net.minecraft.core.Direction;

/**
 * Compact six-face bitmask used by machines that configure per-side item or fluid output.
 * Bits follow {@link Direction#get3DDataValue()}.
 */
public final class SideBitmask {

    private SideBitmask() {}

    public static int with(int mask, Direction side, boolean enabled) {
        int bit = 1 << side.get3DDataValue();
        return enabled ? (mask | bit) : (mask & ~bit);
    }

    public static boolean has(int mask, Direction side) {
        return (mask & (1 << side.get3DDataValue())) != 0;
    }

    public static int toggle(int mask, Direction side) {
        return mask ^ (1 << side.get3DDataValue());
    }

    public static boolean isEmpty(int mask) {
        return mask == 0;
    }
}
