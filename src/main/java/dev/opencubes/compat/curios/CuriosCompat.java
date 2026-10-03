package dev.opencubes.compat.curios;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/** Isolated Curios API calls so the class only loads when Curios is present. */
public final class CuriosCompat {

    private CuriosCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded("curios");
    }

    public static boolean isWearing(LivingEntity entity, Predicate<ItemStack> predicate) {
        return CuriosApi.getCuriosInventory(entity)
                .map(handler -> handler.isEquipped(predicate))
                .orElse(false);
    }

    public static void collectEquipped(LivingEntity entity, List<ItemStack> out) {
        CuriosApi.getCuriosInventory(entity).ifPresent(handler -> collectFromHandler(handler, out));
    }

    private static void collectFromHandler(ICuriosItemHandler handler, List<ItemStack> out) {
        handler.getCurios().forEach((identifier, stackHandler) -> collectFromSlotHandler(stackHandler, out));
    }

    private static void collectFromSlotHandler(ICurioStacksHandler stackHandler, List<ItemStack> out) {
        collectFromDynamicHandler(stackHandler.getStacks(), out);
        if (stackHandler.hasCosmetic()) {
            collectFromDynamicHandler(stackHandler.getCosmeticStacks(), out);
        }
    }

    private static void collectFromDynamicHandler(IDynamicStackHandler stackHandler, List<ItemStack> out) {
        for (int slot = 0; slot < stackHandler.getSlots(); slot++) {
            ItemStack stack = stackHandler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                out.add(stack.copy());
                stackHandler.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }
}
