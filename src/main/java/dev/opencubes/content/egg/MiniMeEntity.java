package dev.opencubes.content.egg;

import com.mojang.authlib.GameProfile;
import dev.opencubes.registry.OCEntityData;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class MiniMeEntity extends PathfinderMob {

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(MiniMeEntity.class, OCEntityData.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> OWNER_NAME =
            SynchedEntityData.defineId(MiniMeEntity.class, EntityDataSerializers.STRING);

    public MiniMeEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_UUID, Optional.empty());
        builder.define(OWNER_NAME, "Steve");
    }

    private int pickupCooldown;
    private boolean wasRidden;

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MiniMePickupPlayerGoal(this));
        goalSelector.addGoal(2, new MiniMeBreakPlantsGoal(this));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (pickupCooldown > 0) {
            pickupCooldown--;
        }
        if (wasRidden && !isVehicle()) {
            wasRidden = false;
            pickupCooldown = 1200;
        } else if (isVehicle()) {
            wasRidden = true;
        }
    }

    public int pickupCooldown() {
        return pickupCooldown;
    }

    /** Blocks accidental owner mounts right after hatch/spawn. */
    public void suppressPickup(int ticks) {
        pickupCooldown = Math.max(pickupCooldown, ticks);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float partialTick) {
        // Must be entity-local. Returning world coords was double-applied and launched riders skyward.
        return new Vec3(0.0D, dimensions.height() + 0.15D, 0.0D);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return pickupCooldown <= 0 && super.canAddPassenger(passenger);
    }

    /**
     * {@link #isBaby()} would also halve the hitbox, which left a full player model on a tiny box
     * nobody could click. The renderer applies the small scale itself.
     */
    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return getType().getDimensions();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        GameProfile owner = ownerProfile();
        if (owner == null || !owner.id().equals(player.getUUID()) || player.getVehicle() == this) {
            return InteractionResult.PASS;
        }
        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        pickupCooldown = 0;
        return player.startRiding(this, true, true) ? InteractionResult.SUCCESS_SERVER : InteractionResult.PASS;
    }

    public void setOwner(GameProfile profile) {
        entityData.set(OWNER_UUID, Optional.of(profile.id()));
        entityData.set(OWNER_NAME, profile.name());
        setCustomName(Component.literal(entityData.get(OWNER_NAME)));
        setCustomNameVisible(true);
    }

    @Nullable
    public GameProfile ownerProfile() {
        Optional<UUID> uuid = entityData.get(OWNER_UUID);
        String name = entityData.get(OWNER_NAME);
        return uuid.map(id -> new GameProfile(id, name)).orElse(null);
    }

    public String ownerName() {
        return entityData.get(OWNER_NAME);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        entityData.get(OWNER_UUID).ifPresent(uuid -> tag.store("OwnerUUID", UUIDUtil.CODEC, uuid));
        tag.putString("OwnerName", entityData.get(OWNER_NAME));
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        tag.read("OwnerUUID", UUIDUtil.CODEC).ifPresent(uuid ->
                setOwner(new GameProfile(uuid, tag.getStringOr("OwnerName", ""))));
    }

    @Override
    public boolean isBaby() {
        return true;
    }
}
