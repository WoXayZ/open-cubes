package dev.opencubes.client.vision;

import dev.opencubes.OCConstants;
import net.minecraft.resources.ResourceLocation;

/**
 * Maps sound event ids to small category icons for the Sonic Glasses HUD.
 * Prefix-based MVP - not a full OpenBlocks atlas stitch.
 */
public final class SoundIconRegistry {

    public enum Category {
        BLOCK("block"),
        HOSTILE("hostile"),
        FRIENDLY("friendly"),
        PLAYER("player"),
        WEATHER("weather"),
        MUSIC("music"),
        UI("ui"),
        AMBIENT("ambient"),
        DEFAULT("default");

        private final ResourceLocation texture;

        Category(String file) {
            this.texture = OCConstants.id("textures/gui/sounds/" + file + ".png");
        }

        public ResourceLocation texture() {
            return texture;
        }
    }

    private SoundIconRegistry() {}

    public static Category resolve(ResourceLocation sound) {
        if (sound == null) {
            return Category.DEFAULT;
        }
        String path = sound.getPath();
        if (path.startsWith("block.") || path.startsWith("block/")) {
            return Category.BLOCK;
        }
        if (path.startsWith("entity.") || path.startsWith("entity/")) {
            if (containsAny(path, "zombie", "skeleton", "creeper", "spider", "enderman", "witch",
                    "blaze", "ghast", "wither", "phantom", "pillager", "vindicator", "evoker",
                    "ravager", "shulker", "slime", "magma", "guardian", "elder", "hoglin",
                    "piglin", "warden", "breeze", "bogged")) {
                return Category.HOSTILE;
            }
            if (path.contains("player") || path.contains("arrow") || path.contains("item.crossbow")) {
                return Category.PLAYER;
            }
            return Category.FRIENDLY;
        }
        if (path.startsWith("weather.") || path.startsWith("weather/")
                || path.contains("rain") || path.contains("thunder")) {
            return Category.WEATHER;
        }
        if (path.startsWith("music.") || path.startsWith("music/") || path.contains("record")) {
            return Category.MUSIC;
        }
        if (path.startsWith("ui.") || path.startsWith("ui/")) {
            return Category.UI;
        }
        if (path.startsWith("ambient.") || path.startsWith("ambient/")) {
            return Category.AMBIENT;
        }
        if (path.startsWith("item.") || path.startsWith("item/")) {
            return Category.BLOCK;
        }
        return Category.DEFAULT;
    }

    private static boolean containsAny(String path, String... tokens) {
        for (String token : tokens) {
            if (path.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
