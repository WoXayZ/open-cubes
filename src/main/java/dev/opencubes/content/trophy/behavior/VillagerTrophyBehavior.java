package dev.opencubes.content.trophy.behavior;

import dev.opencubes.content.trophy.TrophyBehavior;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import dev.opencubes.content.trophy.TrophyDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;

/** Cycles the displayed villager / zombie villager profession. */
public final class VillagerTrophyBehavior implements TrophyBehavior {

    @Override
    public int onActivate(TrophyBlockEntity trophy, Player player, TrophyDefinition definition) {
        int professions = (int) BuiltInRegistries.VILLAGER_PROFESSION.stream().count();
        if (professions <= 0) {
            return 0;
        }
        trophy.setDisplayVariant((trophy.getDisplayVariant() + 1) % professions);
        if (trophy.getLevel() != null) {
            trophy.getLevel().playSound(null, trophy.getBlockPos(), SoundEvents.VILLAGER_YES,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return 20;
    }
}
