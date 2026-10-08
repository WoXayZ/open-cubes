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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
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
                ORDERED.add(item(block.getId().getPath(),
                        () -> new SkyBlockItem(block.get(), props().useBlockDescriptionPrefix(), inverted)));
                return;
            }
            ORDERED.add(REGISTRY.registerSimpleBlockItem(block));
        });

        // Needs vanilla's scaffolding placement walk, so it cannot use the simple block item above.
        TEMPORARY_SCAFFOLDING = item("temporary_scaffolding",
                () -> new TemporaryScaffoldingBlockItem(OCBlocks.TEMPORARY_SCAFFOLDING.get(), props().useBlockDescriptionPrefix()));
        ORDERED.add(TEMPORARY_SCAFFOLDING);

        // Top-edge clicks redirect onto the side face so the ladder can place and unroll.
        ROPE_LADDER = item("rope_ladder",
                () -> new RopeLadderBlockItem(OCBlocks.ROPE_LADDER.get(), props().useBlockDescriptionPrefix()));
        ORDERED.add(ROPE_LADDER);

        TROPHY = item("trophy",
                () -> new TrophyBlockItem(OCBlocks.TROPHY.get(), props().useBlockDescriptionPrefix()));
        ORDERED.add(TROPHY);

        TANK = item("tank",
                () -> new TankBlockItem(OCBlocks.TANK.get(), props().useBlockDescriptionPrefix()));
        ORDERED.add(TANK);

        PAINT_CAN = item("paint_can",
                () -> new PaintCanItem(OCBlocks.PAINT_CAN.get(), props().stacksTo(1).useBlockDescriptionPrefix()));
        ORDERED.add(PAINT_CAN);

        XP_BUCKET = item("xp_bucket", () -> new XpBucketItem(props()));
        ORDERED.add(XP_BUCKET);

        WRENCH = item("wrench", () -> new WrenchItem(props()));
        ORDERED.add(WRENCH);

        SLIMALYZER = item("slimalyzer", () -> new SlimalyzerItem(props()));
        ORDERED.add(SLIMALYZER);

        PEDOMETER = item("pedometer", () -> new PedometerItem(props()));
        ORDERED.add(PEDOMETER);

        DEV_NULL = item("dev_null", () -> new DevNullItem(props()));
        ORDERED.add(DEV_NULL);

        for (DyeColor colour : DyeColor.values()) {
            DeferredItem<SleepingBagItem> bag = item(colour.getName() + "_sleeping_bag",
                    () -> new SleepingBagItem(props(), colour));
            SLEEPING_BAGS.put(colour, bag);
            ORDERED.add(bag);
        }

        GOLDEN_EYE = item("golden_eye", () -> new GoldenEyeItem(props()));
        ORDERED.add(GOLDEN_EYE);

        LUGGAGE = item("luggage", () -> new LuggageItem(props()));
        ORDERED.add(LUGGAGE);

        PAINT_BRUSH = item("paint_brush", () -> new PaintBrushItem(props()));
        ORDERED.add(PAINT_BRUSH);

        SQUEEGEE = item("squeegee", () -> new SqueegeeItem(props()));
        ORDERED.add(SQUEEGEE);

        STENCIL = item("stencil", () -> new StencilItem(props()));
        ORDERED.add(STENCIL);

        UNPREPARED_STENCIL = item("unprepared_stencil", () -> new Item(props()));
        ORDERED.add(UNPREPARED_STENCIL);

        SKETCHING_PENCIL = item("sketching_pencil", () -> new Item(props()));
        ORDERED.add(SKETCHING_PENCIL);

        GLYPH = item("glyph", () -> new GlyphItem(props()));
        ORDERED.add(GLYPH);

        EMPTY_MAP = item("empty_map", () -> new EmptyMapItem(props()));
        ORDERED.add(EMPTY_MAP);

        HEIGHT_MAP = item("height_map", () -> new HeightMapItem(props()));
        ORDERED.add(HEIGHT_MAP);

        CARTOGRAPHER = item("cartographer", () -> new CartographerItem(props()));
        ORDERED.add(CARTOGRAPHER);

        BEAM = item("beam", () -> new Item(props()));
        ORDERED.add(BEAM);

        LINE = item("line", () -> new Item(props()));
        ORDERED.add(LINE);

        CRANE_ENGINE = item("crane_engine", () -> new Item(props()));
        ORDERED.add(CRANE_ENGINE);

        CRANE_MAGNET = item("crane_magnet", () -> new Item(props()));
        ORDERED.add(CRANE_MAGNET);

        CRANE_BACKPACK = item("crane_backpack",
                () -> new CraneBackpackItem(props()));
        ORDERED.add(CRANE_BACKPACK);

        CRANE_CONTROL = item("crane_control",
                () -> new CraneControlItem(props()));
        ORDERED.add(CRANE_CONTROL);

        GLIDER_WING_LEFT = item("glider_wing_left", () -> new Item(props()));
        ORDERED.add(GLIDER_WING_LEFT);

        GLIDER_WING_RIGHT = item("glider_wing_right", () -> new Item(props()));
        ORDERED.add(GLIDER_WING_RIGHT);

        HANG_GLIDER = item("hang_glider", () -> new HangGliderItem(props()));
        ORDERED.add(HANG_GLIDER);

        THERMAL_ELYTRA = item("thermal_elytra", () -> new ThermalElytraItem(props()));
        ORDERED.add(THERMAL_ELYTRA);

        SONIC_GLASSES = item("sonic_glasses", () -> new SonicGlassesItem(props()));
        ORDERED.add(SONIC_GLASSES);

        PENCIL = item("pencil",
                () -> new ImaginaryItem(OCBlocks.IMAGINARY.get(), props(), false));
        ORDERED.add(PENCIL);

        CRAYON = item("crayon",
                () -> new ImaginaryItem(OCBlocks.IMAGINARY.get(), props(), true));
        ORDERED.add(CRAYON);

        PENCIL_GLASSES = item("pencil_glasses",
                () -> new ImaginationGlassesItem(props(), ImaginationGlassesKind.PENCIL));
        ORDERED.add(PENCIL_GLASSES);

        CRAYON_GLASSES = item("crayon_glasses",
                () -> new ImaginationGlassesItem(props(), ImaginationGlassesKind.CRAYON));
        ORDERED.add(CRAYON_GLASSES);

        TECHNICOLOR_GLASSES = item("technicolor_glasses",
                () -> new ImaginationGlassesItem(props(), ImaginationGlassesKind.TECHNICOLOR));
        ORDERED.add(TECHNICOLOR_GLASSES);

        ADMIN_GLASSES = item("admin_glasses",
                () -> new ImaginationGlassesItem(props(), ImaginationGlassesKind.ADMIN));
        ORDERED.add(ADMIN_GLASSES);

        LIQUID_SPONGE_ON_A_STICK = item("liquid_sponge_on_a_stick",
                () -> new SpongeOnAStickItem(props()));
        ORDERED.add(LIQUID_SPONGE_ON_A_STICK);

        SPONGE_ON_A_STICK = item("sponge_on_a_stick",
                () -> new WaterSpongeOnAStickItem(props(), false));
        ORDERED.add(SPONGE_ON_A_STICK);

        WET_SPONGE_ON_A_STICK = item("wet_sponge_on_a_stick",
                () -> new WaterSpongeOnAStickItem(props(), true));
        ORDERED.add(WET_SPONGE_ON_A_STICK);

        POINTER = item("pointer", () -> new PointerItem(props()));
        ORDERED.add(POINTER);

        EPIC_ERASER = item("epic_eraser", () -> new EpicEraserItem(props()));
        ORDERED.add(EPIC_ERASER);

        TASTY_CLAY = item("tasty_clay", () -> new TastyClayItem(props()));
        ORDERED.add(TASTY_CLAY);

        INFO_BOOK = item("info_book", () -> new InfoBookItem(props()));
        ORDERED.add(INFO_BOOK);

        CURSOR = item("cursor", () -> new CursorItem(props()));
        ORDERED.add(CURSOR);
    }

    private OCItems() {}

    private static final ThreadLocal<ResourceKey<Item>> REGISTERING_ID = new ThreadLocal<>();

    /** Properties for the item currently being registered. 26.1 requires the registry id before construction. */
    private static Item.Properties props() {
        Item.Properties properties = new Item.Properties();
        ResourceKey<Item> id = REGISTERING_ID.get();
        if (id != null) {
            properties.setId(id);
        }
        return properties;
    }

    private static <I extends Item> DeferredItem<I> item(String name, java.util.function.Supplier<I> supplier) {
        return REGISTRY.register(name, id -> {
            REGISTERING_ID.set(ResourceKey.create(Registries.ITEM, id));
            try {
                return supplier.get();
            } finally {
                REGISTERING_ID.remove();
            }
        });
    }

    public static List<DeferredItem<? extends Item>> ordered() {
        return Collections.unmodifiableList(ORDERED);
    }

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
