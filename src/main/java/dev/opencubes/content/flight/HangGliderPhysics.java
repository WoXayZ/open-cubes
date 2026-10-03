package dev.opencubes.content.flight;

import dev.opencubes.config.OCCommonConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class HangGliderPhysics {

    public static final double VSPEED_NORMAL = -0.052;
    public static final double VSPEED_FAST = -0.176;
    public static final double VSPEED_MIN = -0.32;
    public static final double VSPEED_MAX = 0.4;

    private HangGliderPhysics() {}

    /**
     * Applies hang-glider motion when the player is falling. Returns the vertical speed used
     * (for the variometer).
     */
    public static double apply(Player player, Level level, double lastMotionY) {
        Vec3 motion = player.getDeltaMovement();
        if (motion.y >= lastMotionY) {
            return motion.y;
        }

        double noise = OCCommonConfig.HANG_GLIDER_THERMAL.get()
                ? ThermalField.lift(level, player)
                : 0.0D;
        double vspeedScale = noise >= 0 ? VSPEED_MAX : -VSPEED_MIN;

        double horizontalSpeed;
        double verticalSpeed;
        if (player.isShiftKeyDown()) {
            horizontalSpeed = 0.1D;
            verticalSpeed = Math.max(VSPEED_FAST + noise * vspeedScale, VSPEED_MIN);
        } else {
            horizontalSpeed = 0.03D;
            verticalSpeed = Math.max(VSPEED_NORMAL + noise * vspeedScale, VSPEED_MIN);
        }

        double yaw = Math.toRadians(player.getYHeadRot() + 90.0F);
        double dx = Math.cos(yaw) * horizontalSpeed;
        double dz = Math.sin(yaw) * horizontalSpeed;
        player.setDeltaMovement(motion.x + dx, verticalSpeed, motion.z + dz);
        player.fallDistance = 0.0F;
        player.hurtMarked = true;
        return verticalSpeed;
    }

    public static boolean canDeploy(Player player) {
        return !player.onGround()
                && !player.isInWater()
                && !player.isSleeping()
                && !player.isSpectator();
    }

    public static float varioFrequency(double vspeed) {
        if (vspeed <= 0) {
            vspeed = Math.max(VSPEED_MIN, vspeed);
            return (float) ((vspeed - VSPEED_MIN) / Math.abs(VSPEED_MIN) * (600 - 300) + 300);
        }
        vspeed = Math.min(VSPEED_MAX, vspeed);
        return (float) (vspeed / Math.abs(VSPEED_MAX) * (2000 - 600) + 600);
    }

    public static float varioBeepRate(double vspeed) {
        if (vspeed > 0) {
            vspeed = Math.min(VSPEED_MAX, vspeed);
            return (float) (vspeed / Math.abs(VSPEED_MAX) * (24 - 4) + 4);
        }
        if (vspeed < 0) {
            double abs = Math.min(Math.abs(vspeed), Math.abs(VSPEED_MIN));
            return (float) (abs / Math.abs(VSPEED_MIN) * (8 - 2) + 2);
        }
        return 0.0F;
    }
}
