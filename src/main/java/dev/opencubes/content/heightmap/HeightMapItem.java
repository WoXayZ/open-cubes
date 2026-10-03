package dev.opencubes.content.heightmap;

import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.registry.OCItems;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class HeightMapItem extends Item {

    public HeightMapItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack create(int mapId) {
        ItemStack stack = new ItemStack(OCItems.HEIGHT_MAP.get());
        stack.set(OCDataComponents.HEIGHT_MAP_ID.get(), mapId);
        return stack;
    }

    public static int getMapId(ItemStack stack) {
        Integer id = stack.get(OCDataComponents.HEIGHT_MAP_ID.get());
        return id == null ? -1 : id;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        int mapId = getMapId(stack);
        if (mapId < 0) {
            return;
        }
        @Nullable Level level = context.level();
        if (level == null) {
            tooltip.add(Component.translatable("opencubes.misc.map_id", mapId));
            return;
        }
        HeightMapData data = HeightMapManager.getMapData(level, mapId);
        if (data.isEmpty()) {
            HeightMapManager.requestMapData(level, mapId);
            tooltip.add(Component.translatable("opencubes.misc.map_id", mapId));
            return;
        }
        if (data.isValid()) {
            tooltip.add(Component.translatable("opencubes.misc.map_center", data.centerX, data.centerZ));
            tooltip.add(Component.translatable("opencubes.misc.map_scale", 1 << data.scale));
        }
    }
}
