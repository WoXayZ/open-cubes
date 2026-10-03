package dev.opencubes.compat;

import dev.opencubes.OCConstants;
import net.minecraft.server.level.ServerPlayer;
import vazkii.patchouli.api.PatchouliAPI;

/** Isolated Patchouli API calls so the class only loads when Patchouli is present. */
public final class PatchouliCompat {

    public static final String BOOK_ID = "world_domination";

    private PatchouliCompat() {}

    public static void openBook(ServerPlayer player) {
        PatchouliAPI.get().openBookGUI(player, OCConstants.id(BOOK_ID));
    }
}
