package dev.opencubes.content.sky;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item.TooltipContext;
import java.util.function.Consumer;

public class SkyBlockItem extends BlockItem {

    private final boolean inverted;

    public SkyBlockItem(Block block, Properties properties, boolean inverted) {
        super(block, properties);
        this.inverted = inverted;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(inverted
                ? "opencubes.tooltip.sky_inverted"
                : "opencubes.tooltip.sky"));
    }
}
