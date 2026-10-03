package dev.opencubes.content.tomfoolery;

import dev.opencubes.OCConstants;
import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCAttachments;
import dev.opencubes.registry.OCCriteria;
import dev.opencubes.registry.OCStats;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class BrickManager {

    private BrickManager() {}

    /** Server-side: drop one brick if the player has bowel count and tomfoolery is allowed. */
    public static void tryDropBrick(ServerPlayer player) {
        if (OCCommonConfig.WE_ARE_SERIOUS_PEOPLE.get()) {
            return;
        }
        int count = player.getData(OCAttachments.BOWEL.get());
        if (count <= 0) {
            return;
        }
        player.setData(OCAttachments.BOWEL.get(), count - 1);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.0F, 0.5F);
        ItemEntity brick = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(),
                new ItemStack(Items.BRICK));
        brick.setPickUpDelay(10);
        player.level().addFreshEntity(brick);
        player.awardStat(Stats.ITEM_DROPPED.get(Items.BRICK));
        player.awardStat(Stats.CUSTOM.get(OCStats.BRICKS_DROPPED_ID));
        OCCriteria.BRICK_DROPPED.get().trigger(player);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        if (OCCommonConfig.WE_ARE_SERIOUS_PEOPLE.get()) {
            return;
        }
        int count = player.getData(OCAttachments.BOWEL.get());
        int drop = Math.min(16, count);
        player.setData(OCAttachments.BOWEL.get(), 0);
        for (int i = 0; i < drop; i++) {
            player.spawnAtLocation(new ItemStack(Items.BRICK));
        }
    }
}
