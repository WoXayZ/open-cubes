package dev.opencubes.content.imaginary;

import dev.opencubes.util.PlayerFeedback;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;

public class ImaginaryItem extends BlockItem {

    public static float defaultUses() {
        return OCCommonConfig.SPEC.isLoaded()
                ? OCCommonConfig.IMAGINARY_USES.get().floatValue()
                : 10.0F;
    }

    private final boolean crayon;

    public ImaginaryItem(Block block, Properties properties, boolean crayon) {
        super(block, properties.stacksTo(1)
                .component(OCDataComponents.IMAGINARY_USES.get(), defaultUses())
                .component(OCDataComponents.IMAGINARY_MODE.get(), ImaginaryPlacementMode.BLOCK.name()));
        this.crayon = crayon;
    }

    public boolean isCrayon() {
        return crayon;
    }

    public static ItemStack createPencil(ImaginaryShape shape, boolean inverted, float uses) {
        ItemStack stack = new ItemStack(dev.opencubes.registry.OCItems.PENCIL.get());
        setUses(stack, uses);
        setMode(stack, modeFor(shape, inverted));
        return stack;
    }

    public static ItemStack createCrayon(int colour, ImaginaryShape shape, boolean inverted, float uses) {
        ItemStack stack = new ItemStack(dev.opencubes.registry.OCItems.CRAYON.get());
        stack.set(OCDataComponents.PAINT_COLOR.get(), colour & 0xFFFFFF);
        setUses(stack, uses);
        setMode(stack, modeFor(shape, inverted));
        return stack;
    }

    private static ImaginaryPlacementMode modeFor(ImaginaryShape shape, boolean inverted) {
        for (ImaginaryPlacementMode mode : ImaginaryPlacementMode.VALUES) {
            if (mode.shape() == shape && mode.inverted() == inverted) {
                return mode;
            }
        }
        return ImaginaryPlacementMode.BLOCK;
    }

    public static float getUses(ItemStack stack) {
        Float uses = stack.get(OCDataComponents.IMAGINARY_USES.get());
        return uses == null ? 0.0F : uses;
    }

    public static void setUses(ItemStack stack, float uses) {
        stack.set(OCDataComponents.IMAGINARY_USES.get(), uses);
    }

    public static ImaginaryPlacementMode getMode(ItemStack stack) {
        String name = stack.get(OCDataComponents.IMAGINARY_MODE.get());
        if (name == null) {
            return ImaginaryPlacementMode.BLOCK;
        }
        for (ImaginaryPlacementMode mode : ImaginaryPlacementMode.VALUES) {
            if (mode.name().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return ImaginaryPlacementMode.BLOCK;
    }

    public static void setMode(ItemStack stack, ImaginaryPlacementMode mode) {
        stack.set(OCDataComponents.IMAGINARY_MODE.get(), mode.name());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                ImaginaryPlacementMode next = getMode(stack).next();
                setMode(stack, next);
                PlayerFeedback.tell(player, Component.translatable(next.translationKey()), true);
            }
            return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        ItemStack snapshot = stack.copy();
        ImaginaryPlacementMode mode = getMode(snapshot);
        float usesBefore = getUses(snapshot);
        if (usesBefore < mode.cost() && (player == null || !player.getAbilities().instabuild)) {
            return InteractionResult.FAIL;
        }

        InteractionResult result = super.place(context);
        if (!result.consumesAction()) {
            return result;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.getBlockEntity(pos) instanceof ImaginaryBlockEntity be) {
            Integer colour = crayon ? snapshot.get(OCDataComponents.PAINT_COLOR.get()) : null;
            if (crayon && colour == null) {
                colour = 0xFFFFFF;
            }
            be.configure(colour, mode.inverted(), mode.shape());
            BlockState state = level.getBlockState(pos);
            if (state.hasProperty(ImaginaryBlock.SHAPE)) {
                level.setBlock(pos, state.setValue(ImaginaryBlock.SHAPE, mode.shape()), 3);
            }
        }

        // BlockItem consumes one count; restore a multi-use pencil/crayon with remaining uses.
        if (player != null && !player.getAbilities().instabuild) {
            float remaining = usesBefore - mode.cost();
            if (remaining > 0.0F) {
                ItemStack restored = snapshot.copy();
                restored.setCount(1);
                setUses(restored, remaining);
                player.setItemInHand(context.getHand(), restored);
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(getMode(stack).translationKey()));
        tooltip.accept(Component.translatable("opencubes.misc.imaginary_uses", String.format("%.1f", getUses(stack))));
        if (crayon) {
            Integer colour = stack.get(OCDataComponents.PAINT_COLOR.get());
            if (colour != null) {
                tooltip.accept(Component.translatable("opencubes.misc.color", String.format("#%06X", colour)));
            }
        }
    }

    public static void fillCreative(java.util.function.Consumer<ItemStack> output, Item pencil, Item crayon) {
        output.accept(createPencil(ImaginaryShape.BLOCK, false, defaultUses()));
        for (net.minecraft.world.item.DyeColor dye : net.minecraft.world.item.DyeColor.values()) {
            output.accept(createCrayon(dye.getTextureDiffuseColor(), ImaginaryShape.BLOCK, false, defaultUses()));
        }
    }
}
