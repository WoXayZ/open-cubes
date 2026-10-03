package dev.opencubes.content.trap;

import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.registry.OCSounds;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Arms when open, snaps shut on the next mob that walks over it, and holds that mob in place
 * until a player clicks the trap or a redstone signal locks it open.
 */
public class BearTrapBlockEntity extends BlockEntity {

    public static final int OPENING_TICKS = 15;

    private boolean shut = true;
    private boolean locked;
    private int ticksSinceOpened;
    @Nullable
    private UUID trappedId;
    @Nullable
    private Entity trappedCache;

    public BearTrapBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.BEAR_TRAP.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BearTrapBlockEntity trap) {
        trap.ticksSinceOpened++;
        trap.immobilise();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BearTrapBlockEntity trap) {
        if (!trap.shut) {
            trap.ticksSinceOpened++;
        }
    }

    private void immobilise() {
        if (!shut) {
            return;
        }

        Entity trapped = resolveTrapped();
        if (trapped == null || !trapped.isAlive()) {
            // Freshly placed traps start shut with nobody inside, then open with the animation.
            open();
            return;
        }

        double x = worldPosition.getX() + 0.5D;
        double y = worldPosition.getY();
        double z = worldPosition.getZ() + 0.5D;
        trapped.setPos(x, y, z);
        trapped.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        trapped.hurtMarked = true;
    }

    public void onEntityInside(Entity entity) {
        if (level == null || level.isClientSide) {
            return;
        }
        if (entity instanceof Mob && !locked && !shut && ticksSinceOpened > OPENING_TICKS) {
            close(entity);
        }
    }

    public void close(Entity trapped) {
        if (shut || level == null) {
            return;
        }
        shut = true;
        trappedId = trapped.getUUID();
        trappedCache = trapped;
        level.setBlock(worldPosition, getBlockState().setValue(BearTrapBlock.SHUT, true), Block.UPDATE_ALL);
        level.playSound(null, worldPosition, OCSounds.BEAR_TRAP_CLOSE.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void open() {
        if (!shut || level == null) {
            return;
        }
        shut = false;
        trappedId = null;
        trappedCache = null;
        ticksSinceOpened = 0;
        level.setBlock(worldPosition, getBlockState().setValue(BearTrapBlock.SHUT, false), Block.UPDATE_ALL);
        level.playSound(null, worldPosition, OCSounds.BEAR_TRAP_OPEN.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void updateRedstone() {
        if (level == null || level.isClientSide) {
            return;
        }
        boolean next = level.getBestNeighborSignal(worldPosition) > 0;
        locked = next;
        if (locked) {
            open();
        }
        setChanged();
    }

    public boolean isShut() {
        return shut;
    }

    public int comparatorSignal() {
        Entity trapped = resolveTrapped();
        if (trapped == null) {
            return 0;
        }
        return Mth.ceil(trapped.getBoundingBox().getSize() / 16.0D);
    }

    @Nullable
    private Entity resolveTrapped() {
        if (trappedId == null || level == null) {
            return null;
        }
        if (trappedCache != null && trappedId.equals(trappedCache.getUUID()) && trappedCache.isAlive()) {
            return trappedCache;
        }
        for (Entity entity : level.getEntities(null, new net.minecraft.world.phys.AABB(worldPosition).inflate(2.0D))) {
            if (trappedId.equals(entity.getUUID())) {
                trappedCache = entity;
                return entity;
            }
        }
        return null;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        shut = tag.getBoolean("Shut");
        locked = tag.getBoolean("Locked");
        ticksSinceOpened = tag.getInt("TicksOpen");
        trappedId = tag.hasUUID("Trapped") ? tag.getUUID("Trapped") : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Shut", shut);
        tag.putBoolean("Locked", locked);
        tag.putInt("TicksOpen", ticksSinceOpened);
        if (trappedId != null) {
            tag.putUUID("Trapped", trappedId);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
