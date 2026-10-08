package dev.opencubes.content.crane;

import dev.opencubes.registry.OCTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Eligibility for magnet grab - tags first, then sensible vanilla defaults. */
public final class MagnetPickup {

    private MagnetPickup() {}

    public static boolean canPickEntity(Entity entity, @org.jetbrains.annotations.Nullable Entity owner) {
        if (entity == owner || entity instanceof MagnetEntity || entity instanceof MountedBlockEntity) {
            return false;
        }
        if (entity.getType().builtInRegistryHolder().is(OCTags.EntityTypes.MAGNET_BLACKLIST)) {
            return false;
        }
        if (entity.getType().builtInRegistryHolder().is(OCTags.EntityTypes.MAGNET_LIFTABLE)) {
            return true;
        }
        if (entity instanceof LivingEntity) {
            return true;
        }
        return entity instanceof ItemEntity
                || entity instanceof Boat
                || entity instanceof AbstractMinecart;
    }

    public static boolean canPickBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(OCTags.Blocks.MAGNET_BLACKLIST)) {
            return false;
        }
        if (state.is(OCTags.Blocks.MAGNET_LIFTABLE)) {
            return true;
        }
        if (state.hasBlockEntity()) {
            BlockEntity be = level.getBlockEntity(pos);
            return be != null && be.getType().builtInRegistryHolder().is(OCTags.BlockEntityTypes.MAGNET_LIFTABLE);
        }
        if (state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        if (state.getRenderShape() == RenderShape.MODEL) {
            return true;
        }
        return state.is(BlockTags.SAND)
                || state.is(BlockTags.STAIRS)
                || state.is(BlockTags.FENCES)
                || state.is(BlockTags.FENCE_GATES)
                || state.is(net.minecraft.world.level.block.Blocks.CACTUS);
    }
}
