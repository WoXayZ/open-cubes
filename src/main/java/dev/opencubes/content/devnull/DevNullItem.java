package dev.opencubes.content.devnull;

import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.registry.OCItems;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item.TooltipContext;
import java.util.function.Consumer;

public class DevNullItem extends Item {

    public static final int STACK_LIMIT = 5;

    public DevNullItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack getContained(ItemStack container) {
        ItemContainerContents contents = container.get(OCDataComponents.DEV_NULL_CONTENTS.get());
        if (contents == null || contents.getSlots() <= 0) {
            return ItemStack.EMPTY;
        }
        return contents.getStackInSlot(0);
    }

    public static void setContained(ItemStack container, ItemStack contents) {
        if (contents.isEmpty()) {
            container.remove(OCDataComponents.DEV_NULL_CONTENTS.get());
        } else {
            container.set(
                    OCDataComponents.DEV_NULL_CONTENTS.get(),
                    ItemContainerContents.fromItems(List.of(contents.copy())));
        }
    }

    /** Walk nested /dev/nulls and return (innermost non-dev-null stack, depth). */
    public static NestResult nest(ItemStack container) {
        ItemStack stack = container;
        int depth = 0;
        while (depth < STACK_LIMIT) {
            if (stack.isEmpty() || !(stack.getItem() instanceof DevNullItem)) {
                return new NestResult(stack, depth);
            }
            stack = getContained(stack);
            depth++;
        }
        return new NestResult(ItemStack.EMPTY, depth);
    }

    public record NestResult(ItemStack innermost, int depth) {}

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (dev.opencubes.config.OCCommonConfig.DEV_NULL_SNEAK_TO_OPEN.get() && !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            int slot = player.getInventory().getSelectedSlot();
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new DevNullMenu(id, inv, slot),
                    Component.translatable("container.opencubes.dev_null")),
                    buf -> buf.writeVarInt(slot));
        }
        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        ItemStack container = context.getItemInHand();
        ItemStack contained = getContained(container);
        if (contained.isEmpty() || !(contained.getItem() instanceof BlockItem)) {
            return InteractionResult.PASS;
        }

        // Temporarily swap the filter item into the hand so BlockItem placement works, then write back.
        ItemStack handBackup = container.copy();
        player.setItemInHand(context.getHand(), contained.copy());
        InteractionResult result = contained.useOn(context);
        ItemStack after = player.getItemInHand(context.getHand());
        setContained(handBackup, after);
        player.setItemInHand(context.getHand(), handBackup);
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        NestResult nest = nest(stack);
        if (nest.depth() >= STACK_LIMIT) {
            tooltip.accept(Component.literal("§kWHOOPS"));
            return;
        }
        ItemStack inner = nest.innermost();
        if (!inner.isEmpty()) {
            tooltip.accept(Component.literal("┌ ").append(inner.getHoverName().copy()
                    .append(Component.literal(" ×" + inner.getCount()))));
        }
    }

    public static boolean isDevNull(ItemStack stack) {
        return stack.is(OCItems.DEV_NULL.get());
    }
}
