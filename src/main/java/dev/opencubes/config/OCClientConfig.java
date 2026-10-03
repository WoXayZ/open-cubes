package dev.opencubes.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Client-only visuals and HUD knobs. Never read from the logical server. */
public final class OCClientConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue GUIDE_RENDER_RANGE;
    public static final ModConfigSpec.DoubleValue GLASSES_OPACITY;
    public static final ModConfigSpec.DoubleValue GLASSES_SOUND_RANGE;
    public static final ModConfigSpec.DoubleValue IMAGINARY_FADE_SPEED;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.comment("Building guides").push("guide");
        GUIDE_RENDER_RANGE = BUILDER
                .comment("Client view distance (blocks) for guide markers.")
                .defineInRange("renderRange", 64, 16, 256);
        BUILDER.pop();

        BUILDER.comment("Sonic glasses").push("glasses");
        GLASSES_OPACITY = BUILDER
                .comment("How strongly the world is obscured (0 = clear, 1 = fully black).")
                .defineInRange("opacity", 1.0D, 0.0D, 1.0D);
        GLASSES_SOUND_RANGE = BUILDER
                .comment("Max distance (blocks) at which sounds create HUD blips.")
                .defineInRange("soundRange", 48.0D, 1.0D, 256.0D);
        BUILDER.pop();

        BUILDER.comment("Imaginary blocks").push("imaginary");
        IMAGINARY_FADE_SPEED = BUILDER
                .comment("Reserved fade speed for imaginary block alpha transitions (unused until fade ships).")
                .defineInRange("fadeSpeed", 0.1D, 0.0D, 1.0D);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private OCClientConfig() {}
}
