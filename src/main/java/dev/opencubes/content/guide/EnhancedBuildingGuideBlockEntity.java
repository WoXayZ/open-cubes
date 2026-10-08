package dev.opencubes.content.guide;

import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.util.shapes.Geometry;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Places marker cells from the held block only (right-click with a BlockItem).
 */
public class EnhancedBuildingGuideBlockEntity extends BuildingGuideBlockEntity {

    private int ticks;

    public EnhancedBuildingGuideBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.ENHANCED_BUILDING_GUIDE.get(), pos, state);
    }

    public static void clientTickEnhanced(Level level, BlockPos pos, BlockState state,
                                          EnhancedBuildingGuideBlockEntity guide) {
        clientTick(level, pos, state, guide);
        guide.ticks++;
    }

    public float ticks() {
        return ticks;
    }

    @Override
    protected boolean canAddCoord(int x, int y, int z) {
        return Math.abs(x) > 1 || Math.abs(y) > 1 || Math.abs(z) > 1;
    }

    public boolean tryPlaceWithHeld(ServerPlayer player, ItemStack held, Direction side,
                                    float hitX, float hitY, float hitZ) {
        if (!isActive() || !(held.getItem() instanceof BlockItem) || level == null) {
            return false;
        }

        if (player.getAbilities().instabuild && isFillMode()) {
            return creativeFill(player, held, side);
        }
        return survivalPlaceOne(player, held, side, hitX, hitY, hitZ);
    }

    private boolean isFillMode() {
        return level != null && level.getBlockState(worldPosition.above()).is(Blocks.OBSIDIAN);
    }

    private boolean creativeFill(ServerPlayer player, ItemStack template, Direction side) {
        if (!(level instanceof ServerLevel serverLevel) || !(template.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        boolean any = false;
        for (BlockPos rel : shapeCoords()) {
            BlockPos abs = worldPosition.offset(rel);
            if (!serverLevel.isLoaded(abs) || !serverLevel.getBlockState(abs).canBeReplaced()) {
                continue;
            }
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), side, abs, false);
            BlockPlaceContext ctx = new BlockPlaceContext(
                    new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
            BlockState state = blockItem.getBlock().getStateForPlacement(ctx);
            if (state == null) {
                state = blockItem.getBlock().defaultBlockState();
            }
            if (serverLevel.setBlock(abs, state, 3)) {
                any = true;
            }
        }
        return any;
    }

    private boolean survivalPlaceOne(ServerPlayer player, ItemStack held, Direction side,
                                     float hitX, float hitY, float hitZ) {
        if (!(level instanceof ServerLevel serverLevel) || held.isEmpty()
                || !(held.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        int minY = serverLevel.getMinY();
        int maxY = serverLevel.getMaxY();

        for (BlockPos rel : shapeCoords()) {
            BlockPos abs = worldPosition.offset(rel);
            if (!serverLevel.isLoaded(abs)
                    || !serverLevel.getBlockState(abs).canBeReplaced()
                    || abs.getY() < minY
                    || abs.getY() >= maxY) {
                continue;
            }

            ItemStack toPlace = player.getAbilities().instabuild ? held.copyWithCount(1) : held;
            BlockHitResult hit = new BlockHitResult(
                    new Vec3(abs.getX() + hitX, abs.getY() + hitY, abs.getZ() + hitZ),
                    side, abs, false);
            UseOnContext use = new UseOnContext(serverLevel, player, InteractionHand.MAIN_HAND, toPlace, hit);
            InteractionResult result = blockItem.place(new BlockPlaceContext(use));
            if (result.consumesAction()) {
                spawnPlaceFx(serverLevel, abs);
                return true;
            }
            return false;
        }
        return false;
    }

    private void spawnPlaceFx(ServerLevel level, BlockPos placed) {
        BlockState state = level.getBlockState(placed);
        Geometry.line3D(
                worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                placed.getX(), placed.getY(), placed.getZ(),
                (x, y, z) -> {
                    double dx = x + 0.5;
                    double dy = y + 0.5;
                    double dz = z + 0.5;
                    var random = ThreadLocalRandom.current();
                    for (int i = 0; i < 5; i++) {
                        double px = dx + 0.3 * random.nextDouble();
                        double py = dy + 0.3 * random.nextDouble();
                        double pz = dz + 0.3 * random.nextDouble();
                        level.sendParticles(ParticleTypes.PORTAL, true, true, px, py, pz, 8, 0.25, 0.45, 0.25, 1.0);
                        level.sendParticles(
                                new BlockParticleOption(ParticleTypes.BLOCK, state),
                                px, py, pz, 1, 0, 0, 0, 0);
                    }
                });
    }
}
