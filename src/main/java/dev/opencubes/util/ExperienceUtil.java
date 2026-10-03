package dev.opencubes.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * Vanilla keeps the level-to-experience curve inside {@code Player} without exposing a
 * "how much experience does this player hold" accessor, so the curve is reproduced here.
 * Kept in one place so every feature that charges experience agrees on the number.
 */
public final class ExperienceUtil {

    private ExperienceUtil() {}

    public static int totalExperience(Player player) {
        int bar = Math.max(1, player.getXpNeededForNextLevel());
        // Epsilon avoids float truncation dropping the last point after drinking XP juice
        // (progress * bar can land just under an integer).
        int intoBar = Mth.clamp(Mth.floor(player.experienceProgress * bar + 1.0E-5F), 0, bar);
        return experienceAtLevel(player.experienceLevel) + intoBar;
    }

    /** Total experience accumulated by the time a player reaches {@code level}. */
    public static int experienceAtLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level <= 16) {
            return level * level + 6 * level;
        }
        if (level <= 31) {
            return (int) (2.5D * level * level - 40.5D * level + 360.0D);
        }
        return (int) (4.5D * level * level - 162.5D * level + 2220.0D);
    }

    /** Highest whole level reachable with {@code experience} points. */
    public static int levelForExperience(int experience) {
        int level = 0;
        int remaining = Math.max(0, experience);
        while (true) {
            int forNext = experienceForNextLevel(level);
            if (remaining < forNext) {
                return level;
            }
            remaining -= forNext;
            level++;
        }
    }

    /** XP needed to go from {@code level} to {@code level + 1}. */
    public static int experienceForNextLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        }
        if (level >= 15) {
            return 37 + (level - 15) * 5;
        }
        return 7 + level * 2;
    }

    /**
     * Removes experience points, clamping at zero. {@link Player#giveExperiencePoints} does
     * the right thing with negatives but will not stop at an empty bar on its own.
     */
    public static void consume(Player player, int amount) {
        if (amount <= 0) {
            return;
        }
        player.giveExperiencePoints(-Math.min(amount, totalExperience(player)));
    }
}
