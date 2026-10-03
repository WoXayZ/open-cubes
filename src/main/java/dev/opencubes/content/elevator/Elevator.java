package dev.opencubes.content.elevator;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A block that participates in elevator travel. Internal on purpose: OpenCubes makes no
 * public API commitment, so other mods hook in through block tags rather than this type.
 */
public interface Elevator {

    DyeColor elevatorColour(BlockState state);

    /**
     * The direction the player should face after arriving, or {@code null} to leave their
     * view alone.
     */
    @Nullable
    default Direction arrivalFacing(BlockState state) {
        return null;
    }
}
