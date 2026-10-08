package dev.opencubes.content.trophy.behavior;

import dev.opencubes.content.trophy.TrophyBehavior;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import dev.opencubes.content.trophy.TrophyDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.level.Level;

public final class BreezeTrophyBehavior implements TrophyBehavior {

    @Override
    public int onActivate(TrophyBlockEntity trophy, Player player, TrophyDefinition definition) {
        Level level = trophy.getLevel();
        if (level == null) {
            return 0;
        }
        BlockPos pos = trophy.getBlockPos();
        WindCharge charge = EntityType.WIND_CHARGE.create(level, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
        if (charge == null) {
            return 0;
        }
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 1.0D;
        double z = pos.getZ() + 0.5D;
        charge.setOwner(player);
        charge.setPos(x, y, z);
        double dX = player.getX() - x;
        double dY = player.getY(0.3333333333333333D) - y;
        double dZ = player.getZ() - z;
        float dist = Mth.sqrt((float) (dX * dX + dZ * dZ)) * 0.2F;
        charge.shoot(dX, dY + dist, dZ, 0.8F, 4.0F);
        level.playSound(null, pos, SoundEvents.WIND_CHARGE_THROW, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.addFreshEntity(charge);
        return 60;
    }
}
