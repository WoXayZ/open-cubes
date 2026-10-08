package dev.opencubes.content.goldeneye;

import dev.opencubes.registry.OCEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class GoldenEyeEntity extends Entity implements ItemSupplier {

    private static final int MAX_LIFE = 60;

    private ItemStack stack = ItemStack.EMPTY;
    @Nullable
    private BlockPos target;
    @Nullable
    private Player owner;
    private int life;

    public GoldenEyeEntity(EntityType<? extends GoldenEyeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public GoldenEyeEntity(Level level) {
        this(OCEntities.GOLDEN_EYE.get(), level);
    }

    public void setStack(ItemStack stack) {
        this.stack = stack.copy();
    }

    public void setTarget(BlockPos target) {
        this.target = target.immutable();
    }

    public void setOwner(@Nullable Player owner) {
        this.owner = owner;
    }

    @Override
    public ItemStack getItem() {
        return stack;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    public void tick() {
        super.tick();
        life++;

        if (level().isClientSide()) {
            level().addParticle(ParticleTypes.PORTAL, getX(), getY(), getZ(),
                    random.nextGaussian() * 0.1D, random.nextGaussian() * 0.1D, random.nextGaussian() * 0.1D);
        }

        if (target != null) {
            Vec3 dest = Vec3.atCenterOf(target);
            Vec3 delta = dest.subtract(position());
            double dist = delta.length();
            if (dist > 0.1D) {
                Vec3 motion = delta.normalize().scale(Math.min(0.5D, dist * 0.2D));
                setDeltaMovement(getDeltaMovement().scale(0.8D).add(motion.scale(0.2D)));
            }
        }

        move(MoverType.SELF, getDeltaMovement());

        if (!level().isClientSide() && life >= MAX_LIFE) {
            dropAndDiscard();
        }
    }

    private void dropAndDiscard() {
        if (!stack.isEmpty() && level() instanceof ServerLevel serverLevel) {
            spawnAtLocation(serverLevel, stack);
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.ENDER_EYE_DEATH,
                    getSoundSource(), 1.0F, 1.0F);
        }
        discard();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (!isInvulnerableToBase(source)) {
            markHurt();
        }
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        stack = tag.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        tag.getInt("TX").ifPresent(x ->
                target = new BlockPos(x, tag.getIntOr("TY", 0), tag.getIntOr("TZ", 0)));
        life = tag.getIntOr("Life", 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        if (!stack.isEmpty()) {
            tag.store("Item", ItemStack.CODEC, stack);
        }
        if (target != null) {
            tag.putInt("TX", target.getX());
            tag.putInt("TY", target.getY());
            tag.putInt("TZ", target.getZ());
        }
        tag.putInt("Life", life);
    }
}
