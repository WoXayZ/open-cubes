package dev.opencubes.content.crane;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCEntities;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Crane arm tip. Follows a point 2 blocks ahead of the owner at a controllable arm length.
 */
public class MagnetEntity extends Entity {

    private static final EntityDataAccessor<Boolean> DATA_ABOVE_TARGET =
            SynchedEntityData.defineId(MagnetEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER =
            SynchedEntityData.defineId(MagnetEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private boolean aboveTarget;

    public MagnetEntity(EntityType<? extends MagnetEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public MagnetEntity(Level level) {
        this(OCEntities.MAGNET.get(), level);
    }

    public void setOwner(Player player) {
        entityData.set(DATA_OWNER, Optional.of(player.getUUID()));
    }

    @Nullable
    public Player getOwner() {
        return entityData.get(DATA_OWNER).map(level()::getPlayerByUUID).orElse(null);
    }

    public boolean isValid() {
        Player owner = getOwner();
        return owner != null
                && owner.isAlive()
                && owner.level() == level()
                && CraneBackpackItem.isWearing(owner);
    }

    public boolean isLocked() {
        return isVehicle();
    }

    public boolean isAboveTarget() {
        return entityData.get(DATA_ABOVE_TARGET);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ABOVE_TARGET, false);
        builder.define(DATA_OWNER, Optional.empty());
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (!isValid()) {
            releaseCargoQuietly();
            discard();
            return;
        }

        Player owner = getOwner();
        if (owner == null) {
            return;
        }

        Vec3 target = tipTarget(owner).add(0.0D, -getBbHeight(), 0.0D);
        if (OCCommonConfig.CRANE_COLLISION_CHECK.get()) {
            target = clipTip(owner, target);
        }
        Vec3 delta = target.subtract(position()).scale(0.35D);
        setDeltaMovement(delta);
        move(MoverType.SELF, getDeltaMovement());
        setPos(target.x, target.y, target.z);

        aboveTarget = !detectTargets().isEmpty();
        entityData.set(DATA_ABOVE_TARGET, aboveTarget);

        // Keep passengers glued under the magnet.
        for (Entity passenger : getPassengers()) {
            passenger.setPos(getX(), getY() - passenger.getBbHeight(), getZ());
        }
    }

    public static Vec3 tipTarget(Player player) {
        float yaw = player.getYRot();
        double x = player.getX() + CraneRegistry.ARM_RADIUS * Mth.cos((yaw + 90.0F) * ((float) Math.PI / 180.0F));
        double z = player.getZ() + CraneRegistry.ARM_RADIUS * Mth.sin((yaw + 90.0F) * ((float) Math.PI / 180.0F));
        double y = player.getY() + player.getBbHeight()
                - CraneRegistry.INSTANCE.getMagnetDistance(player);
        return new Vec3(x, y, z);
    }

    private static Vec3 clipTip(Player owner, Vec3 target) {
        Vec3 from = owner.getEyePosition();
        var hit = owner.level().clip(new net.minecraft.world.level.ClipContext(
                from, target,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                owner));
        if (hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS) {
            return target;
        }
        Vec3 loc = hit.getLocation();
        // Stop slightly short of the hit face so the tip does not embed in the block.
        Vec3 dir = loc.subtract(from);
        double len = dir.length();
        if (len < 0.25D) {
            return from;
        }
        return from.add(dir.scale((len - 0.25D) / len));
    }

    public boolean toggleMagnet() {
        if (!getPassengers().isEmpty()) {
            Entity passenger = getPassengers().getFirst();
            if (passenger instanceof MountedBlockEntity mounted && !mounted.canRelease()) {
                return false;
            }
            double y = passenger.getY();
            passenger.stopRiding();
            passenger.setPos(passenger.getX(), y, passenger.getZ());
            return true;
        }
        if (level().isClientSide) {
            return false;
        }

        Entity target = null;
        if (OCCommonConfig.CRANE_PICK_ENTITIES.get()) {
            target = findEntityToPick();
        }
        if (target == null && OCCommonConfig.CRANE_PICK_BLOCKS.get()
                && level() instanceof ServerLevel serverLevel) {
            target = createMountedBlock(serverLevel);
        }
        if (target != null) {
            target.startRiding(this, true);
            return true;
        }
        return false;
    }

    public void releaseCargoQuietly() {
        for (Entity passenger : List.copyOf(getPassengers())) {
            passenger.stopRiding();
        }
    }

    @Nullable
    private Entity findEntityToPick() {
        List<Entity> targets = detectTargets();
        return targets.isEmpty() ? null : targets.getFirst();
    }

    private List<Entity> detectTargets() {
        AABB box = getBoundingBox().inflate(0.25D, 0.0D, 0.25D).move(0.0D, -1.0D, 0.0D);
        Player owner = getOwner();
        return level().getEntities(this, box, e -> MagnetPickup.canPickEntity(e, owner));
    }

    @Nullable
    private Entity createMountedBlock(ServerLevel level) {
        Player owner = getOwner();
        if (owner == null) {
            return null;
        }
        int x = Mth.floor(getX());
        int y = Mth.floor(getY() - 0.5D);
        int z = Mth.floor(getZ());
        net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(x, y, z);
        if (!level.isLoaded(pos) || level.getBlockState(pos).isAir()) {
            return null;
        }
        MountedBlockEntity mounted = MountedBlockEntity.create(owner, level, pos);
        if (mounted != null) {
            mounted.moveTo(getX(), getY(), getZ());
            level.addFreshEntity(mounted);
        }
        return mounted;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            entityData.set(DATA_OWNER, Optional.of(tag.getUUID("Owner")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        entityData.get(DATA_OWNER).ifPresent(uuid -> tag.putUUID("Owner", uuid));
    }
}
