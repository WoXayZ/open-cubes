package dev.opencubes.util;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCFluids;
import dev.opencubes.registry.OCTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Converts between experience points and millibuckets of XP juice.
 * Default is twenty millibuckets per point, matching the original.
 */
public final class XpFluidUtil {

    /** Vanilla bottle o' enchanting awards this much XP when drunk. */
    public static final int XP_PER_BOTTLE = 8;

    private XpFluidUtil() {}

    public static int millibucketsPerXp() {
        return OCCommonConfig.XP_MILLIBUCKETS_PER_POINT.get();
    }

    public static int millibucketsPerBottle() {
        return toMillibuckets(XP_PER_BOTTLE);
    }

    public static FluidStack juice(int millibuckets) {
        return new FluidStack(OCFluids.XP_JUICE.get(), millibuckets);
    }

    public static int toMillibuckets(int xp) {
        return xp * millibucketsPerXp();
    }

    public static int toXp(int millibuckets) {
        return millibuckets / millibucketsPerXp();
    }

    public static boolean isXpJuice(FluidStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        // Own juice (still or flowing), or any fluid tagged c:experience (Create / other XP pipes).
        return stack.getFluid().isSame(OCFluids.XP_JUICE.get())
                || stack.getFluid().isSame(OCFluids.FLOWING_XP_JUICE.get())
                || stack.getFluid().is(OCTags.Fluids.EXPERIENCE);
    }

    /**
     * How much XP juice it takes to fill the player's current experience bar to the next level.
     */
    public static int millibucketsToFillBar(Player player) {
        float remaining = 1.0F - player.experienceProgress;
        int xpNeeded = Mth.ceil(player.getXpNeededForNextLevel() * remaining);
        return toMillibuckets(Math.max(xpNeeded, 1));
    }
}
