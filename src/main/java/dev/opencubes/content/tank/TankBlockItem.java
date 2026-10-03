package dev.opencubes.content.tank;

import dev.opencubes.registry.OCDataComponents;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

/**
 * Shows fluid type and millibuckets when the stack carries {@link OCDataComponents#TANK_FLUID}
 * (creative pick-block with NBT, or a break that preserved components).
 */
public class TankBlockItem extends BlockItem {

    public TankBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        SimpleFluidContent content = stack.get(OCDataComponents.TANK_FLUID.get());
        if (content == null || content.isEmpty()) {
            return;
        }
        FluidStack fluid = content.copy();
        tooltip.add(Component.translatable("opencubes.misc.tank_contents",
                fluid.getHoverName(), fluid.getAmount()));
    }
}
