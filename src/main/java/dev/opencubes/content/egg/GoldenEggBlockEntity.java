package dev.opencubes.content.egg;

import com.mojang.authlib.GameProfile;
import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.content.crane.MagnetPickup;
import dev.opencubes.content.crane.MountedBlockEntity;
import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.registry.OCEntities;
import dev.opencubes.util.OCFakePlayers;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.state.BlockState;
/**
 * OpenBlocks golden egg hatch: spin stages accelerate, then the egg rises with light rays, falls,
 * and spawns a Mini Me.
 */
public class GoldenEggBlockEntity extends BlockEntity {

    private static final GameProfile FALLBACK =
            new GameProfile(UUID.fromString("d38ae975-7b6d-4b86-9cf6-fb034cb07481"), "Mr Glitch");

    private static final float SPEED_CHANGE_RATE = 0.1F;
    private static final int STAGE_CHANGE_TICK = 100;
    private static final int RISING_TIME = 400;
    private static final int FALLING_TIME = 10;
    public static final float MAX_HEIGHT = 5.0F;
    private static final double STAGE_CHANGE_CHANCE = 0.8D;

    public enum Phase {
        IDLE(0.0F, 0.0F, false),
        ROTATING_SLOW(1.0F, 0.0F, false),
        ROTATING_MEDIUM(10.0F, 0.0F, false),
        ROTATING_FAST(50.0F, 0.0F, false),
        FLOATING(100.0F, 1.0F / RISING_TIME, true),
        FALLING(150.0F, -1.0F / FALLING_TIME, true);

        public final float rotationSpeed;
        public final float progressSpeed;
        public final boolean specialEffects;

        Phase(float rotationSpeed, float progressSpeed, boolean specialEffects) {
            this.rotationSpeed = rotationSpeed;
            this.progressSpeed = progressSpeed;
            this.specialEffects = specialEffects;
        }
    }

    @Nullable
    private GameProfile owner = FALLBACK;
    private Phase phase = Phase.IDLE;
    private int phaseTicks;

    /** Client-smoothed animation (mirrors OpenBlocks tile fields). */
    private float rotation;
    private float rotationSpeed;
    private float riseProgress;
    private float riseSpeed;

    private final List<MountedBlockEntity> carriedBlocks = new ArrayList<>();

