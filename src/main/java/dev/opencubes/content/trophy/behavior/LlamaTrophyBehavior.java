package dev.opencubes.content.trophy.behavior;

import dev.opencubes.content.trophy.TrophyBehavior;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import dev.opencubes.content.trophy.TrophyDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.level.Level;

public final class LlamaTrophyBehavior implements TrophyBehavior {

    @Override
    public int onActivate(TrophyBlockEntity trophy, Player player, TrophyDefinition definition) {
        Level level = trophy.getLevel();
        if (level == null) {
            return 0;
        }
        BlockPos pos = trophy.getBlockPos();
        double pX = pos.getX() + 0.5D;
        double pY = pos.getY() + 1.0D;
        double pZ = pos.getZ() + 0.5D;

        LlamaSpit spit = net.minecraft.world.entity.EntityType.LLAMA_SPIT.create(level);
        if (spit == null) {
            return 0;
        }
        spit.setOwner(player);
        spit.setPos(pX, pY, pZ);

        double dX = player.getX() - pX;
        double dY = player.getY(0.3333333333333333D) - spit.getY();
        double dZ = player.getZ() - pZ;
        float f = Mth.sqrt((float) (dX * dX + dZ * dZ)) * 0.2F;
        spit.shoot(dX, dY + f, dZ, 1.5F, 10.0F);
        level.playSound(null, pos, SoundEvents.LLAMA_SPIT, SoundSource.NEUTRAL, 1.0F,
                1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F);
        level.addFreshEntity(spit);
        return 0;
    }
}
