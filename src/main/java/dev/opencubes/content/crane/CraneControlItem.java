package dev.opencubes.content.crane;

import dev.opencubes.config.OCCommonConfig;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * Remote for the crane: hold right-click to lower the magnet, sneak+hold to raise it;
 * left-click / swing to grab or release.
 */
public class CraneControlItem extends Item {

    public CraneControlItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!CraneBackpackItem.isWearing(player)) {
            return InteractionResultHolder.fail(stack);
        }
        CraneRegistry.Data data = CraneRegistry.INSTANCE.getData(player, true);
        if (data != null) {
            // shiftControl: hold = lower (extend), sneak+hold = raise. Magnet starts high —
            // lowering first is what players try. Otherwise each use toggles direction.
            data.isExtending = OCCommonConfig.CRANE_SHIFT_CONTROL.get()
                    ? !player.isShiftKeyDown()
                    : !data.isExtending;
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
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
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable("opencubes.tooltip.crane_control"));
    }
}
