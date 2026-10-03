package dev.opencubes.content.paint;

import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.registry.OCItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class StencilItem extends Item {

    public StencilItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(StencilPattern pattern) {
        ItemStack stack = new ItemStack(OCItems.STENCIL.get());
        stack.set(OCDataComponents.STENCIL_PATTERN.get(), pattern.id());
        return stack;
    }

    public static StencilPattern pattern(ItemStack stack) {
        String id = stack.get(OCDataComponents.STENCIL_PATTERN.get());
        return id == null ? StencilPattern.CREEPER_FACE : StencilPattern.byId(id);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockEntity be = level.getBlockEntity(pos);

        CanvasBlockEntity canvas;
        if (be instanceof CanvasBlockEntity existing) {
            canvas = existing;
        } else {
            if (!CanvasReplace.replace(level, pos)) {
                return InteractionResult.PASS;
            }
            if (!(level.getBlockEntity(pos) instanceof CanvasBlockEntity replaced)) {
                return InteractionResult.FAIL;
            }
            canvas = replaced;
        }

        ItemStack stack = context.getItemInHand();
        StencilPattern pattern = pattern(stack);

        if (player != null && player.isShiftKeyDown()) {
            var popped = canvas.rotateOrPopStencil(context.getClickedFace(), true);
            if (popped.isPresent() && !level.isClientSide && !player.getAbilities().instabuild) {
                player.getInventory().add(create(popped.get()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (canvas.face(context.getClickedFace()).cover() != null) {
            canvas.rotateOrPopStencil(context.getClickedFace(), false);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        Direction side = context.getClickedFace();
        if (canvas.placeStencil(side, pattern, placementRotation(side, context.getHorizontalDirection()))) {
            if (!level.isClientSide && player != null && !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.FAIL;
    }

    /**
     * Quarter turns that make the pattern read upright for a player looking the given way, like
     * glazed terracotta. Walls are already upright; the floor and ceiling turn with the player.
     */
    public static int placementRotation(Direction side, Direction looking) {
        return switch (side) {
            case UP -> (looking.get2DDataValue() + 2) & 3;
            case DOWN -> (2 - looking.get2DDataValue()) & 3;
            default -> 0;
        };
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId() + "." + pattern(stack).id());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("opencubes.misc.stencil_place_tip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("opencubes.misc.stencil_brush_tip").withStyle(ChatFormatting.GRAY));
    }
}
