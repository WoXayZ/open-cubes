package dev.opencubes.content.tomfoolery;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface FlimFlamAction {
    boolean execute(ServerPlayer player);
}
