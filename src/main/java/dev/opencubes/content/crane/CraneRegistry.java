package dev.opencubes.content.crane;

import dev.opencubes.registry.OCEntities;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Per-player crane arm length and magnet binding. */
public final class CraneRegistry {

    public static final double ARM_RADIUS = 2.0;
    public static final double MIN_LENGTH = 0.25;
    public static final double MAX_LENGTH = 10.0;
    public static final double LENGTH_DELTA = 0.1;

    public static final CraneRegistry INSTANCE = new CraneRegistry();

    public static final class Data {
        public boolean isExtending;
        /** Starts slightly extended so the tip is visible below the player. */
        public double length = 1.5;

        public void updateLength() {
            if (isExtending && length < MAX_LENGTH) {
                length += LENGTH_DELTA;
            } else if (!isExtending && length > MIN_LENGTH) {
                length -= LENGTH_DELTA;
            }
        }
    }

    private final Map<UUID, Data> itemData = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> magnetEntityIds = new ConcurrentHashMap<>();

    private CraneRegistry() {}

    public Data getData(LivingEntity player, boolean canCreate) {
        Data data = itemData.get(player.getUUID());
        if (data == null && canCreate) {
            data = new Data();
            itemData.put(player.getUUID(), data);
        }
        return data;
    }

    public double getMagnetDistance(LivingEntity player) {
        Data data = getData(player, false);
        return data != null ? data.length : MIN_LENGTH;
    }

    public void bindMagnet(Player player, MagnetEntity magnet) {
        magnetEntityIds.put(player.getUUID(), magnet.getId());
    }

    public void unbindMagnet(Player player) {
        magnetEntityIds.remove(player.getUUID());
    }

    @Nullable
    public MagnetEntity getMagnet(Player player) {
        Integer id = magnetEntityIds.get(player.getUUID());
        if (id == null) {
            return null;
        }
        Level level = player.level();
        if (level.getEntity(id) instanceof MagnetEntity magnet && magnet.isAlive()) {
            return magnet;
        }
        magnetEntityIds.remove(player.getUUID());
        return null;
    }

    public void ensureMagnet(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        MagnetEntity existing = getMagnet(player);
        if (existing != null && existing.isValid()) {
            return;
        }
        if (existing != null) {
            existing.discard();
        }
        MagnetEntity magnet = OCEntities.MAGNET.get().create(serverLevel);
        if (magnet == null) {
            return;
        }
        magnet.setOwner(player);
        magnet.moveTo(player.getX(), player.getY() + player.getBbHeight(), player.getZ());
        serverLevel.addFreshEntity(magnet);
        bindMagnet(player, magnet);
    }

    public void clearPlayer(Player player) {
        MagnetEntity magnet = getMagnet(player);
        if (magnet != null) {
            magnet.releaseCargoQuietly();
            magnet.discard();
        }
        unbindMagnet(player);
        itemData.remove(player.getUUID());
    }
}
