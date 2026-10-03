package dev.opencubes.content.trophy.behavior;

import dev.opencubes.content.trophy.TrophyBehavior;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import dev.opencubes.content.trophy.TrophyDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ItemDropTrophyBehavior implements TrophyBehavior {

    @Override
    public int onActivate(TrophyBlockEntity trophy, Player player, TrophyDefinition definition) {
        TrophyDefinition.DropSpec drop = definition.drop().orElse(null);
        if (drop == null) {
            return 0;
        }

        Item item = BuiltInRegistries.ITEM.get(drop.item());
        if (item == null) {
            return 0;
        }

        Level level = trophy.getLevel();
        if (level == null) {
            return 0;
        }

        drop.sound().flatMap(BuiltInRegistries.SOUND_EVENT::getOptional).ifPresent(sound ->
                level.playSound(null, trophy.getBlockPos(), sound, SoundSource.NEUTRAL, 1.0F,
                        (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F));

        ItemStack stack = new ItemStack(item, Math.max(1, drop.count()));
        ItemEntity entity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
        entity.setPickUpDelay(0);
        level.addFreshEntity(entity);
        return Math.max(0, drop.cooldown());
    }
}
