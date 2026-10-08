package dev.opencubes.content.heightmap;

import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.registry.OCItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item.TooltipContext;
import java.util.function.Consumer;

public class EmptyMapItem extends Item {

    public static final int MAX_SCALE = 4;

    public EmptyMapItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(int scale) {
        ItemStack stack = new ItemStack(OCItems.EMPTY_MAP.get());
        stack.set(OCDataComponents.MAP_SCALE.get(), Math.max(0, Math.min(MAX_SCALE, scale)));
        return stack;
    }

    public static int getScale(ItemStack stack) {
        Integer scale = stack.get(OCDataComponents.MAP_SCALE.get());
        return scale == null ? 0 : Math.max(0, Math.min(MAX_SCALE, scale));
    }

    public static ItemStack upgradeToMap(ServerLevel level, ItemStack emptyMap) {
        byte scale = (byte) getScale(emptyMap);
        int mapId = HeightMapManager.createNewMap(level, scale);
        return HeightMapItem.create(mapId);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.accept(Component.translatable("opencubes.misc.map_scale", 1 << getScale(stack)));
    }
}
