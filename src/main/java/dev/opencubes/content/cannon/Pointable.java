package dev.opencubes.content.cannon;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface Pointable {
    void setTarget(Level level, BlockPos target, Player player);
}
