package dev.opencubes.content.cursor;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.util.ExperienceUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Sneak-use a block to bind; right-click (air or block) to remotely activate it.
 * Same-dimension, range-limited, and routed through {@link PlayerInteractEvent.RightClickBlock}.
 */
public class CursorItem extends Item {

    public CursorItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();

        if (player.isShiftKeyDown()) {
            stack.set(OCDataComponents.CURSOR_TARGET.get(), new CursorTarget(
                    level.dimension().location(),
                    context.getClickedPos().immutable(),
                    context.getClickedFace()));
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("opencubes.misc.cursor_bound",
                        context.getClickedPos().getX(),
                        context.getClickedPos().getY(),
                        context.getClickedPos().getZ()), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (stack.has(OCDataComponents.CURSOR_TARGET.get())) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                activate(serverPlayer, stack, context.getHand());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        if (!stack.has(OCDataComponents.CURSOR_TARGET.get())) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            activate(serverPlayer, stack, hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static void activate(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        CursorTarget target = stack.get(OCDataComponents.CURSOR_TARGET.get());
        if (target == null) {
            player.displayClientMessage(Component.translatable("opencubes.misc.cursor_unbound"), true);
            return;
        }
        ServerLevel level = player.serverLevel();
        if (!level.dimension().location().equals(target.dimension())) {
            player.displayClientMessage(Component.translatable("opencubes.misc.cursor_wrong_dim"), true);
            return;
        }
        BlockPos pos = target.pos();
        if (!level.isLoaded(pos)) {
            return;
        }
        double distance = Math.sqrt(player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D));
        if (distance > OCCommonConfig.CURSOR_MAX_DISTANCE.get()) {
            player.displayClientMessage(Component.translatable("opencubes.misc.cursor_too_far"), true);
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }

        int cost = player.getAbilities().instabuild ? 0 : Math.max(0, (int) Math.ceil(distance) - 10);
        if (cost > 0 && ExperienceUtil.totalExperience(player) < cost) {
            player.displayClientMessage(Component.translatable("opencubes.misc.cursor_no_xp"), true);
            return;
        }

        Direction side = target.side();
        Vec3 hitVec = Vec3.atCenterOf(pos).relative(side, 0.5D);
        BlockHitResult hit = new BlockHitResult(hitVec, side, pos, false);

        PlayerInteractEvent.RightClickBlock event =
                new PlayerInteractEvent.RightClickBlock(player, hand, pos, hit);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return;
        }
        InteractionResult cancelled = event.getCancellationResult();
        if (cancelled != InteractionResult.PASS && cancelled.consumesAction()) {
            return;
        }

        boolean consumed = false;
        InteractionResult withoutItem = state.useWithoutItem(level, player, hit);
        if (withoutItem.consumesAction()) {
            consumed = true;
        } else {
            ItemInteractionResult withItem = state.useItemOn(ItemStack.EMPTY, level, player, hand, hit);
            consumed = withItem.consumesAction();
        }
        if (consumed && cost > 0) {
            ExperienceUtil.consume(player, cost);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CursorTarget target = stack.get(OCDataComponents.CURSOR_TARGET.get());
        if (target == null) {
            tooltip.add(Component.translatable("opencubes.misc.cursor_unbound"));
        } else {
            tooltip.add(Component.translatable("opencubes.misc.cursor_bound",
                    target.pos().getX(), target.pos().getY(), target.pos().getZ()));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(OCDataComponents.CURSOR_TARGET.get());
    }
}
