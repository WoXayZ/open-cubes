package dev.opencubes.content.fan;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Pushes entities in a cone in front of itself. Force scales with redstone power.
 *
 * <p>The yaw is free-floating in ten-degree steps rather than a block state, because the
 * original let you nudge it with a click and a cardinal facing is too coarse for that.
 */
public class FanBlockEntity extends BlockEntity {

    private static final double CONE_HALF_APERTURE = 0.6D;
    private static final float DEGREES_PER_POWER = 45.0F;

    private float yaw;
    private int power;
    private float bladeRotation;
    private float bladeSpeed;

    public FanBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.FAN.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FanBlockEntity fan) {
        float redstone = fan.power / 15.0F;
        fan.bladeSpeed = DEGREES_PER_POWER * redstone;
        fan.bladeRotation = (fan.bladeRotation + fan.bladeSpeed) % 360.0F;

        double maxForce = OCCommonConfig.FAN_FORCE.get() * redstone;
        if (maxForce <= 0.0D) {
            return;
        }

        double range = OCCommonConfig.FAN_RANGE.get();
        List<Entity> entities = level.getEntities(null, fan.searchBox(range));
        if (entities.isEmpty()) {
            return;
        }

        double radians = Math.toRadians(fan.yaw - 90.0F);
        Vec3 apex = fan.coneApex(radians);
        Vec3 base = fan.coneBase(radians, range);
        Vec3 axis = base.subtract(apex);

        for (Entity entity : entities) {
            Vec3 toEntity = entity.position().subtract(apex);
            if (!insideCone(axis, toEntity, CONE_HALF_APERTURE)) {
                continue;
            }

            double distance = toEntity.length();
            double force = (1.0D - distance / range) * maxForce;
            if (force <= 0.0D) {
                continue;
            }

            Vec3 normal = toEntity.normalize();
            entity.setDeltaMovement(entity.getDeltaMovement().add(force * normal.x, 0.0D, force * normal.z));
            entity.hurtMarked = true;
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, FanBlockEntity fan) {
        float redstone = fan.power / 15.0F;
        fan.bladeSpeed = DEGREES_PER_POWER * redstone;
        fan.bladeRotation = (fan.bladeRotation + fan.bladeSpeed) % 360.0F;
    }

    private AABB searchBox(double range) {
        return new AABB(worldPosition).inflate(range).expandTowards(0.0D, 2.0D, 0.0D).expandTowards(0.0D, -2.0D, 0.0D);
    }

    private Vec3 coneApex(double radians) {
        return Vec3.atLowerCornerOf(worldPosition).add(
                0.5D - Math.cos(radians) * 1.1D,
                0.5D,
                0.5D - Math.sin(radians) * 1.1D);
    }

    private Vec3 coneBase(double radians, double range) {
        return Vec3.atCenterOf(worldPosition).add(
                Math.cos(radians) * range,
                0.0D,
                Math.sin(radians) * range);
    }

    private static boolean insideCone(Vec3 axis, Vec3 toTarget, double halfAperture) {
        double length = toTarget.length() * axis.length();
        if (length <= 1.0E-6D) {
            return false;
        }
        return toTarget.dot(axis) / length > Math.cos(halfAperture);
    }

    public void nudge(boolean reverse) {
        yaw = Mth.wrapDegrees(yaw + (reverse ? -10.0F : 10.0F));
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Pushes the new angle to clients straight away. Waiting for a power change to carry it left
     * freshly placed fans drawn at zero while they blew somewhere else.
     */
    public void setYaw(float yaw) {
        this.yaw = Mth.wrapDegrees(yaw);
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void updatePower() {
        if (level == null || level.isClientSide()) {
            return;
        }
        int next = OCCommonConfig.FAN_NEEDS_REDSTONE.get()
                ? level.getBestNeighborSignal(worldPosition)
                : 15;
        if (next != power) {
            power = next;
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public float getYaw() {
        return yaw;
    }

    public float getBladeRotation(float partialTick) {
        return (bladeRotation + bladeSpeed * partialTick) % 360.0F;
    }

    public int getPower() {
        return power;
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        yaw = tag.getFloatOr("Yaw", 0.0F);
        power = tag.getByteOr("Power", (byte) 0);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putFloat("Yaw", yaw);
        tag.putByte("Power", (byte) power);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
