package dev.opencubes.content.goldeneye;

import dev.opencubes.registry.OCEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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

        if (level().isClientSide) {
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

        if (!level().isClientSide && life >= MAX_LIFE) {
            dropAndDiscard();
        }
    }

    private void dropAndDiscard() {
        if (!stack.isEmpty()) {
            spawnAtLocation(stack);
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.ENDER_EYE_DEATH,
                    getSoundSource(), 1.0F, 1.0F);
        }
        discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Item")) {
            stack = ItemStack.parse(level().registryAccess(), tag.getCompound("Item")).orElse(ItemStack.EMPTY);
        }
        if (tag.contains("TX")) {
            target = new BlockPos(tag.getInt("TX"), tag.getInt("TY"), tag.getInt("TZ"));
        }
        life = tag.getInt("Life");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (!stack.isEmpty()) {
            tag.put("Item", stack.save(level().registryAccess()));
        }
        if (target != null) {
            tag.putInt("TX", target.getX());
            tag.putInt("TY", target.getY());
            tag.putInt("TZ", target.getZ());
        }
        tag.putInt("Life", life);
    }
}
