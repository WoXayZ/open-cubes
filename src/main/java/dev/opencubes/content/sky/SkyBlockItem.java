package dev.opencubes.content.sky;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class SkyBlockItem extends BlockItem {

    private final boolean inverted;

    public SkyBlockItem(Block block, Properties properties, boolean inverted) {
        super(block, properties);
        this.inverted = inverted;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(inverted
                ? "opencubes.tooltip.sky_inverted"
                : "opencubes.tooltip.sky"));
    }
}
