package dev.opencubes.content.paint;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item.TooltipContext;
import java.util.function.Consumer;

public class PaintBrushItem extends Item {

    public PaintBrushItem(Properties properties) {
        // Default matches config; runtime max damage comes from getMaxDamage.
        super(properties.stacksTo(1).durability(24));
    }

    public static int maxUses() {
        return OCCommonConfig.SPEC.isLoaded() ? OCCommonConfig.PAINT_BRUSH_USES.get() : 24;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return maxUses();
    }

    public static Integer getColor(ItemStack stack) {
        return stack.get(OCDataComponents.PAINT_COLOR.get());
    }

    public static ItemStack withColor(Item item, int rgb) {
        ItemStack stack = new ItemStack(item);
        stack.set(OCDataComponents.PAINT_COLOR.get(), rgb & 0xFFFFFF);
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || context.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        Integer color = getColor(stack);
        if (color == null || stack.getDamageValue() >= maxUses()) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        int argb = PaintColors.asArgb(color);

        if (!tryPaint(level, pos, face, argb, player.isShiftKeyDown())) {
            if (OCCommonConfig.PAINT_BRUSH_REPLACE_BLOCKS.get() && CanvasReplace.replace(level, pos)) {
                tryPaint(level, pos, face, argb, player.isShiftKeyDown());
            } else {
                return InteractionResult.FAIL;
            }
        }

        if (!level.isClientSide()) {
            level.playSound(null, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.4F, 1.8F);
            if (!player.getAbilities().instabuild) {
                stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
        }
        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
    }

    private static boolean tryPaint(Level level, BlockPos pos, Direction face, int argb, boolean all) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CanvasBlockEntity canvas) {
            return canvas.applyPaint(face, argb, all);
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        Integer color = getColor(stack);
        if (color != null) {
            tooltip.accept(Component.literal(String.format("#%06X", color)));
        }
    }

    public static void fillCreative(java.util.function.Consumer<ItemStack> output, Item item) {
        output.accept(new ItemStack(item));
        for (DyeColor dye : DyeColor.values()) {
            output.accept(withColor(item, PaintColors.fromDye(dye)));
        }
    }
}