    public GoldenEggBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.GOLDEN_EGG.get(), pos, state);
    }

    public void setOwner(GameProfile profile) {
        this.owner = profile;
        setChanged();
    }

    @Nullable
    public GameProfile owner() {
        return owner;
    }

    public Phase phase() {
        return phase;
    }

    public boolean isIdle() {
        return phase == Phase.IDLE;
    }

    public float getRotation(float partialTick) {
        return rotation + rotationSpeed * partialTick;
    }

    public float getRiseProgress(float partialTick) {
        return Mth.clamp(riseProgress + riseSpeed * partialTick, 0.0F, 1.0F);
    }

    public float getOffset(float partialTick) {
        return getRiseProgress(partialTick) * MAX_HEIGHT;
    }

    /** Starts the OpenBlocks hatch sequence (spin → rise → fall → Mini Me). */
    public boolean beginHatch() {
        if (level == null || level.isClientSide() || phase != Phase.IDLE) {
            return false;
        }
        enterPhase(Phase.ROTATING_SLOW);
        level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.SNIFFER_EGG_CRACK,
                net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.8F);
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GoldenEggBlockEntity egg) {
        if (egg.phase == Phase.IDLE) {
            return;
        }
        if (level.isClientSide()) {
            egg.clientAnimate();
            return;
        }
        egg.serverTick((ServerLevel) level);
    }

    private void clientAnimate() {
        rotationSpeed = (1.0F - SPEED_CHANGE_RATE) * rotationSpeed
                + SPEED_CHANGE_RATE * phase.rotationSpeed;
        rotation += rotationSpeed;

        riseSpeed = (1.0F - SPEED_CHANGE_RATE) * riseSpeed
                + SPEED_CHANGE_RATE * phase.progressSpeed;
        riseProgress = Mth.clamp(riseProgress + riseSpeed, 0.0F, 1.0F);
        phaseTicks++;
    }

    private void serverTick(ServerLevel level) {
        phaseTicks++;
        // Keep rise height authoritative on the server for carried blocks.
        riseSpeed = (1.0F - SPEED_CHANGE_RATE) * riseSpeed
                + SPEED_CHANGE_RATE * phase.progressSpeed;
        riseProgress = Mth.clamp(riseProgress + riseSpeed, 0.0F, 1.0F);

        switch (phase) {
            case ROTATING_SLOW -> tryAdvance(Phase.ROTATING_MEDIUM);
            case ROTATING_MEDIUM -> tryAdvance(Phase.ROTATING_FAST);
            case ROTATING_FAST -> tryAdvance(Phase.FLOATING);
            case FLOATING -> {
                tryPickNearbyBlock(level);
                updateCarriedPositions();
                if (phaseTicks >= RISING_TIME) {
                    enterPhase(Phase.FALLING);
                }
            }
            case FALLING -> {
                if (phaseTicks >= FALLING_TIME) {
                    hatch(level);
                }
            }
            default -> {
            }
        }
    }

    private void tryAdvance(Phase next) {
        if (phaseTicks > 0 && phaseTicks % STAGE_CHANGE_TICK == 0
                && level != null && level.getRandom().nextDouble() < STAGE_CHANGE_CHANCE) {
            enterPhase(next);
        }
    }

    private void enterPhase(Phase next) {
        if (next == Phase.FLOATING) {
            riseProgress = 0.0F;
            riseSpeed = 0.0F;
            phaseTicks = 0;
        } else if (next == Phase.FALLING) {
            dropCarriedBlocks();
            riseProgress = 1.0F;
        }
        phase = next;
        phaseTicks = 0;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void tryPickNearbyBlock(ServerLevel level) {
        if (!OCCommonConfig.SPEC.isLoaded() || !OCCommonConfig.GOLDEN_EGG_PICK_BLOCKS.get()) {
            return;
        }
        if (level.getRandom().nextInt(6) != 0) {
            return;
        }
        BlockPos target = worldPosition.offset(
                level.getRandom().nextInt(20) - 10,
                level.getRandom().nextInt(2) - 1,
                level.getRandom().nextInt(20) - 10);
        if (!MagnetPickup.canPickBlock(level, target)) {
            return;
        }
        MountedBlockEntity mounted = MountedBlockEntity.create(OCFakePlayers.get(level), level, target);
        if (mounted == null) {
            return;
        }
        mounted.setSuspended(true);
        level.addFreshEntity(mounted);
        carriedBlocks.add(mounted);
    }

    private void updateCarriedPositions() {
        pruneCarriedBlocks();
        double centerX = worldPosition.getX() + 0.5D;
        double centerY = worldPosition.getY() + getOffset(0.0F) + 0.8D;
        double centerZ = worldPosition.getZ() + 0.5D;
        int count = carriedBlocks.size();
        for (int i = 0; i < count; i++) {
            MountedBlockEntity block = carriedBlocks.get(i);
            double angle = i * (Math.PI * 2.0D / Math.max(count, 1));
            block.setPos(
                    centerX + Math.cos(angle) * 0.6D,
                    centerY + i * 0.3D,
                    centerZ + Math.sin(angle) * 0.6D);
        }
    }

    private void dropCarriedBlocks() {
        for (MountedBlockEntity block : carriedBlocks) {
            if (block.isAlive()) {
                block.releaseFromSuspension();
            }
        }
        carriedBlocks.clear();
    }

    private void pruneCarriedBlocks() {
        Iterator<MountedBlockEntity> it = carriedBlocks.iterator();
        while (it.hasNext()) {
            if (!it.next().isAlive()) {
                it.remove();
            }
        }
    }

    private void hatch(ServerLevel level) {
        dropCarriedBlocks();
        GameProfile profile = owner == null ? FALLBACK : owner;
        MiniMeEntity mini = OCEntities.MINI_ME.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mini != null) {
            double x = worldPosition.getX() + 0.5D;
            double y = worldPosition.getY() + 0.5D;
            double z = worldPosition.getZ() + 0.5D;
            mini.setPos(x, y, z);
            mini.setYRot(0.0F);
            mini.setXRot(0.0F);
            mini.setOwner(profile);
            mini.suppressPickup(80);
        }
        // Remove the egg first, then spawn the mini-me after the blast.
        level.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), 3);
        level.explode(null, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D,
                2.0F, Level.ExplosionInteraction.TNT);
        if (mini != null) {
            level.addFreshEntity(mini);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putString("Phase", phase.name());
        tag.putInt("PhaseTicks", phaseTicks);
        tag.putFloat("RiseProgress", riseProgress);
        if (owner != null) {
            tag.store("OwnerUUID", UUIDUtil.CODEC, owner.id());
            tag.putString("OwnerName", owner.name());
        }
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        phaseTicks = tag.getIntOr("PhaseTicks", 0);
        riseProgress = tag.getFloatOr("RiseProgress", 0.0F);
        if (tag.keySet().contains("Phase")) {
            try {
                phase = Phase.valueOf(tag.getStringOr("Phase", ""));
            } catch (IllegalArgumentException ignored) {
                phase = Phase.IDLE;
            }
        } else {
            // Legacy progress-based save
            int progress = tag.getIntOr("Progress", 0);
            if (progress <= 0) {
                phase = Phase.IDLE;
            } else if (progress < 200) {
                phase = Phase.ROTATING_MEDIUM;
            } else if (progress < 600) {
                phase = Phase.FLOATING;
                riseProgress = Math.min(1.0F, (progress - 200) / 400.0F);
            } else {
                phase = Phase.FALLING;
                riseProgress = 1.0F;
            }
        }
        tag.read("OwnerUUID", UUIDUtil.CODEC).ifPresent(uuid ->
                owner = new GameProfile(uuid, tag.getStringOr("OwnerName", "")));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ValueInput input) {
        loadAdditional(input);
    }
}
