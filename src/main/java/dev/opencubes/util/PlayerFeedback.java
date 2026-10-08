package dev.opencubes.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Action-bar and chat lines. {@code Player.displayClientMessage} is gone in 26.1. */
public final class PlayerFeedback {

    private PlayerFeedback() {}

    public static void tell(Player player, Component message, boolean actionBar) {
        if (!(player instanceof ServerPlayer server)) {
            return;
        }
        if (actionBar) {
            server.connection.send(new ClientboundSetActionBarTextPacket(message));
        } else {
            server.sendSystemMessage(message);
        }
    }
}
