package dev.opencubes.content.sleeping;

import dev.opencubes.OCConstants;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class SleepingBagEvents {

    private SleepingBagEvents() {}

    private static boolean wearing(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof SleepingBagItem;
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof SleepingBagItem) {
            SleepingBagItem.onArmorTick(chest, player.level(), player);
        }
    }

    /**
     * NeoForge skips every vanilla check when the sleep pos holds no FACING property, so the
     * time and monster checks a bed would get are applied here for the bag.
     */
    @SubscribeEvent
    public static void allowSleepAnywhere(CanPlayerSleepEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !wearing(player)) {
            return;
        }
        event.setProblem(bagProblem(player, event.getPos()));
    }

    @Nullable
    private static Player.BedSleepingProblem bagProblem(ServerPlayer player, BlockPos pos) {
        Level level = player.level();
        if (!level.dimensionType().natural()) {
            return Player.BedSleepingProblem.NOT_POSSIBLE_HERE;
        }
        if (level.isDay()) {
            return Player.BedSleepingProblem.NOT_POSSIBLE_NOW;
        }
        if (!player.isCreative()) {
            Vec3 centre = Vec3.atBottomCenterOf(pos);
            AABB area = new AABB(centre.x - 8, centre.y - 5, centre.z - 8, centre.x + 8, centre.y + 5, centre.z + 8);
            if (!level.getEntitiesOfClass(Monster.class, area, monster -> monster.isPreventingPlayerRest(player)).isEmpty()) {
                return Player.BedSleepingProblem.NOT_SAFE;
            }
        }
        return null;
    }

    /**
     * Vanilla wakes anyone whose sleep pos is not a bed every tick ({@code NOT_POSSIBLE_HERE}).
     * Keep bag sleepers asleep until day ({@code NOT_POSSIBLE_NOW}) so the night can skip.
     */
    @SubscribeEvent
    public static void continueBagSleep(CanContinueSleepingEvent event) {
        if (!wearing(event.getEntity())) {
            return;
        }
        if (event.getProblem() == Player.BedSleepingProblem.NOT_POSSIBLE_HERE) {
            event.setContinueSleeping(true);
        }
    }

    @SubscribeEvent
    public static void blockSpawnReset(PlayerSetSpawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && wearing(player)) {
            event.setCanceled(true);
        }
    }
}
