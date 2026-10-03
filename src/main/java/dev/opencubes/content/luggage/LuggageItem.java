package dev.opencubes.content.luggage;

import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.registry.OCEntities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import java.util.ArrayList;
import java.util.List;

public class LuggageItem extends Item {

    public LuggageItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public record InventoryData(ItemStackHandler handler, boolean special) {}

    public static void saveInventory(ItemStack stack, ItemStackHandler handler, boolean special) {
        NonNullListAdapter contents = new NonNullListAdapter(handler);
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents.asList()));
        stack.set(OCDataComponents.LUGGAGE_SPECIAL.get(), special);
    }

    public static InventoryData loadInventory(ItemStack stack) {
        boolean special = Boolean.TRUE.equals(stack.get(OCDataComponents.LUGGAGE_SPECIAL.get()));
        int size = special ? LuggageEntity.SIZE_SPECIAL : LuggageEntity.SIZE_NORMAL;
        ItemStackHandler handler = new ItemStackHandler(size);
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents != null) {
            java.util.concurrent.atomic.AtomicInteger i = new java.util.concurrent.atomic.AtomicInteger();
            contents.stream().forEach(item -> {
                int slot = i.getAndIncrement();
                if (slot < handler.getSlots()) {
                    handler.setStackInSlot(slot, item.copy());
                }
            });
        }
        return new InventoryData(handler, special);
    }

    /** Tiny helper so we can feed ItemContainerContents.fromItems a List. */
    private static final class NonNullListAdapter {
        private final ItemStackHandler handler;

        NonNullListAdapter(ItemStackHandler handler) {
            this.handler = handler;
        }

        java.util.List<ItemStack> asList() {
            java.util.ArrayList<ItemStack> list = new java.util.ArrayList<>(handler.getSlots());
            for (int i = 0; i < handler.getSlots(); i++) {
                list.add(handler.getStackInSlot(i));
            }
            return list;
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return Boolean.TRUE.equals(stack.get(OCDataComponents.LUGGAGE_SPECIAL.get())) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        InventoryData data = loadInventory(stack);
        List<ItemStack> filled = new ArrayList<>();
        for (int i = 0; i < data.handler().getSlots(); i++) {
            ItemStack slot = data.handler().getStackInSlot(i);
            if (!slot.isEmpty()) {
                filled.add(slot);
            }
        }
        if (filled.isEmpty()) {
            return;
        }
        int shown = Math.min(5, filled.size());
        for (int i = 0; i < shown; i++) {
            ItemStack slot = filled.get(i);
            tooltip.add(Component.translatable("opencubes.luggage.tooltip.entry",
                    slot.getCount(), slot.getHoverName()));
        }
        int remaining = filled.size() - shown;
        if (remaining > 0) {
            tooltip.add(Component.translatable("opencubes.luggage.tooltip.more", remaining));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            Vec3 look = player.getLookAngle();
            Vec3 spawn = player.position().add(look.x * 2.0D, 0.0D, look.z * 2.0D);
            LuggageEntity luggage = OCEntities.LUGGAGE.get().create(level);
            if (luggage != null) {
                luggage.moveTo(spawn.x, player.getY(), spawn.z, player.getYRot(), 0.0F);
                luggage.tame(player);
                luggage.setOwnerUUID(player.getUUID());
                luggage.restoreFromStack(stack);
                level.addFreshEntity(luggage);
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
