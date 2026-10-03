package dev.opencubes.util;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/**
 * Thin wrappers around NeoForge {@link FakePlayerFactory}. Call per use - the factory owns
 * lifecycle cleanup on world unload.
 */
public final class OCFakePlayers {

    private static final GameProfile PROFILE =
            new GameProfile(UUID.fromString("a0a0a0a0-0c0c-4b0b-8c8c-0c0c0c0c0c0c"), "[OpenCubes]");

    private static final ItemStack[] TOOL_PROBES = {
            new ItemStack(Items.DIAMOND_PICKAXE),
            new ItemStack(Items.DIAMOND_SHOVEL),
            new ItemStack(Items.DIAMOND_AXE),
            new ItemStack(Items.DIAMOND_SWORD),
            new ItemStack(Items.SHEARS),
            new ItemStack(Items.DIAMOND_HOE)
    };

    private OCFakePlayers() {}

    public static FakePlayer get(ServerLevel level) {
        return FakePlayerFactory.get(level, PROFILE);
    }

    /**
     * Removes a block without drops, after asking protection mods via a break event attributed
     * to {@code player}. Returns false if the break was cancelled or the block was already air.
     */
    public static boolean silentRemoveBlock(ServerLevel level, BlockPos pos, net.minecraft.world.entity.player.Player player) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        var result = net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(
                new net.neoforged.neoforge.event.level.BlockEvent.BreakEvent(level, pos, state, player));
        if (result.isCanceled()) {
            return false;
        }
        return level.removeBlock(pos, false);
    }

    /**
     * Harvests {@code pos} with an effective diamond-tier tool and returns the drop stacks.
     * Leftover stacks that are not inserted elsewhere stay the caller's problem - this method
     * does not spawn item entities.
     */
    public static List<ItemStack> breakBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return List.of();
        }

        FakePlayer player = get(level);
        ItemStack tool = findEffectiveTool(level, pos, state, player);
        player.setItemInHand(InteractionHand.MAIN_HAND, tool.copy());

        BlockEntity blockEntity = level.getBlockEntity(pos);
        List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity, player, tool);
        int xp = state.getExpDrop(level, pos, blockEntity, player, tool);
        level.destroyBlock(pos, false, player);
        if (xp > 0) {
            state.getBlock().popExperience(level, pos, xp);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return drops;
    }

    private static ItemStack findEffectiveTool(ServerLevel level, BlockPos pos, BlockState state,
                                               FakePlayer player) {
        for (ItemStack probe : TOOL_PROBES) {
            player.setItemInHand(InteractionHand.MAIN_HAND, probe);
            if (probe.isCorrectToolForDrops(state) || !state.requiresCorrectToolForDrops()) {
                return probe;
            }
        }
        return TOOL_PROBES[0];
    }

    /**
     * Right-clicks {@code stack} against {@code target} as if a player stood at {@code playerPos}
     * looking at the block. Returns the leftover stack (may be empty or smaller).
     */
    public static ItemStack useItemOn(ServerLevel level, ItemStack stack, BlockPos playerPos,
                                      BlockPos target, Direction clickedFace) {
        if (stack.isEmpty()) {
            return stack;
        }

        FakePlayer player = get(level);
        ItemStack held = stack.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, held);

        Vec3 eye = Vec3.atCenterOf(playerPos);
        Vec3 lookAt = Vec3.atCenterOf(target);
        Vec3 delta = lookAt.subtract(eye);
        float yaw = (float) (Math.toDegrees(Math.atan2(-delta.x, delta.z)));
        float pitch = (float) (Math.toDegrees(-Math.asin(delta.normalize().y)));
        player.moveTo(eye.x, eye.y, eye.z, yaw, pitch);

        BlockHitResult hit = new BlockHitResult(lookAt, clickedFace, target, false);
        InteractionResult result = held.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        ItemStack leftover = player.getItemInHand(InteractionHand.MAIN_HAND);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return result.consumesAction() ? leftover : stack;
    }

    /** Spawns a tossed item entity with exact velocity (no vanilla dropper cone). */
    public static void dropItem(ServerLevel level, ItemStack stack, double x, double y, double z,
                                double vx, double vy, double vz) {
        if (stack.isEmpty()) {
            return;
        }
        ItemEntity entity = new ItemEntity(level, x, y, z, stack);
        entity.setDeltaMovement(vx, vy, vz);
        entity.setDefaultPickUpDelay();
        level.addFreshEntity(entity);
    }
}
