package dev.opencubes.content.target;

import dev.opencubes.OCConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

/**
 * Backup path for arrow hits on thin target shapes when {@code onProjectileHit} is skipped.
 */
@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class ArcheryTargetEvents {

    private ArcheryTargetEvents() {}

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow)) {
            return;
        }
        HitResult hit = event.getRayTraceResult();
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockHitResult blockHit = (BlockHitResult) hit;
        Level level = event.getProjectile().level();
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ArcheryTargetBlock) {
            ArcheryTargetBlock.handleArrowHit(level, pos, state, blockHit.getLocation());
        }
    }
}
