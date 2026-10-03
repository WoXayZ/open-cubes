package dev.opencubes.client.heightmap;

import dev.opencubes.OCConstants;
import dev.opencubes.content.heightmap.CartographerEntity;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/** Shows mapping progress for the local player's cartographer drone. */
@EventBusSubscriber(modid = OCConstants.MOD_ID, value = Dist.CLIENT)
public final class CartographerProgressOverlay {

    private static final int GRID_SIZE = 8;
    private static final int CELL = 3;

    private CartographerProgressOverlay() {}

    @SubscribeEvent
    public static void onHud(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) {
            return;
        }

        CartographerEntity cartographer = findTarget(mc);
        if (cartographer == null || !cartographer.isMapping()) {
            return;
        }

        int total = cartographer.mappingJobsTotal();
        if (total <= 0) {
            return;
        }

        int done = cartographer.mappingJobsDone();
        int percent = cartographer.mappingPercent();
        GuiGraphics graphics = event.getGuiGraphics();
        int x = mc.getWindow().getGuiScaledWidth() / 2;
        int y = mc.getWindow().getGuiScaledHeight() - 59;

        Component label = Component.translatable("opencubes.misc.cartographer_progress", percent);
        int textWidth = mc.font.width(label);
        graphics.drawString(mc.font, label, x - textWidth / 2, y, 0xFFFFFF, true);

        int gridWidth = GRID_SIZE * CELL;
        int gridX = x - gridWidth / 2;
        int gridY = y + 12;
        int filledCells = done * GRID_SIZE * GRID_SIZE / total;
        for (int i = 0; i < GRID_SIZE * GRID_SIZE; i++) {
            int cellX = gridX + (i % GRID_SIZE) * CELL;
            int cellY = gridY + (i / GRID_SIZE) * CELL;
            int colour = i < filledCells ? 0xFF55AAFF : 0x80333333;
            graphics.fill(cellX, cellY, cellX + CELL - 1, cellY + CELL - 1, colour);
        }
    }

    private static CartographerEntity findTarget(Minecraft mc) {
        HitResult hit = mc.hitResult;
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof CartographerEntity cartographer) {
            if (isOwn(cartographer, mc.player)) {
                return cartographer;
            }
        }
        for (CartographerEntity cartographer : mc.level.getEntitiesOfClass(
                CartographerEntity.class, mc.player.getBoundingBox().inflate(12.0D))) {
            if (isOwn(cartographer, mc.player)) {
                return cartographer;
            }
        }
        return null;
    }

    private static boolean isOwn(CartographerEntity cartographer, Player player) {
        Optional<UUID> owner = cartographer.ownerUuid();
        return owner.isPresent() && owner.get().equals(player.getUUID());
    }
}
