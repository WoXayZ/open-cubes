package dev.opencubes.content.crane;

import dev.opencubes.config.OCCommonConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item.TooltipContext;
import java.util.function.Consumer;

/**
 * Remote for the crane: hold right-click to lower the magnet, sneak+hold to raise it;
 * left-click / swing to grab or release.
 */
public class CraneControlItem extends Item {

    public CraneControlItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!CraneBackpackItem.isWearing(player)) {
            return InteractionResult.FAIL;
        }
        CraneRegistry.Data data = CraneRegistry.INSTANCE.getData(player, true);
        if (data != null) {
            // shiftControl: hold = lower (extend), sneak+hold = raise.
            // Without it, each use toggles direction. The magnet starts high.
            data.isExtending = OCCommonConfig.CRANE_SHIFT_CONTROL.get()
                    ? !player.isShiftKeyDown()
                    : !data.isExtending;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (entity instanceof ServerPlayer player && CraneBackpackItem.isWearing(player)) {
            CraneRegistry.Data data = CraneRegistry.INSTANCE.getData(player, true);
            if (data != null) {
                if (OCCommonConfig.CRANE_SHIFT_CONTROL.get()) {
                    data.isExtending = !player.isShiftKeyDown();
                }
                data.updateLength();
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.accept(Component.translatable("opencubes.tooltip.crane_control"));
    }
}
