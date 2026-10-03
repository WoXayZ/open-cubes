package dev.opencubes.content.flight;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

/**
 * World thermal lift - shared by the Hang Glider and Thermal Elytra.
 * Port of OpenBlocks {@code EntityHangGlider#getNoise} with the §10 rain/exposure fix.
 */
public final class ThermalField {

    public static final int THERMAL_HEIGHT_MIN = 70;
    public static final int THERMAL_HEIGHT_OPT = 110;
    public static final int THERMAL_HEIGHT_MAX = 136;
    public static final int THERMAL_STRONG_BONUS_HEIGHT = 100;

    private static final Map<Integer, ImprovedNoise> NOISE_BY_DAY = new ConcurrentHashMap<>();

    private ThermalField() {}

    public static double lift(Level level, Player player) {
        if (level.dimension() != Level.OVERWORLD) {
            return 0.0D;
        }

        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        ImprovedNoise noiseGen = NOISE_BY_DAY.computeIfAbsent(dayOfYear,
                day -> new ImprovedNoise(RandomSource.create(day)));

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        double noise = noiseGen.noise(x / 20.0D, 0.0D, z / 20.0D) / 4.0D;
        boolean strong = noise > 0.7D;
        int bonus = strong ? THERMAL_STRONG_BONUS_HEIGHT : 0;

        noise *= Math.min(Math.max(y - THERMAL_HEIGHT_MIN, 0.0D)
                / (THERMAL_HEIGHT_OPT - THERMAL_HEIGHT_MIN), 1.0D);
        noise *= Math.min(Math.max(THERMAL_HEIGHT_MAX + bonus - y, 0.0D)
                / (THERMAL_HEIGHT_MAX - THERMAL_HEIGHT_OPT + bonus / 4.0D), 1.0D);

        int worldTime = (int) (level.getDayTime() % 24000L);
        noise *= Math.min(worldTime / 1000.0D, 1.0D);
        noise *= Math.min(Math.max(12000 - worldTime, 0) / 1000.0D, 1.0D);

        if (level.isRaining() && !strong && player.isInWaterOrRain()) {
            noise = -0.5D;
        }
        return noise;
    }

    public static double clampVSpeed(double verticalSpeed) {
        return Mth.clamp(verticalSpeed, HangGliderPhysics.VSPEED_MIN, HangGliderPhysics.VSPEED_MAX);
    }
}
