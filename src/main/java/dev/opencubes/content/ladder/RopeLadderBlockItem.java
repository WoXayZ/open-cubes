package dev.opencubes.content.ladder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Redirects top-face edge clicks onto the nearest vertical face so the ladder places beside
 * the block (and can unroll down) instead of failing {@code canSurvive} in the cell above.
 */
public class RopeLadderBlockItem extends BlockItem {

    public RopeLadderBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() == Direction.UP) {
            Direction edge = nearestTopEdge(context.getClickedPos(), context.getClickLocation());
            if (edge != null) {
                BlockHitResult redirected = new BlockHitResult(
                        context.getClickLocation(),
                        edge,
                        context.getClickedPos(),
                        context.isInside());
                UseOnContext sideContext = new UseOnContext(
                        context.getLevel(),
                        context.getPlayer(),
                        context.getHand(),
                        context.getItemInHand(),
                        redirected);
                InteractionResult result = super.useOn(sideContext);
                if (result.consumesAction()) {
                    return result;
                }
            }
        }
        return super.useOn(context);
    }

    @Nullable
    static Direction nearestTopEdge(BlockPos clicked, Vec3 hit) {
        double x = hit.x - clicked.getX();
        double z = hit.z - clicked.getZ();
        double toWest = x;
        double toEast = 1.0D - x;
        double toNorth = z;
        double toSouth = 1.0D - z;
        double best = Math.min(Math.min(toWest, toEast), Math.min(toNorth, toSouth));
        if (best > 0.35D) {
            return null;
        }
        if (best == toNorth) {
            return Direction.NORTH;
        }
        if (best == toSouth) {
            return Direction.SOUTH;
        }
        if (best == toWest) {
            return Direction.WEST;
        }
        return Direction.EAST;
    }
}
