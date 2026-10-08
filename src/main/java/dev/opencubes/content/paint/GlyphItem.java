package dev.opencubes.content.paint;

import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.registry.OCEntities;
import dev.opencubes.registry.OCItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item.TooltipContext;
import java.util.function.Consumer;

public class GlyphItem extends Item {

    /**
     * Printable letters, in drawing table order. Item textures are named after the hex code of
     * each character ({@code item/glyph/0041.png} for 'A'); keep under 64 entries for the model
     * overrides.
     */
    public static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!?.,:'-+=&#@()/*%";

    public GlyphItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(char character) {
        ItemStack stack = new ItemStack(OCItems.GLYPH.get());
        stack.set(OCDataComponents.GLYPH_CHAR.get(), String.valueOf(sanitize(character)));
        return stack;
    }

    public static char character(ItemStack stack) {
        String value = stack.get(OCDataComponents.GLYPH_CHAR.get());
        return value == null || value.isEmpty() ? CHARACTERS.charAt(0) : sanitize(value.charAt(0));
    }

    public static char sanitize(char character) {
        char upper = Character.toUpperCase(character);
        return CHARACTERS.indexOf(upper) >= 0 ? upper : CHARACTERS.charAt(0);
    }

    public static int index(char character) {
        return Math.max(0, CHARACTERS.indexOf(sanitize(character)));
    }

    @Override
    public Component getName(ItemStack stack) {
        // TranslatableContents rejects Character; must pass a String (or Component/Number/Boolean).
        return Component.translatable("item.opencubes.glyph.named", String.valueOf(character(stack)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("opencubes.misc.glyph_place_tip").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction face = context.getClickedFace();
        if (!face.getAxis().isHorizontal()) {
            return InteractionResult.FAIL;
        }
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos().relative(face);
        ItemStack stack = context.getItemInHand();
        if (player != null && !player.mayUseItemAt(pos, face, stack)) {
            return InteractionResult.FAIL;
        }
        Level level = context.getLevel();
        int[] offsets = pixelOffsets(context.getClickedPos(), face, context.getClickLocation());
        GlyphEntity glyph = new GlyphEntity(level, pos, face, offsets[0], offsets[1], character(stack));
        if (!glyph.survives()) {
            return InteractionResult.CONSUME;
        }
        if (!level.isClientSide()) {
            glyph.playPlacementSound();
            level.gameEvent(player, GameEvent.ENTITY_PLACE, glyph.position());
            level.addFreshEntity(glyph);
        }
        stack.consume(1, player);
        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
    }

    /**
     * Pixel under the cursor on a wall face, as seen by someone looking at the wall: x grows to
     * the right and y downwards. Clamped so the 8×8 letter centred there stays on that block.
     */
    public static int[] pixelOffsets(BlockPos wall, Direction face, Vec3 hit) {
        Direction right = face.getCounterClockWise();
        double localX = hit.x - wall.getX() - 0.5D;
        double localZ = hit.z - wall.getZ() - 0.5D;
        double u = localX * right.getStepX() + localZ * right.getStepZ() + 0.5D;
        double v = 1.0D - (hit.y - wall.getY());
        int x = Mth.clamp(Mth.floor(u * 16.0D), GlyphEntity.HALF, 16 - GlyphEntity.HALF);
        int y = Mth.clamp(Mth.floor(v * 16.0D), GlyphEntity.HALF, 16 - GlyphEntity.HALF);
        return new int[] {x, y};
    }
}
