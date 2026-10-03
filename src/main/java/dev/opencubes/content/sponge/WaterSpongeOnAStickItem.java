package dev.opencubes.content.sponge;

import dev.opencubes.registry.OCItems;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Sponge on a Stick that keeps the vanilla sponge rules: water only, a flood fill capped at
 * {@value #MAX_BLOCKS} blocks, and it turns into a wet variant that has to be dried out again.
 * The OpenBlocks flavour lives in {@link SpongeOnAStickItem}, which soaks any fluid instead.
 */
public class WaterSpongeOnAStickItem extends Item {

    private static final int MAX_BLOCKS = 64;
    private static final int MAX_DEPTH = 6;

    private final boolean wet;

    public WaterSpongeOnAStickItem(Properties properties, boolean wet) {
        super(properties.stacksTo(1));
        this.wet = wet;
    }

    public boolean isWet() {
        return wet;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockPos target = context.getClickedPos().relative(context.getClickedFace());
        return absorb(context.getLevel(), target, player, context.getHand())
                ? InteractionResult.sidedSuccess(context.getLevel().isClientSide)
                : InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return absorb(level, player.blockPosition(), player, hand)
                ? InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
                : InteractionResultHolder.pass(stack);
    }

    private boolean absorb(Level level, BlockPos origin, Player player, InteractionHand hand) {
        if (wet) {
            return false;
        }
        if (level.isClientSide) {
            return hasWaterNearby(level, origin);
        }
        if (removeWaterBreadthFirst(level, origin) == 0) {
            return false;
        }
        level.playSound(null, origin, SoundEvents.SPONGE_ABSORB, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (!player.getAbilities().instabuild) {
            player.setItemInHand(hand, new ItemStack(OCItems.WET_SPONGE_ON_A_STICK.get()));
        }
        return true;
    }

    private static boolean hasWaterNearby(Level level, BlockPos origin) {
        for (Direction direction : Direction.values()) {
            if (level.getFluidState(origin.relative(direction)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return level.getFluidState(origin).is(FluidTags.WATER);
    }

    /** Same breadth-first water removal vanilla sponges use, seeded on the clicked position. */
    private static int removeWaterBreadthFirst(Level level, BlockPos origin) {
        Deque<Entry> queue = new ArrayDeque<>();
        queue.add(new Entry(origin, 0));
        int removed = 0;

        while (!queue.isEmpty() && removed < MAX_BLOCKS) {
            Entry entry = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos pos = entry.pos().relative(direction);
                if (!level.isLoaded(pos) || !level.getFluidState(pos).is(FluidTags.WATER)) {
                    continue;
                }
                if (!drain(level, pos)) {
                    continue;
                }
                removed++;
                if (entry.depth() < MAX_DEPTH) {
                    queue.add(new Entry(pos, entry.depth() + 1));
                }
                if (removed >= MAX_BLOCKS) {
                    break;
                }
            }
        }
        return removed;
    }

    private static boolean drain(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof LiquidBlock) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return true;
        }
        if (state.getBlock() instanceof BucketPickup pickup
                && !pickup.pickupBlock(null, level, pos, state).isEmpty()) {
            return true;
        }
        if (state.is(Blocks.KELP) || state.is(Blocks.KELP_PLANT)
                || state.is(Blocks.SEAGRASS) || state.is(Blocks.TALL_SEAGRASS)) {
            Block.dropResources(state, level, pos, level.getBlockEntity(pos));
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return true;
        }
        return false;
    }

    private record Entry(BlockPos pos, int depth) {}
}
