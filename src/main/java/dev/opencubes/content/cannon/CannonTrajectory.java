package dev.opencubes.content.cannon;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Ballistic helpers for the item cannon. Gravity and air drag match
 * {@link net.minecraft.world.entity.item.ItemEntity} so the Pointer preview and the fired item share one path.
 */
public final class CannonTrajectory {

    /** Same per-tick gravity applied to dropped items. */
    public static final double GRAVITY_PER_TICK = 0.04D;

    /** Horizontal / vertical air drag applied after each ItemEntity move step. */
    public static final double AIR_DRAG = 0.98D;

    /**
     * Aim solver ignores drag; scale solved speed so real ItemEntity flight (with drag) still reaches.
     */
    private static final float DRAG_SPEED_COMPENSATION = 1.35F;

    private static final double STEPS_PER_SECOND = 20.0D;
    private static final double PARTIAL_TIME = 1.0D / STEPS_PER_SECOND;
    private static final double PARTIAL_TIME_SQUARE = PARTIAL_TIME * PARTIAL_TIME;
    private static final Vec3 GRAVITY_ACCEL_SQUARE_PARTIAL =
            new Vec3(0.0D, PARTIAL_TIME_SQUARE * -GRAVITY_PER_TICK, 0.0D);

    private static final int YAW_OFFSET_DEGREES = -90;
    private static final int LOB_MINIMUM = 20;
    private static final int LOB_MAXIMUM = 75;
    private static final int LOB_VERTICAL_MUL = 4;
    private static final int LOB_HORIZONTAL_MUL = 1;
    private static final int LOB_BONUS = 5;

    private CannonTrajectory() {}

    public static Vec3 projectileOrigin(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
    }

    public static Vec3 muzzleOffset(float yaw, float pitch) {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        return new Vec3(
                0.5D - Math.sin(yawRad) * Math.cos(pitchRad) * 0.55D,
                0.55D + Math.sin(pitchRad) * 0.55D,
                0.5D + Math.cos(yawRad) * Math.cos(pitchRad) * 0.55D);
    }

    public static Vec3 muzzlePosition(BlockPos pos, float yaw, float pitch) {
        return projectileOrigin(pos).add(muzzleOffset(yaw, pitch).subtract(0.5D, 0.0D, 0.5D));
    }

    public static void aimAt(BlockPos cannonPos, BlockPos targetPos, float[] outYawPitchSpeed) {
        Vec3 origin = projectileOrigin(cannonPos);
        Vec3 target = new Vec3(targetPos.getX() + 0.5D, targetPos.getY() + 1.0D, targetPos.getZ() + 0.5D);

        double distHorizontal = LOB_HORIZONTAL_MUL * Math.sqrt(
                Mth.square(target.x - origin.x) + Mth.square(target.z - origin.z));
        double distVertical = Math.max((target.y - origin.y) * LOB_VERTICAL_MUL, 0.0D);
        float lobScale = (float) Mth.clamp(LOB_BONUS + distHorizontal + distVertical, LOB_MINIMUM, LOB_MAXIMUM);

        Vec3 velocity = calculateTrajectory(origin, target, lobScale);
        outYawPitchSpeed[2] = (float) velocity.length() * DRAG_SPEED_COMPENSATION;

        Vec3 direction = velocity.normalize();
        double pitch = Math.asin(direction.y);
        double yaw = Math.atan2(direction.z, direction.x);
        outYawPitchSpeed[0] = (float) (Math.toDegrees(yaw) + YAW_OFFSET_DEGREES);
        outYawPitchSpeed[1] = (float) Math.toDegrees(pitch);
    }

    /**
     * Initial velocity for a lobbed shot from {@code start} to {@code target}.
     */
    public static Vec3 calculateTrajectory(Vec3 start, Vec3 target, float scale) {
        double n = scale * STEPS_PER_SECOND;
        double accelerationMultiplier = 0.5D * n * n + n;

        Vec3 scaledAcceleration = new Vec3(
                GRAVITY_ACCEL_SQUARE_PARTIAL.x * accelerationMultiplier,
                GRAVITY_ACCEL_SQUARE_PARTIAL.y * accelerationMultiplier,
                GRAVITY_ACCEL_SQUARE_PARTIAL.z * accelerationMultiplier);

        double velocityMultiplier = -STEPS_PER_SECOND / n;
        return new Vec3(
                (start.x + scaledAcceleration.x - target.x) * velocityMultiplier,
                (start.y + scaledAcceleration.y - target.y) * velocityMultiplier,
                (start.z + scaledAcceleration.z - target.z) * velocityMultiplier);
    }

    public static Vec3 motionFromAngles(float yaw, float pitch, float speed) {
        double pitchRad = Math.toRadians(pitch);
        double yawRad = Math.toRadians(180.0D - yaw);
        double sinPitch = Math.sin(pitchRad);
        double cosPitch = Math.cos(pitchRad);
        double sinYaw = Math.sin(yawRad);
        double cosYaw = Math.cos(yawRad);
        return new Vec3(-cosPitch * sinYaw * speed, sinPitch * speed, -cosPitch * cosYaw * speed);
    }

    /**
     * Simulates {@code steps} ItemEntity physics ticks from a block-relative {@code muzzle}
     * with {@code initialMotion}: gravity, integrate position, then air drag.
     */
    public static void simulateRelative(Vec3 muzzle, Vec3 initialMotion, int steps, float[] xs, float[] ys, float[] zs) {
        double motionX = initialMotion.x;
        double motionY = initialMotion.y;
        double motionZ = initialMotion.z;
        double posX = muzzle.x;
        double posY = muzzle.y;
        double posZ = muzzle.z;
        int count = Math.min(steps, xs.length);
        for (int i = 0; i < count; i++) {
            xs[i] = (float) posX;
            ys[i] = (float) posY;
            zs[i] = (float) posZ;
            motionY -= GRAVITY_PER_TICK;
            posX += motionX;
            posY += motionY;
            posZ += motionZ;
            motionX *= AIR_DRAG;
            motionY *= AIR_DRAG;
            motionZ *= AIR_DRAG;
        }
    }
}
