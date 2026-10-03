package dev.opencubes.content.sponge;

import dev.opencubes.config.OCCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class SpongeOnAStickItem extends Item {

    public SpongeOnAStickItem(Properties properties) {
        // Default matches config; runtime max damage comes from getMaxDamage.
        super(properties.stacksTo(1).durability(256));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return OCCommonConfig.SPEC.isLoaded()
                ? OCCommonConfig.SPONGE_STICK_DURABILITY.get()
                : 256;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        return soak(context.getLevel(), context.getClickedPos(), player, context.getItemInHand(), context.getHand())
                ? InteractionResult.sidedSuccess(context.getLevel().isClientSide)
                : InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean ok = soak(level, player.blockPosition(), player, stack, hand);
        return ok
                ? InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
                : InteractionResultHolder.fail(stack);
    }

    private static boolean soak(Level level, BlockPos pos, Player player, ItemStack stack, InteractionHand hand) {
        FluidSoak.Result result = FluidSoak.soak(level, pos, FluidSoak.stickRange(), FluidSoak.stickUpdates());
        if (result.hitLava()) {
            stack.shrink(1);
            player.setRemainingFireTicks(6 * 20);
            return true;
        }
        if (result.absorbedAnything()) {
            stack.hurtAndBreak(1, player,
                    hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            return true;
        }
        return false;
    }
}
