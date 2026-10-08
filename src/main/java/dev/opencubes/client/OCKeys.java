package dev.opencubes.client;

import dev.opencubes.OCConstants;
import net.minecraft.client.KeyMapping;

/** One controls-screen group for every OpenCubes key. */
public final class OCKeys {

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(OCConstants.id("opencubes"));

    private OCKeys() {}
}
