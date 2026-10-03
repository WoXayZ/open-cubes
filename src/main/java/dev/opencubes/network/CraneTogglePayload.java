package dev.opencubes.network;

import dev.opencubes.OCConstants;
import dev.opencubes.content.crane.CraneBackpackItem;
import dev.opencubes.content.crane.CraneControlItem;
import dev.opencubes.content.crane.CraneRegistry;
import dev.opencubes.content.crane.MagnetEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client swung the crane control (including into empty air). */
public record CraneTogglePayload() implements CustomPacketPayload {

    public static final Type<CraneTogglePayload> TYPE = new Type<>(OCConstants.id("crane_toggle"));
    public static final CraneTogglePayload INSTANCE = new CraneTogglePayload();

    public static final StreamCodec<ByteBuf, CraneTogglePayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    private static final java.util.Map<java.util.UUID, Long> DEBOUNCE =
            new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CraneTogglePayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack main = player.getMainHandItem();
        if (!(main.getItem() instanceof CraneControlItem) || !CraneBackpackItem.isWearing(player)) {
            return;
        }
        long time = player.level().getGameTime();
        Long last = DEBOUNCE.get(player.getUUID());
        if (last != null && time - last <= 5) {
            return;
        }
        DEBOUNCE.put(player.getUUID(), time);
        MagnetEntity magnet = CraneRegistry.INSTANCE.getMagnet(player);
        if (magnet != null) {
            magnet.toggleMagnet();
        }
    }
}
