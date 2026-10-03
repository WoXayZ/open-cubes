package dev.opencubes.client;

import dev.opencubes.OCConstants;
import dev.opencubes.content.elevator.Elevator;
import dev.opencubes.content.flight.GliderPaint;
import dev.opencubes.content.imaginary.ImaginationGlassesItem;
import dev.opencubes.content.paint.PaintBrushItem;
import dev.opencubes.content.paint.PaintCanBlockEntity;
import dev.opencubes.content.paint.PaintMixerBlockEntity;
import dev.opencubes.client.paint.PaintCanItemDecoration;
import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.registry.OCItems;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;

/**
 * Elevators, flags and the paint can ship as greyscale textures tinted at render time rather
 * than as one coloured copy of each texture.
 */
@EventBusSubscriber(modid = OCConstants.MOD_ID, value = Dist.CLIENT)
public final class OCColourHandlers {

    private static final int NO_TINT = -1;
    private static final int WHITE = 0xFFFFFF;

    private OCColourHandlers() {}

    @SubscribeEvent
    public static void registerBlockColours(RegisterColorHandlersEvent.Block event) {
        for (DyeColor colour : DyeColor.values()) {
            registerElevatorTint(event, OCBlocks.ELEVATORS.get(colour).get());
            registerElevatorTint(event, OCBlocks.ROTATING_ELEVATORS.get(colour).get());
            event.register((state, level, pos, tintIndex) -> tintIndex == 0 ? tint(colour) : NO_TINT,
                    OCBlocks.FLAGS.get(colour).get());
        }

        // Break particles read tint 0 as well, which is why the can's particle texture is plain white.
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex != 0 || level == null || pos == null) {
                return WHITE;
            }
            return level.getBlockEntity(pos) instanceof PaintCanBlockEntity can ? can.getColor() : WHITE;
        }, OCBlocks.PAINT_CAN.get());

        // Front overlay (tintindex 0) follows the colour currently selected in the mixer GUI.
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex != 0 || level == null || pos == null) {
                return WHITE;
            }
            return level.getBlockEntity(pos) instanceof PaintMixerBlockEntity mixer
                    ? mixer.getTargetColor()
                    : WHITE;
        }, OCBlocks.PAINT_MIXER.get());

        // Inactive inverted sky is the same metal shell, shifted so the two blocks read apart in the world.
        event.register((state, level, pos, tintIndex) -> tintIndex == 0 ? 0xFFD0A0FF : NO_TINT,
                OCBlocks.INVERTED_SKY_BLOCK.get());
    }

    @SubscribeEvent
    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        event.register(OCBlocks.PAINT_CAN.get(), PaintCanItemDecoration.INSTANCE);
    }

    @SubscribeEvent
    public static void registerItemColours(RegisterColorHandlersEvent.Item event) {
        for (DyeColor colour : DyeColor.values()) {
            event.register((stack, tintIndex) -> tintIndex == 0 ? tint(colour) : NO_TINT,
                    OCBlocks.ELEVATORS.get(colour).get(),
                    OCBlocks.ROTATING_ELEVATORS.get(colour).get(),
                    OCBlocks.FLAGS.get(colour).get());
            // Greyscale sleeping_bag.png x dye diffuse colour (blue is the reference shade).
            event.register((stack, tintIndex) -> tintIndex == 0 ? 0xFF000000 | tint(colour) : NO_TINT,
                    OCItems.SLEEPING_BAGS.get(colour).get());
        }
        event.register((stack, tintIndex) -> {
            if (tintIndex != 0) {
                return NO_TINT;
            }
            Integer color = stack.get(OCDataComponents.PAINT_COLOR.get());
            return color == null ? WHITE : 0xFF000000 | color;
        }, OCBlocks.PAINT_CAN.get());
        event.register((stack, tintIndex) -> tintIndex == 0 ? WHITE : NO_TINT, OCBlocks.PAINT_MIXER.get());
        event.register((stack, tintIndex) -> {
            if (tintIndex == 1) {
                Integer color = PaintBrushItem.getColor(stack);
                // 0xFFFFFF is alpha 0 and hides the tip. Keep it opaque.
                return color == null ? 0xFFFFFFFF : 0xFF000000 | color;
            }
            return NO_TINT;
        }, OCItems.PAINT_BRUSH.get());
        event.register((stack, tintIndex) -> {
            if (tintIndex == 1) {
                Integer color = stack.get(OCDataComponents.PAINT_COLOR.get());
                return color == null ? NO_TINT : 0xFF000000 | color;
            }
            return NO_TINT;
        }, OCItems.CRAYON.get());
        event.register((stack, tintIndex) -> {
            Integer color = ImaginationGlassesItem.getCrayonColour(stack);
            return color == null ? NO_TINT : 0xFF000000 | color;
        }, OCItems.CRAYON_GLASSES.get());
        event.register((stack, tintIndex) -> tintIndex == 0 ? 0xFFD0A0FF : NO_TINT,
                OCBlocks.INVERTED_SKY_BLOCK.get());
        // Greyscale canvas on layer0; the glider's wooden frame is an untinted layer1.
        event.register((stack, tintIndex) -> tintIndex == 0 ? 0xFF000000 | GliderPaint.colour(stack) : NO_TINT,
                OCItems.HANG_GLIDER.get(), OCItems.THERMAL_ELYTRA.get());
    }

    private static void registerElevatorTint(RegisterColorHandlersEvent.Block event, Block block) {
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex != 0 || !(block instanceof Elevator elevator)) {
                return NO_TINT;
            }
            return tint(elevator.elevatorColour(state));
        }, block);
    }

    private static int tint(DyeColor colour) {
        return colour.getTextureDiffuseColor();
    }
}
