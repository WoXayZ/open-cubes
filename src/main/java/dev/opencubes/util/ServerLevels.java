package dev.opencubes.util;

import dev.opencubes.util.ServerLevels;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/** {@code ServerLevels.of(Entity)} was removed. Server players already return a server level from {@code level()}. */
public final class ServerLevels {

    private ServerLevels() {}

    public static ServerLevel of(Entity entity) {
        return (ServerLevel) entity.level();
    }
}
