package dev.opencubes.content.automation;

import net.minecraft.util.StringRepresentable;

/**
 * What a vacuum hopper face is currently outputting, driven by the item/xp output side masks.
 * Purely a rendering concern: picks which overlay model (if any) a multipart face shows.
 */
public enum FaceMode implements StringRepresentable {
    NONE("none"),
    ITEMS("items"),
    FLUIDS("fluids"),
    BOTH("both");

    private final String name;

    FaceMode(String name) {
        this.name = name;
    }

    public static FaceMode of(boolean items, boolean fluids) {
        if (items && fluids) {
            return BOTH;
        }
        if (items) {
            return ITEMS;
        }
        if (fluids) {
            return FLUIDS;
        }
        return NONE;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
