package dev.opencubes.content.guide;

import com.mojang.serialization.MapCodec;
import dev.opencubes.registry.OCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class EnhancedBuildingGuideBlock extends BuildingGuideBlock {

    public static final MapCodec<EnhancedBuildingGuideBlock> CODEC = simpleCodec(EnhancedBuildingGuideBlock::new);

    public EnhancedBuildingGuideBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BuildingGuideBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof EnhancedBuildingGuideBlockEntity guide)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        // Sneak or dye: fall through to dye / open-GUI behaviour.
        if (player.isShiftKeyDown() || DyeColor.getColor(stack) != null) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (guide.isActive() && stack.getItem() instanceof BlockItem) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                Vec3Hit hitLoc = new Vec3Hit(hit);
                if (guide.tryPlaceWithHeld(serverPlayer, stack, hit.getDirection(),
                        hitLoc.x, hitLoc.y, hitLoc.z)) {
                    return ItemInteractionResult.CONSUME;
                }
            }
            // Consume the click so the held block is not placed against the guide.
            return ItemInteractionResult.FAIL;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.7D;
        double z = pos.getZ() + 0.5D;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnhancedBuildingGuideBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, OCBlockEntities.ENHANCED_BUILDING_GUIDE.get(),
                EnhancedBuildingGuideBlockEntity::clientTickEnhanced)
                : null;
    }

    /** Local hit fractions relative to the clicked block cell. */
    private record Vec3Hit(float x, float y, float z) {
        Vec3Hit(BlockHitResult hit) {
            this(
                    (float) (hit.getLocation().x - hit.getBlockPos().getX()),
                    (float) (hit.getLocation().y - hit.getBlockPos().getY()),
                    (float) (hit.getLocation().z - hit.getBlockPos().getZ()));
        }
    }
}
