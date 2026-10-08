package dev.opencubes.content.sleeping;

import dev.opencubes.OCConstants;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.List;
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
            passTheNight(player);
        }
    }

    /** Skips the night once enough bag sleepers have slept long enough. */
    private static void passTheNight(ServerPlayer player) {
        ServerLevel level = player.level();
        if (!player.isSleeping() || !player.isSleepingLongEnough() || !level.isDarkOutside()) {
            return;
        }
        if (!level.getGameRules().get(GameRules.ADVANCE_TIME)) {
            return;
        }
        int percentage = level.getGameRules().get(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
        int active = 0;
        int deep = 0;
        for (ServerPlayer other : level.players()) {
            if (other.isSpectator()) {
                continue;
            }
            active++;
            if (other.isSleeping() && other.isSleepingLongEnough()) {
                deep++;
            }
        }
        int needed = Math.max(1, (active * percentage + 99) / 100);
        if (deep < needed) {
            return;
        }
        var clock = level.dimensionType().defaultClock();
        if (clock.isEmpty()) {
            return;
        }
        var adjustment = net.neoforged.neoforge.event.EventHooks.onSleepFinished(level,
                new net.neoforged.neoforge.common.util.ClockAdjustment.Marker(ClockTimeMarkers.WAKE_UP_FROM_SLEEP));
        if (adjustment != null) {
            adjustment.apply(level.getServer().clockManager(), clock.get());
        }
        for (ServerPlayer other : List.copyOf(level.players())) {
            if (other.isSleeping()) {
                other.stopSleepInBed(false, true);
            }
        }
    }

    /** Applies the usual night and monster checks. The bag block has no facing. */
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
        if (level.dimension() != Level.OVERWORLD) {
            return Player.BedSleepingProblem.OTHER_PROBLEM;
        }
        if (level.isBrightOutside()) {
            return Player.BedSleepingProblem.OTHER_PROBLEM;
        }
        if (!player.isCreative()) {
            Vec3 centre = Vec3.atBottomCenterOf(pos);
            AABB area = new AABB(centre.x - 8, centre.y - 5, centre.z - 8, centre.x + 8, centre.y + 5, centre.z + 8);
            if (!level.getEntitiesOfClass(Monster.class, area, monster -> monster.isPreventingPlayerRest((ServerLevel) level, player)).isEmpty()) {
                return Player.BedSleepingProblem.NOT_SAFE;
            }
        }
        return null;
    }

    /** Keeps bag sleepers asleep through the night. */
    @SubscribeEvent
    public static void continueBagSleep(CanContinueSleepingEvent event) {
        if (!wearing(event.getEntity())) {
            return;
        }
        Player.BedSleepingProblem problem = event.getProblem();
        if (problem == null || problem == Player.BedSleepingProblem.NOT_SAFE) {
            return;
        }
        if (event.getEntity().level().isDarkOutside()) {
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
