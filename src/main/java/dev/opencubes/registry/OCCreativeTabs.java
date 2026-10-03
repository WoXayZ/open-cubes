package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.content.heightmap.EmptyMapItem;
import dev.opencubes.content.imaginary.ImaginationGlassesItem;
import dev.opencubes.content.imaginary.ImaginaryItem;
import dev.opencubes.content.paint.GlyphItem;
import dev.opencubes.content.paint.PaintBrushItem;
import dev.opencubes.content.paint.PaintCanItem;
import dev.opencubes.content.paint.StencilItem;
import dev.opencubes.content.paint.StencilPattern;
import dev.opencubes.content.trophy.TrophyBlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class OCCreativeTabs {

    private static final DeferredRegister<CreativeModeTab> REGISTRY =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OCConstants.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = REGISTRY.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + OCConstants.MOD_ID + ".main"))
                    .icon(() -> new ItemStack(OCBlocks.ELEVATORS.get(DyeColor.PURPLE).get()))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .displayItems((parameters, output) -> {
                        OCItems.ordered().forEach(item -> {
                            if (item == OCItems.TROPHY || item == OCItems.PAINT_BRUSH
                                    || item == OCItems.STENCIL || item == OCItems.PAINT_CAN
                                    || item == OCItems.EMPTY_MAP || item == OCItems.HEIGHT_MAP
                                    || item == OCItems.PENCIL || item == OCItems.CRAYON
                                    || item == OCItems.CRAYON_GLASSES || item == OCItems.GLYPH) {
                                return;
                            }
                            if (item == OCItems.XP_BUCKET && OCCommonConfig.SPEC.isLoaded()
                                    && !OCCommonConfig.XP_BUCKET_SHOW_IN_CREATIVE.get()) {
                                return;
                            }
                            output.accept(item.get());
                        });
                        for (int scale = 0; scale <= EmptyMapItem.MAX_SCALE; scale++) {
                            output.accept(EmptyMapItem.create(scale));
                        }
                        TrophyBlockItem.fillCreative(parameters.holders(), output::accept);
                        PaintBrushItem.fillCreative(output::accept, OCItems.PAINT_BRUSH.get());
                        for (StencilPattern pattern : StencilPattern.values()) {
                            output.accept(StencilItem.create(pattern));
                        }
                        if (!OCCommonConfig.SPEC.isLoaded() || OCCommonConfig.GLYPHS_SHOW_IN_CREATIVE.get()) {
                            for (char character : GlyphItem.CHARACTERS.toCharArray()) {
                                output.accept(GlyphItem.create(character));
                            }
                        }
                        output.accept(PaintCanItem.create(OCBlocks.PAINT_CAN.get(), 0xFF0000, 30));
                        ImaginaryItem.fillCreative(output::accept, OCItems.PENCIL.get(), OCItems.CRAYON.get());
                        for (DyeColor colour : DyeColor.values()) {
                            output.accept(ImaginationGlassesItem.createCrayonGlasses(
                                    OCItems.CRAYON_GLASSES.get(), colour.getTextureDiffuseColor()));
                        }
                    })
                    .build());

    private OCCreativeTabs() {}

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
