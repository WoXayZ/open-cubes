package dev.opencubes;

import net.minecraft.resources.ResourceLocation;

public final class OCConstants {

    public static final String MOD_ID = "opencubes";

    private OCConstants() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
