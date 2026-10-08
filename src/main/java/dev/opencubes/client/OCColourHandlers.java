package dev.opencubes.client;

import dev.opencubes.OCConstants;
import dev.opencubes.content.paint.PaintCanBlockEntity;
import dev.opencubes.content.paint.PaintMixerBlockEntity;
import dev.opencubes.client.paint.PaintCanItemDecoration;
import dev.opencubes.registry.OCBlocks;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
    public static void registerBlockColours(RegisterColorHandlersEvent.BlockTintSources event) {
        for (DyeColor colour : DyeColor.values()) {
            int dyed = 0xFF000000 | tint(colour);
            event.register(List.of(BlockTintSources.constant(dyed)),
                    OCBlocks.ELEVATORS.get(colour).get(),
                    OCBlocks.ROTATING_ELEVATORS.get(colour).get(),
                    OCBlocks.FLAGS.get(colour).get());
        }
        event.register(List.of(blockEntityTint(PaintCanBlockEntity.class, PaintCanBlockEntity::getColor)),
                OCBlocks.PAINT_CAN.get());
        event.register(List.of(blockEntityTint(PaintMixerBlockEntity.class, PaintMixerBlockEntity::getTargetColor)),
                OCBlocks.PAINT_MIXER.get());
        event.register(List.of(BlockTintSources.constant(0xFFD0A0FF)), OCBlocks.INVERTED_SKY_BLOCK.get());
    }

    @SubscribeEvent
    public static void registerItemTints(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(OCConstants.id("paint"), OCItemTints.PAINT);
        event.register(OCConstants.id("glider"), OCItemTints.GLIDER);
    }

    @SubscribeEvent
    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        event.register(OCBlocks.PAINT_CAN.get(), PaintCanItemDecoration.INSTANCE);
    }

    private static <T> BlockTintSource blockEntityTint(Class<T> type, java.util.function.ToIntFunction<T> colour) {
        return new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return WHITE;
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                if (level.getBlockEntity(pos) instanceof BlockEntity be && type.isInstance(be)) {
                    return 0xFF000000 | colour.applyAsInt(type.cast(be));
                }
                return WHITE;
            }
        };
    }

    private static int tint(DyeColor colour) {
        return colour.getTextureDiffuseColor();
    }
}
