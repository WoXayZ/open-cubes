package dev.opencubes.content.sponge;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SpongeBlock extends Block {

    public static final MapCodec<SpongeBlock> CODEC = simpleCodec(SpongeBlock::new);
    private static final int TICK_RATE = 20 * 5;
    private static final int EVENT_BURN = 123;

    public SpongeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, net.minecraft.world.level.redstone.Orientation fromPos, boolean isMoving) {
        clear(level, pos);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        clear(level, pos);
        level.scheduleTick(pos, this, TICK_RATE + level.getRandom().nextInt(5));
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        clear(level, pos);
        level.scheduleTick(pos, this, TICK_RATE + random.nextInt(5));
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (!FluidSoak.blockUpdates()) {
            FluidSoak.wakeBorderLiquids(level, pos, FluidSoak.blockRange());
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    private void clear(Level level, BlockPos pos) {
        FluidSoak.Result result = FluidSoak.soak(level, pos, FluidSoak.blockRange(), FluidSoak.blockUpdates());
        if (result.hitLava()) {
            level.blockEvent(pos, this, EVENT_BURN, 0);
        }
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id == EVENT_BURN) {
            if (level.isClientSide()) {
                for (int i = 0; i < 20; i++) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE,
                            pos.getX() + level.getRandom().nextDouble() * 0.1D,
                            pos.getY() + 1.0D + level.getRandom().nextDouble(),
                            pos.getZ() + level.getRandom().nextDouble(),
                            0.0D, 0.0D, 0.0D);
                }
            } else {
                level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
            }
            return true;
        }
        return super.triggerEvent(state, level, pos, id, param);
    }
}
