package dev.opencubes.content.tomfoolery;

import dev.opencubes.registry.OCAttachments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TastyClayItem extends Item {

    public static final FoodProperties FOOD = new FoodProperties.Builder()
            .nutrition(1)
            .saturationModifier(0.1F)
            .alwaysEdible()
            .build();

    public TastyClayItem(Properties properties) {
        super(properties.food(FOOD));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            int count = player.getData(OCAttachments.BOWEL.get());
            player.setData(OCAttachments.BOWEL.get(), count + 1);
        }
        return result;
    }
}
