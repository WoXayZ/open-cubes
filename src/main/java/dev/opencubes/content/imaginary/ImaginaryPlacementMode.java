package dev.opencubes.content.imaginary;

public enum ImaginaryPlacementMode {
    BLOCK(1.0F, ImaginaryShape.BLOCK, false, "block"),
    PANEL(0.5F, ImaginaryShape.PANEL, false, "panel"),
    HALF_PANEL(0.5F, ImaginaryShape.HALF_PANEL, false, "half_panel"),
    STAIRS(0.75F, ImaginaryShape.STAIRS, false, "stairs"),
    INV_BLOCK(1.5F, ImaginaryShape.BLOCK, true, "inverted_block"),
    INV_PANEL(1.0F, ImaginaryShape.PANEL, true, "inverted_panel"),
    INV_HALF_PANEL(1.0F, ImaginaryShape.HALF_PANEL, true, "inverted_half_panel"),
    INV_STAIRS(1.25F, ImaginaryShape.STAIRS, true, "inverted_stairs");

    public static final ImaginaryPlacementMode[] VALUES = values();

    private final float cost;
    private final ImaginaryShape shape;
    private final boolean inverted;
    private final String key;

    ImaginaryPlacementMode(float cost, ImaginaryShape shape, boolean inverted, String key) {
        this.cost = cost;
        this.shape = shape;
        this.inverted = inverted;
        this.key = key;
    }

    public float cost() {
        return cost;
    }

    public ImaginaryShape shape() {
        return shape;
    }

    public boolean inverted() {
        return inverted;
    }

    public String translationKey() {
        return "opencubes.misc.mode." + key;
    }

    public ImaginaryPlacementMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }
}
