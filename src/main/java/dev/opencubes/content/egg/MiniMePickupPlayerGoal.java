package dev.opencubes.content.egg;

import com.mojang.authlib.GameProfile;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

/** Paths to the owner and offers a shoulder ride when close enough. */
public class MiniMePickupPlayerGoal extends Goal {

    private static final double SEARCH_RANGE = 10.0D;
    private static final double PICKUP_RANGE_SQ = 1.0D;

    private final MiniMeEntity miniMe;
    @Nullable
    private Player targetPlayer;

    public MiniMePickupPlayerGoal(MiniMeEntity miniMe) {
        this.miniMe = miniMe;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (miniMe.pickupCooldown() > 0 || !miniMe.getNavigation().isDone()) {
            return false;
        }
        if (miniMe.level().isClientSide) {
            return false;
        }
        targetPlayer = findOwnerPlayer();
        return targetPlayer != null && canOfferRide(targetPlayer);
    }

    @Override
    public boolean canContinueToUse() {
        return miniMe.isAlive()
                && targetPlayer != null
                && targetPlayer.isAlive()
                && canOfferRide(targetPlayer)
                && miniMe.distanceToSqr(targetPlayer) < SEARCH_RANGE * SEARCH_RANGE * 4.0D;
    }

    @Override
    public void start() {
        if (targetPlayer != null) {
            miniMe.getNavigation().moveTo(targetPlayer, 1.0D);
        }
    }

    @Override
    public void stop() {
        miniMe.getNavigation().stop();
        targetPlayer = null;
    }

    @Override
    public void tick() {
        if (targetPlayer == null) {
            return;
        }
        miniMe.getLookControl().setLookAt(targetPlayer, 30.0F, 30.0F);
        if (miniMe.distanceToSqr(targetPlayer) > PICKUP_RANGE_SQ) {
            miniMe.getNavigation().moveTo(targetPlayer, 1.0D);
            return;
        }
        if (!targetPlayer.isPassenger()) {
            targetPlayer.startRiding(miniMe, true);
        }
    }

    @Nullable
    private Player findOwnerPlayer() {
        GameProfile owner = miniMe.ownerProfile();
        if (owner == null || owner.getId() == null) {
            return null;
        }
        Player player = miniMe.level().getPlayerByUUID(owner.getId());
        if (player == null || !player.isAlive()) {
            return null;
        }
        if (miniMe.distanceToSqr(player) > SEARCH_RANGE * SEARCH_RANGE) {
            return null;
        }
        return player;
    }

    private boolean canOfferRide(Player player) {
        GameProfile owner = miniMe.ownerProfile();
        return owner != null
                && owner.getId() != null
                && owner.getId().equals(player.getUUID())
                && !player.isPassenger()
                && miniMe.pickupCooldown() <= 0
                && !miniMe.isVehicle();
    }
}
