package dev.opencubes;

import net.minecraft.resources.Identifier;

public final class OCConstants {

    public static final String MOD_ID = "opencubes";

    private OCConstants() {}

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
