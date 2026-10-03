package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import dev.opencubes.content.devnull.DevNullItem;
import dev.opencubes.content.flight.HangGliderItem;
import dev.opencubes.content.flight.ThermalElytraItem;
import dev.opencubes.content.goldeneye.GoldenEyeItem;
import dev.opencubes.content.crane.CraneBackpackItem;
import dev.opencubes.content.crane.CraneControlItem;
import dev.opencubes.content.heightmap.CartographerItem;
import dev.opencubes.content.heightmap.EmptyMapItem;
import dev.opencubes.content.heightmap.HeightMapItem;
import dev.opencubes.content.imaginary.ImaginationGlassesItem;
import dev.opencubes.content.imaginary.ImaginationGlassesKind;
import dev.opencubes.content.imaginary.ImaginaryItem;
import dev.opencubes.content.item.XpBucketItem;
import dev.opencubes.content.luggage.LuggageItem;
import dev.opencubes.content.paint.GlyphItem;
import dev.opencubes.content.paint.PaintBrushItem;
import dev.opencubes.content.paint.PaintCanItem;
import dev.opencubes.content.paint.SqueegeeItem;
import dev.opencubes.content.paint.StencilItem;
import dev.opencubes.content.ladder.RopeLadderBlockItem;
import dev.opencubes.content.sky.SkyBlockItem;
import dev.opencubes.content.scaffolding.TemporaryScaffoldingBlockItem;
import dev.opencubes.content.sleeping.SleepingBagItem;
import dev.opencubes.content.tank.TankBlockItem;
import dev.opencubes.content.tools.PedometerItem;
import dev.opencubes.content.tools.SlimalyzerItem;
import dev.opencubes.content.tools.WrenchItem;
import dev.opencubes.content.trophy.TrophyBlockItem;
import dev.opencubes.content.vision.SonicGlassesItem;
import dev.opencubes.content.sponge.SpongeOnAStickItem;
import dev.opencubes.content.sponge.WaterSpongeOnAStickItem;
import dev.opencubes.content.cannon.PointerItem;
import dev.opencubes.content.book.InfoBookItem;
import dev.opencubes.content.cursor.CursorItem;
import dev.opencubes.content.tomfoolery.EpicEraserItem;
import dev.opencubes.content.tomfoolery.TastyClayItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class OCItems {

    private static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(OCConstants.MOD_ID);
    private static final List<DeferredItem<? extends Item>> ORDERED = new ArrayList<>();

    public static final Map<DyeColor, DeferredItem<SleepingBagItem>> SLEEPING_BAGS = new EnumMap<>(DyeColor.class);

    public static final DeferredItem<TemporaryScaffoldingBlockItem> TEMPORARY_SCAFFOLDING;
    public static final DeferredItem<RopeLadderBlockItem> ROPE_LADDER;
    public static final DeferredItem<TrophyBlockItem> TROPHY;
    public static final DeferredItem<TankBlockItem> TANK;
    public static final DeferredItem<PaintCanItem> PAINT_CAN;
    public static final DeferredItem<XpBucketItem> XP_BUCKET;
    public static final DeferredItem<WrenchItem> WRENCH;
    public static final DeferredItem<SlimalyzerItem> SLIMALYZER;
    public static final DeferredItem<PedometerItem> PEDOMETER;
    public static final DeferredItem<DevNullItem> DEV_NULL;
    public static final DeferredItem<GoldenEyeItem> GOLDEN_EYE;
    public static final DeferredItem<LuggageItem> LUGGAGE;
    public static final DeferredItem<PaintBrushItem> PAINT_BRUSH;
    public static final DeferredItem<SqueegeeItem> SQUEEGEE;
    public static final DeferredItem<StencilItem> STENCIL;
    public static final DeferredItem<Item> UNPREPARED_STENCIL;
    public static final DeferredItem<Item> SKETCHING_PENCIL;
    public static final DeferredItem<GlyphItem> GLYPH;
    public static final DeferredItem<EmptyMapItem> EMPTY_MAP;
    public static final DeferredItem<HeightMapItem> HEIGHT_MAP;
    public static final DeferredItem<CartographerItem> CARTOGRAPHER;
    public static final DeferredItem<Item> BEAM;
    public static final DeferredItem<Item> LINE;
    public static final DeferredItem<Item> CRANE_ENGINE;
    public static final DeferredItem<Item> CRANE_MAGNET;
    public static final DeferredItem<CraneBackpackItem> CRANE_BACKPACK;
    public static final DeferredItem<CraneControlItem> CRANE_CONTROL;
    public static final DeferredItem<Item> GLIDER_WING_LEFT;
    public static final DeferredItem<Item> GLIDER_WING_RIGHT;
    public static final DeferredItem<HangGliderItem> HANG_GLIDER;
    public static final DeferredItem<ThermalElytraItem> THERMAL_ELYTRA;
    public static final DeferredItem<SonicGlassesItem> SONIC_GLASSES;
    public static final DeferredItem<ImaginaryItem> PENCIL;
    public static final DeferredItem<ImaginaryItem> CRAYON;
    public static final DeferredItem<ImaginationGlassesItem> PENCIL_GLASSES;
    public static final DeferredItem<ImaginationGlassesItem> CRAYON_GLASSES;
    public static final DeferredItem<ImaginationGlassesItem> TECHNICOLOR_GLASSES;
    public static final DeferredItem<ImaginationGlassesItem> ADMIN_GLASSES;
    public static final DeferredItem<SpongeOnAStickItem> LIQUID_SPONGE_ON_A_STICK;
    public static final DeferredItem<WaterSpongeOnAStickItem> SPONGE_ON_A_STICK;
    public static final DeferredItem<WaterSpongeOnAStickItem> WET_SPONGE_ON_A_STICK;
    public static final DeferredItem<PointerItem> POINTER;
    public static final DeferredItem<EpicEraserItem> EPIC_ERASER;
    public static final DeferredItem<TastyClayItem> TASTY_CLAY;
    public static final DeferredItem<InfoBookItem> INFO_BOOK;
    public static final DeferredItem<CursorItem> CURSOR;

    static {
        OCBlocks.ordered().forEach(block -> {
            if (block == OCBlocks.TROPHY || block == OCBlocks.PAINT_CAN || block == OCBlocks.IMAGINARY
                    || block == OCBlocks.TEMPORARY_SCAFFOLDING || block == OCBlocks.TANK
                    || block == OCBlocks.ROPE_LADDER) {
                return;
            }
            if (block == OCBlocks.SKY_BLOCK || block == OCBlocks.INVERTED_SKY_BLOCK) {
                boolean inverted = block == OCBlocks.INVERTED_SKY_BLOCK;
                ORDERED.add(REGISTRY.register(block.getId().getPath(),
                        () -> new SkyBlockItem(block.get(), new Item.Properties(), inverted)));
                return;
            }
            ORDERED.add(REGISTRY.registerSimpleBlockItem(block));
        });

        // Needs vanilla's scaffolding placement walk, so it cannot use the simple block item above.
        TEMPORARY_SCAFFOLDING = REGISTRY.register("temporary_scaffolding",
                () -> new TemporaryScaffoldingBlockItem(OCBlocks.TEMPORARY_SCAFFOLDING.get(), new Item.Properties()));
        ORDERED.add(TEMPORARY_SCAFFOLDING);

        // Top-edge clicks redirect onto the side face so the ladder can place and unroll.
        ROPE_LADDER = REGISTRY.register("rope_ladder",
                () -> new RopeLadderBlockItem(OCBlocks.ROPE_LADDER.get(), new Item.Properties()));
        ORDERED.add(ROPE_LADDER);

        TROPHY = REGISTRY.register("trophy",
                () -> new TrophyBlockItem(OCBlocks.TROPHY.get(), new Item.Properties()));
        ORDERED.add(TROPHY);

        TANK = REGISTRY.register("tank",
                () -> new TankBlockItem(OCBlocks.TANK.get(), new Item.Properties()));
        ORDERED.add(TANK);

        PAINT_CAN = REGISTRY.register("paint_can",
                () -> new PaintCanItem(OCBlocks.PAINT_CAN.get(), new Item.Properties().stacksTo(1)));
        ORDERED.add(PAINT_CAN);

        XP_BUCKET = REGISTRY.register("xp_bucket", () -> new XpBucketItem(new Item.Properties()));
        ORDERED.add(XP_BUCKET);

        WRENCH = REGISTRY.register("wrench", () -> new WrenchItem(new Item.Properties()));
        ORDERED.add(WRENCH);

        SLIMALYZER = REGISTRY.register("slimalyzer", () -> new SlimalyzerItem(new Item.Properties()));
        ORDERED.add(SLIMALYZER);

        PEDOMETER = REGISTRY.register("pedometer", () -> new PedometerItem(new Item.Properties()));
        ORDERED.add(PEDOMETER);

        DEV_NULL = REGISTRY.register("dev_null", () -> new DevNullItem(new Item.Properties()));
        ORDERED.add(DEV_NULL);

        for (DyeColor colour : DyeColor.values()) {
            DeferredItem<SleepingBagItem> bag = REGISTRY.register(colour.getName() + "_sleeping_bag",
                    () -> new SleepingBagItem(new Item.Properties(), colour));
            SLEEPING_BAGS.put(colour, bag);
            ORDERED.add(bag);
        }

        GOLDEN_EYE = REGISTRY.register("golden_eye", () -> new GoldenEyeItem(new Item.Properties()));
        ORDERED.add(GOLDEN_EYE);

        LUGGAGE = REGISTRY.register("luggage", () -> new LuggageItem(new Item.Properties()));
        ORDERED.add(LUGGAGE);

        PAINT_BRUSH = REGISTRY.register("paint_brush", () -> new PaintBrushItem(new Item.Properties()));
        ORDERED.add(PAINT_BRUSH);

        SQUEEGEE = REGISTRY.register("squeegee", () -> new SqueegeeItem(new Item.Properties()));
        ORDERED.add(SQUEEGEE);

        STENCIL = REGISTRY.register("stencil", () -> new StencilItem(new Item.Properties()));
        ORDERED.add(STENCIL);

        UNPREPARED_STENCIL = REGISTRY.register("unprepared_stencil", () -> new Item(new Item.Properties()));
        ORDERED.add(UNPREPARED_STENCIL);

        SKETCHING_PENCIL = REGISTRY.register("sketching_pencil", () -> new Item(new Item.Properties()));
        ORDERED.add(SKETCHING_PENCIL);

        GLYPH = REGISTRY.register("glyph", () -> new GlyphItem(new Item.Properties()));
        ORDERED.add(GLYPH);

        EMPTY_MAP = REGISTRY.register("empty_map", () -> new EmptyMapItem(new Item.Properties()));
        ORDERED.add(EMPTY_MAP);

        HEIGHT_MAP = REGISTRY.register("height_map", () -> new HeightMapItem(new Item.Properties()));
        ORDERED.add(HEIGHT_MAP);

        CARTOGRAPHER = REGISTRY.register("cartographer", () -> new CartographerItem(new Item.Properties()));
        ORDERED.add(CARTOGRAPHER);

        BEAM = REGISTRY.register("beam", () -> new Item(new Item.Properties()));
        ORDERED.add(BEAM);

        LINE = REGISTRY.register("line", () -> new Item(new Item.Properties()));
        ORDERED.add(LINE);

        CRANE_ENGINE = REGISTRY.register("crane_engine", () -> new Item(new Item.Properties()));
        ORDERED.add(CRANE_ENGINE);

        CRANE_MAGNET = REGISTRY.register("crane_magnet", () -> new Item(new Item.Properties()));
        ORDERED.add(CRANE_MAGNET);

        CRANE_BACKPACK = REGISTRY.register("crane_backpack",
                () -> new CraneBackpackItem(new Item.Properties()));
        ORDERED.add(CRANE_BACKPACK);

        CRANE_CONTROL = REGISTRY.register("crane_control",
                () -> new CraneControlItem(new Item.Properties()));
        ORDERED.add(CRANE_CONTROL);

        GLIDER_WING_LEFT = REGISTRY.register("glider_wing_left", () -> new Item(new Item.Properties()));
        ORDERED.add(GLIDER_WING_LEFT);

        GLIDER_WING_RIGHT = REGISTRY.register("glider_wing_right", () -> new Item(new Item.Properties()));
        ORDERED.add(GLIDER_WING_RIGHT);

        HANG_GLIDER = REGISTRY.register("hang_glider", () -> new HangGliderItem(new Item.Properties()));
        ORDERED.add(HANG_GLIDER);

        THERMAL_ELYTRA = REGISTRY.register("thermal_elytra", () -> new ThermalElytraItem(new Item.Properties()));
        ORDERED.add(THERMAL_ELYTRA);

        SONIC_GLASSES = REGISTRY.register("sonic_glasses", () -> new SonicGlassesItem(new Item.Properties()));
        ORDERED.add(SONIC_GLASSES);

        PENCIL = REGISTRY.register("pencil",
                () -> new ImaginaryItem(OCBlocks.IMAGINARY.get(), new Item.Properties(), false));
        ORDERED.add(PENCIL);

        CRAYON = REGISTRY.register("crayon",
                () -> new ImaginaryItem(OCBlocks.IMAGINARY.get(), new Item.Properties(), true));
        ORDERED.add(CRAYON);

        PENCIL_GLASSES = REGISTRY.register("pencil_glasses",
                () -> new ImaginationGlassesItem(new Item.Properties(), ImaginationGlassesKind.PENCIL));
        ORDERED.add(PENCIL_GLASSES);

        CRAYON_GLASSES = REGISTRY.register("crayon_glasses",
                () -> new ImaginationGlassesItem(new Item.Properties(), ImaginationGlassesKind.CRAYON));
        ORDERED.add(CRAYON_GLASSES);

        TECHNICOLOR_GLASSES = REGISTRY.register("technicolor_glasses",
                () -> new ImaginationGlassesItem(new Item.Properties(), ImaginationGlassesKind.TECHNICOLOR));
        ORDERED.add(TECHNICOLOR_GLASSES);

        ADMIN_GLASSES = REGISTRY.register("admin_glasses",
                () -> new ImaginationGlassesItem(new Item.Properties(), ImaginationGlassesKind.ADMIN));
        ORDERED.add(ADMIN_GLASSES);

        LIQUID_SPONGE_ON_A_STICK = REGISTRY.register("liquid_sponge_on_a_stick",
                () -> new SpongeOnAStickItem(new Item.Properties()));
        ORDERED.add(LIQUID_SPONGE_ON_A_STICK);

        SPONGE_ON_A_STICK = REGISTRY.register("sponge_on_a_stick",
                () -> new WaterSpongeOnAStickItem(new Item.Properties(), false));
        ORDERED.add(SPONGE_ON_A_STICK);

        WET_SPONGE_ON_A_STICK = REGISTRY.register("wet_sponge_on_a_stick",
                () -> new WaterSpongeOnAStickItem(new Item.Properties(), true));
        ORDERED.add(WET_SPONGE_ON_A_STICK);

        POINTER = REGISTRY.register("pointer", () -> new PointerItem(new Item.Properties()));
        ORDERED.add(POINTER);

        EPIC_ERASER = REGISTRY.register("epic_eraser", () -> new EpicEraserItem(new Item.Properties()));
        ORDERED.add(EPIC_ERASER);

        TASTY_CLAY = REGISTRY.register("tasty_clay", () -> new TastyClayItem(new Item.Properties()));
        ORDERED.add(TASTY_CLAY);

        INFO_BOOK = REGISTRY.register("info_book", () -> new InfoBookItem(new Item.Properties()));
        ORDERED.add(INFO_BOOK);

        CURSOR = REGISTRY.register("cursor", () -> new CursorItem(new Item.Properties()));
        ORDERED.add(CURSOR);
    }

    private OCItems() {}

    public static List<DeferredItem<? extends Item>> ordered() {
        return Collections.unmodifiableList(ORDERED);
    }

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
