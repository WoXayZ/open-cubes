package dev.opencubes.client.imaginary;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/** Local player, used when a raycast has no entity. */
public final class ImaginaryClient {

    private ImaginaryClient() {}

    @Nullable
    public static Player player() {
        return Minecraft.getInstance().player;
    }
}
