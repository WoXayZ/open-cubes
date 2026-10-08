package dev.opencubes.content.paint;

import dev.opencubes.registry.OCDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item.TooltipContext;
import java.util.function.Consumer;

public class PaintCanItem extends BlockItem {

    public PaintCanItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static ItemStack create(Block block, int rgb, int amount) {
        ItemStack stack = new ItemStack(block);
        stack.set(OCDataComponents.PAINT_COLOR.get(), rgb & 0xFFFFFF);
        stack.set(OCDataComponents.PAINT_AMOUNT.get(), amount);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        Integer color = stack.get(OCDataComponents.PAINT_COLOR.get());
        Integer amount = stack.get(OCDataComponents.PAINT_AMOUNT.get());
        if (color != null) {
            tooltip.accept(Component.literal(String.format("#%06X", color)));
        }
        if (amount != null) {
            tooltip.accept(Component.translatable("opencubes.misc.paint_amount", amount));
        }
    }
}
