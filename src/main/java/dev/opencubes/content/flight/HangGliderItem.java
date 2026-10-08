package dev.opencubes.content.flight;

import dev.opencubes.util.PlayerFeedback;

import dev.opencubes.registry.OCAttachments;
import dev.opencubes.registry.OCDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item.TooltipContext;
import java.util.function.Consumer;

public class HangGliderItem extends Item {

    public HangGliderItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean engaged = !Boolean.TRUE.equals(stack.get(OCDataComponents.GLIDER_ENGAGED.get()));
        stack.set(OCDataComponents.GLIDER_ENGAGED.get(), engaged);

        GliderState state = player.getData(OCAttachments.GLIDER.get());
        state.setEngaged(engaged);
        state.setHand(hand);

        if (!level.isClientSide()) {
            PlayerFeedback.tell(player, Component.translatable(
                    engaged ? "opencubes.misc.glider_engaged" : "opencubes.misc.glider_stowed"), true);
        }
        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        GliderPaint.appendTooltip(stack, tooltip);
    }

    public static boolean isHoldingEngaged(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() instanceof HangGliderItem
                    && Boolean.TRUE.equals(held.get(OCDataComponents.GLIDER_ENGAGED.get()))) {
                return true;
            }
        }
        return false;
    }

    public static InteractionHand engagedHand(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() instanceof HangGliderItem
                    && Boolean.TRUE.equals(held.get(OCDataComponents.GLIDER_ENGAGED.get()))) {
                return hand;
            }
        }
        return InteractionHand.MAIN_HAND;
    }

    public static ItemStack engagedStack(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() instanceof HangGliderItem
                    && Boolean.TRUE.equals(held.get(OCDataComponents.GLIDER_ENGAGED.get()))) {
                return held;
            }
        }
        return ItemStack.EMPTY;
    }
}
