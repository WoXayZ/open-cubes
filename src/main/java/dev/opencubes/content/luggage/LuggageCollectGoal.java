package dev.opencubes.content.luggage;

import dev.opencubes.config.OCCommonConfig;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class LuggageCollectGoal extends Goal {

    private final LuggageEntity luggage;
    private ItemEntity target;

    public LuggageCollectGoal(LuggageEntity luggage) {
        this.luggage = luggage;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!OCCommonConfig.LUGGAGE_COLLECT_ITEMS.get() || luggage.level().isClientSide) {
            return false;
        }
        double range = OCCommonConfig.LUGGAGE_COLLECT_RANGE.get();
        List<ItemEntity> items = luggage.level().getEntitiesOfClass(ItemEntity.class,
                luggage.getBoundingBox().inflate(range),
                e -> e.isAlive() && !e.hasPickUpDelay() && !e.getItem().isEmpty());
        if (items.isEmpty()) {
            return false;
        }
        target = items.get(luggage.getRandom().nextInt(items.size()));
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!OCCommonConfig.LUGGAGE_COLLECT_ITEMS.get()) {
            return false;
        }
        double keep = OCCommonConfig.LUGGAGE_COLLECT_RANGE.get() * 2.0D;
        return target != null && target.isAlive() && luggage.distanceToSqr(target) < keep * keep;
    }

    @Override
    public void stop() {
        target = null;
    }

    @Override
    public void tick() {
        if (target == null) {
            return;
        }
        luggage.getNavigation().moveTo(target, 1.0D);
        if (luggage.distanceToSqr(target) < 1.5D) {
            ItemStack stack = target.getItem();
            ItemStack remainder = ItemHandlerHelper.insertItem(luggage.getInventory(), stack.copy(), false);
            if (remainder.getCount() < stack.getCount()) {
                luggage.level().playSound(null, luggage.blockPosition(), SoundEvents.GENERIC_EAT,
                        SoundSource.NEUTRAL, 0.5F, 1.0F);
            }
            if (remainder.isEmpty()) {
                target.discard();
            } else {
                target.setItem(remainder);
            }
            target = null;
        }
    }
}
