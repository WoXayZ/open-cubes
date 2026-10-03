package dev.opencubes.content.trophy.behavior;

import dev.opencubes.content.trophy.TrophyBehavior;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import dev.opencubes.content.trophy.TrophyDefinition;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class IronGolemTrophyBehavior implements TrophyBehavior {

    @Override
    public int onActivate(TrophyBlockEntity trophy, Player player, TrophyDefinition definition) {
        Level level = trophy.getLevel();
        if (level == null) {
            return 0;
        }
        level.playSound(null, trophy.getBlockPos(), SoundEvents.IRON_GOLEM_ATTACK,
                SoundSource.BLOCKS, 1.0F, 1.0F);
        ItemStack poppy = new ItemStack(Items.POPPY);
        if (!player.addItem(poppy)) {
            level.addFreshEntity(new ItemEntity(level,
                    trophy.getBlockPos().getX() + 0.5D,
                    trophy.getBlockPos().getY() + 0.8D,
                    trophy.getBlockPos().getZ() + 0.5D,
                    poppy));
        }
        return 200;
    }
}
