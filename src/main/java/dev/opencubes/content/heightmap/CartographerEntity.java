package dev.opencubes.content.heightmap;

import dev.opencubes.content.heightmap.HeightMapBuilder.ChunkJob;
import dev.opencubes.registry.OCEntities;
import dev.opencubes.registry.OCItems;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Flying mapping assistant. Right-click with an empty/height map to start scanning loaded chunks;
 * empty-hand right-click to retrieve the map.
 */
public class CartographerEntity extends Entity {

    private static final int MAP_JOB_DELAY = 5;
    private static final int MOVE_DELAY = 35;

    private static final EntityDataAccessor<Boolean> DATA_MAPPING =
            SynchedEntityData.defineId(CartographerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER =
            SynchedEntityData.defineId(CartographerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> DATA_JOBS_DONE =
            SynchedEntityData.defineId(CartographerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_JOBS_TOTAL =
            SynchedEntityData.defineId(CartographerEntity.class, EntityDataSerializers.INT);

    @Nullable
    private UUID ownerUuid;
    private ItemStack mapItem = ItemStack.EMPTY;
    private String mappingDimension = "";
    private final BitSet finishedBits = new BitSet();
    @Nullable
    private Set<ChunkJob> jobs;
    private int countdownToAction = MAP_JOB_DELAY;
    private int countdownToMove = MOVE_DELAY;
    private float randomYaw;

    public CartographerEntity(EntityType<? extends CartographerEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        noPhysics = true;
    }

    public CartographerEntity(Level level) {
        this(OCEntities.CARTOGRAPHER.get(), level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_MAPPING, false);
        builder.define(DATA_OWNER, Optional.empty());
        builder.define(DATA_JOBS_DONE, 0);
        builder.define(DATA_JOBS_TOTAL, 0);
    }

    public void setOwner(Player player) {
        this.ownerUuid = player.getUUID();
        entityData.set(DATA_OWNER, Optional.of(player.getUUID()));
    }

    public Optional<UUID> ownerUuid() {
        return entityData.get(DATA_OWNER);
    }

    public int mappingJobsDone() {
        return entityData.get(DATA_JOBS_DONE);
    }

    public int mappingJobsTotal() {
        return entityData.get(DATA_JOBS_TOTAL);
    }

    public int mappingPercent() {
        int total = mappingJobsTotal();
        if (total <= 0) {
            return 0;
        }
        return mappingJobsDone() * 100 / total;
    }

    private void syncMappingProgress() {
        int done = finishedBits.cardinality();
        int total = done + (jobs != null ? jobs.size() : 0);
        entityData.set(DATA_JOBS_DONE, done);
        entityData.set(DATA_JOBS_TOTAL, total);
    }

    public void loadFromItem(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        if (!tag.isEmpty()) {
            readExtra(tag);
        }
    }

    public boolean isMapping() {
        return entityData.get(DATA_MAPPING);
    }

    private void setMapping(boolean mapping) {
        entityData.set(DATA_MAPPING, mapping);
    }

    @Nullable
    private Player findOwner() {
        if (ownerUuid == null) {
            return null;
        }
        return level().getPlayerByUUID(ownerUuid);
    }

    @Override
    public void tick() {
        super.tick();
        Player owner = findOwner();
        if (!level().isClientSide) {
            float yaw;
            if (isMapping()) {
                if (--countdownToMove <= 0) {
                    countdownToMove = MOVE_DELAY;
                    randomYaw = (float) (2 * Math.PI * getRandom().nextFloat());
                }
                yaw = randomYaw;
            } else {
                yaw = owner != null ? (float) Math.toRadians(owner.getYRot()) : 0.0F;
            }
            double ox = Mth.sin(-yaw);
            double oz = Mth.cos(-yaw);

            if (owner != null) {
                Vec3 target = owner.getEyePosition().add(ox, 0.2D, oz);
                Vec3 delta = target.subtract(position()).scale(0.15D);
                setDeltaMovement(delta);
                move(MoverType.SELF, getDeltaMovement());
                setYRot(owner.getYRot());
            }

            if (isMapping()
                    && level() instanceof ServerLevel serverLevel
                    && serverLevel.dimension().location().toString().equals(mappingDimension)
                    && --countdownToAction <= 0) {
                runJob(serverLevel);
                countdownToAction = MAP_JOB_DELAY;
            }
        } else if (owner != null) {
            setPos(position().add(owner.getEyePosition().subtract(position()).scale(0.1D)));
        }
    }

    private void runJob(ServerLevel level) {
        if (jobs == null) {
            jobs = new HashSet<>();
        }
        ChunkJob job = HeightMapBuilder.doNextChunk(level, getX(), getZ(), jobs);
        if (job != null) {
            jobs.remove(job);
            finishedBits.set(job.bitNum);
            syncMappingProgress();
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || distanceTo(player) >= 5.0F) {
            return InteractionResult.PASS;
        }
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack holding = player.getMainHandItem();
        // Empty hand: retrieve the map.
        if (holding.isEmpty() && !mapItem.isEmpty()) {
            player.setItemInHand(hand, mapItem);
            mapItem = ItemStack.EMPTY;
            setMapping(false);
            stopMapping();
            return InteractionResult.SUCCESS;
        }
        // Give empty/height map — sneak not required (sneaking often aims past the hitbox at a block).
        if (!holding.isEmpty() && mapItem.isEmpty()
                && (holding.getItem() instanceof HeightMapItem || holding.getItem() instanceof EmptyMapItem)
                && level() instanceof ServerLevel serverLevel) {
            mapItem = holding.split(1);
            mappingDimension = serverLevel.dimension().location().toString();
            mapItem = HeightMapBuilder.upgradeToMap(serverLevel, mapItem);
            setMapping(true);
            int mapId = HeightMapItem.getMapId(mapItem);
            if (mapId >= 0) {
                startMapping(serverLevel, mapId);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void startMapping(ServerLevel level, int mapId) {
        HeightMapBuilder builder = new HeightMapBuilder(mapId);
        builder.resetMap(level, getNewMapCenterX(), getNewMapCenterZ());
        builder.resize(finishedBits);
        jobs = builder.createJobs(finishedBits);
        syncMappingProgress();
    }

    private void resumeMapping(ServerLevel level, int mapId) {
        HeightMapBuilder builder = new HeightMapBuilder(mapId);
        builder.loadMap(level);
        builder.resizeIfNeeded(finishedBits);
        jobs = builder.createJobs(finishedBits);
        syncMappingProgress();
    }

    private void stopMapping() {
        if (jobs != null) {
            jobs.clear();
        }
        finishedBits.clear();
        syncMappingProgress();
    }

    private int getNewMapCenterX() {
        return ((int) getX()) & ~0x0F;
    }

    private int getNewMapCenterZ() {
        return ((int) getZ()) & ~0x0F;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && !isRemoved()) {
            spawnAtLocation(toItemStack());
            discard();
        }
        return true;
    }

    public ItemStack toItemStack() {
        ItemStack result = new ItemStack(OCItems.CARTOGRAPHER.get());
        CompoundTag tag = new CompoundTag();
        writeExtra(tag);
        result.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(tag));
        return result;
    }

    private void writeExtra(CompoundTag tag) {
        tag.putBoolean("Mapping", isMapping());
        tag.putByteArray("Bits", finishedBits.toByteArray());
        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }
        if (!mapItem.isEmpty()) {
            tag.put("MapItem", mapItem.save(level().registryAccess()));
            tag.putString("Dimension", mappingDimension);
        }
    }

    private void readExtra(CompoundTag tag) {
        setMapping(tag.getBoolean("Mapping"));
        finishedBits.clear();
        if (tag.contains("Bits")) {
            BitSet loaded = BitSet.valueOf(tag.getByteArray("Bits"));
            finishedBits.or(loaded);
        }
        if (tag.hasUUID("Owner")) {
            ownerUuid = tag.getUUID("Owner");
            entityData.set(DATA_OWNER, Optional.of(ownerUuid));
        }
        if (tag.contains("MapItem") && level() instanceof ServerLevel serverLevel) {
            Optional<ItemStack> loaded = ItemStack.parse(level().registryAccess(), tag.getCompound("MapItem"));
            mapItem = loaded.orElse(ItemStack.EMPTY);
            mappingDimension = tag.getString("Dimension");
            if (!mapItem.isEmpty() && isMapping()) {
                int mapId = HeightMapItem.getMapId(mapItem);
                if (mapId >= 0) {
                    resumeMapping(serverLevel, mapId);
                }
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        readExtra(tag);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        writeExtra(tag);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
}
