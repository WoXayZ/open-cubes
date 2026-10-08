package dev.opencubes.content.xp;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.entity.XpOrbTargetingEvent;
import dev.opencubes.OCConstants;

/**
 * Marks an experience orb so it falls instead of seeking players - what the shower needs,
 * so the orbs land in a drain or a hopper instead of rushing back to whoever is standing
 * under it.
 *
 * <p>Implemented as a flag on a vanilla orb rather than a separate entity type: the spawn
 * packet for experience orbs is hard-wired to the vanilla type, and NeoForge already
 * exposes {@link XpOrbTargetingEvent} for exactly this.
 */
@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class XpOrbNoFly {

    private static final String TAG = "opencubes_no_fly";

    private XpOrbNoFly() {}

    public static ExperienceOrb spawn(Level level, double x, double y, double z, int xp) {
        return spawn(level, x, y, z, xp, new Vec3(0.0D, -0.1D * level.getRandom().nextFloat(), 0.0D));
    }

    public static ExperienceOrb spawn(Level level, double x, double y, double z, int xp, Vec3 motion) {
        ExperienceOrb orb = new ExperienceOrb(level, x, y, z, xp);
        orb.getPersistentData().putBoolean(TAG, true);
        orb.setPos(x, y, z);
        orb.setDeltaMovement(motion);
        orb.xo = x;
        orb.yo = y;
        orb.zo = z;
        level.addFreshEntity(orb);
        return orb;
    }

    public static boolean isNoFly(ExperienceOrb orb) {
        return orb.getPersistentData().getBooleanOr(TAG, false);
    }

    @SubscribeEvent
    public static void onTarget(XpOrbTargetingEvent event) {
        if (isNoFly(event.getXpOrb())) {
            event.setFollowingPlayer(null);
        }
    }
}
