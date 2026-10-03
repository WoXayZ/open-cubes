package dev.opencubes.fluid;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Liquid experience. Normally it lives in tanks and machines, but an XP Bucket can also
 * spill it into the world as a source block.
 */
public class XpJuiceFluidType extends FluidType {

    public XpJuiceFluidType() {
        super(Properties.create()
                .descriptionId("fluid_type.opencubes.xp_juice")
                .canSwim(false)
                .canDrown(false)
                .canExtinguish(false)
                .canConvertToSource(false)
                .fallDistanceModifier(0.0F)
                .supportsBoating(false)
                .pathType(PathType.DAMAGE_OTHER)
                .adjacentPathType(null)
                .canHydrate(false)
                .lightLevel(10)
                .density(800)
                .temperature(300)
                .viscosity(1500)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.EXPERIENCE_ORB_PICKUP)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.PLAYER_LEVELUP));
    }
}
