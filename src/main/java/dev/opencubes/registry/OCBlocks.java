package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import dev.opencubes.content.grave.GraveBlock;
import dev.opencubes.content.automation.AutoAnvilBlock;
import dev.opencubes.content.automation.AutoEnchantmentTableBlock;
import dev.opencubes.content.automation.BlockBreakerBlock;
import dev.opencubes.content.automation.BlockPlacerBlock;
import dev.opencubes.content.automation.ItemDropperBlock;
import dev.opencubes.content.automation.VacuumHopperBlock;
import dev.opencubes.content.button.BigButtonBlock;
import dev.opencubes.content.button.BigButtonMaterial;
import dev.opencubes.content.elevator.ElevatorBlock;
import dev.opencubes.content.elevator.RotatingElevatorBlock;
import dev.opencubes.content.fan.FanBlock;
import dev.opencubes.content.flag.FlagBlock;
import dev.opencubes.content.ladder.RopeLadderBlock;
import dev.opencubes.content.tank.TankBlock;
import dev.opencubes.content.trap.BearTrapBlock;
import dev.opencubes.content.guide.BuildingGuideBlock;
import dev.opencubes.content.guide.EnhancedBuildingGuideBlock;
import dev.opencubes.content.heightmap.HeightMapProjectorBlock;
import dev.opencubes.content.imaginary.ImaginaryBlock;
import dev.opencubes.content.sky.SkyBlock;
import dev.opencubes.content.sponge.SpongeBlock;
import dev.opencubes.content.scaffolding.TemporaryScaffoldingBlock;
import dev.opencubes.content.healer.HealerBlock;
import dev.opencubes.content.sprinkler.SprinklerBlock;
import dev.opencubes.content.target.ArcheryTargetBlock;
import dev.opencubes.content.cannon.ItemCannonBlock;
import dev.opencubes.content.egg.GoldenEggBlock;
import dev.opencubes.content.village.VillageHighlighterBlock;
import dev.opencubes.content.paint.CanvasBlock;
import dev.opencubes.content.paint.DrawingTableBlock;
import dev.opencubes.content.paint.GlassCanvasBlock;
import dev.opencubes.content.paint.PaintCanBlock;
import dev.opencubes.content.paint.PaintMixerBlock;
import dev.opencubes.content.trophy.TrophyBlock;
import dev.opencubes.content.xp.XpBottlerBlock;
import dev.opencubes.content.xp.XpDrainBlock;
import dev.opencubes.content.xp.XpShowerBlock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

public final class OCBlocks {

    private static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(OCConstants.MOD_ID);
    private static final List<DeferredBlock<? extends Block>> ORDERED = new ArrayList<>();

    public static final Map<DyeColor, DeferredBlock<ElevatorBlock>> ELEVATORS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, DeferredBlock<RotatingElevatorBlock>> ROTATING_ELEVATORS = new EnumMap<>(DyeColor.class);
    public static final Map<BigButtonMaterial, DeferredBlock<BigButtonBlock>> BIG_BUTTONS = new LinkedHashMap<>();
    public static final Map<DyeColor, DeferredBlock<FlagBlock>> FLAGS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, DeferredBlock<SlabBlock>> WOOL_SLABS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, DeferredBlock<StairBlock>> WOOL_STAIRS = new EnumMap<>(DyeColor.class);

    public static final DeferredBlock<RopeLadderBlock> ROPE_LADDER;
    public static final DeferredBlock<FanBlock> FAN;
    public static final DeferredBlock<BearTrapBlock> BEAR_TRAP;
    public static final DeferredBlock<TankBlock> TANK;
    public static final DeferredBlock<XpDrainBlock> XP_DRAIN;
    public static final DeferredBlock<XpShowerBlock> XP_SHOWER;
    public static final DeferredBlock<XpBottlerBlock> XP_BOTTLER;
    public static final DeferredBlock<VacuumHopperBlock> VACUUM_HOPPER;
    public static final DeferredBlock<ItemDropperBlock> ITEM_DROPPER;
    public static final DeferredBlock<BlockBreakerBlock> BLOCK_BREAKER;
    public static final DeferredBlock<BlockPlacerBlock> BLOCK_PLACER;
    public static final DeferredBlock<AutoAnvilBlock> AUTO_ANVIL;
    public static final DeferredBlock<AutoEnchantmentTableBlock> AUTO_ENCHANTMENT_TABLE;
    public static final DeferredBlock<GraveBlock> GRAVE;
    public static final DeferredBlock<TrophyBlock> TROPHY;
    public static final DeferredBlock<CanvasBlock> CANVAS;
    public static final DeferredBlock<GlassCanvasBlock> GLASS_CANVAS;
    public static final DeferredBlock<PaintCanBlock> PAINT_CAN;
    public static final DeferredBlock<PaintMixerBlock> PAINT_MIXER;
    public static final DeferredBlock<DrawingTableBlock> DRAWING_TABLE;
    public static final DeferredBlock<BuildingGuideBlock> BUILDING_GUIDE;
    public static final DeferredBlock<EnhancedBuildingGuideBlock> ENHANCED_BUILDING_GUIDE;
    public static final DeferredBlock<HeightMapProjectorBlock> HEIGHT_MAP_PROJECTOR;
    public static final DeferredBlock<ImaginaryBlock> IMAGINARY;
    public static final DeferredBlock<SkyBlock> SKY_BLOCK;
    public static final DeferredBlock<SkyBlock> INVERTED_SKY_BLOCK;
    public static final DeferredBlock<TemporaryScaffoldingBlock> TEMPORARY_SCAFFOLDING;
    public static final DeferredBlock<SpongeBlock> LIQUID_SPONGE;
    public static final DeferredBlock<HealerBlock> HEALER;
    public static final DeferredBlock<SprinklerBlock> SPRINKLER;
    public static final DeferredBlock<ArcheryTargetBlock> ARCHERY_TARGET;
    public static final DeferredBlock<ItemCannonBlock> ITEM_CANNON;
    public static final DeferredBlock<GoldenEggBlock> GOLDEN_EGG;
    public static final DeferredBlock<VillageHighlighterBlock> VILLAGE_HIGHLIGHTER;

    static {
        for (DyeColor colour : DyeColor.values()) {
            ELEVATORS.put(colour, register(colour.getName() + "_elevator",
                    () -> new ElevatorBlock(colour, elevatorProperties(colour))));
        }
        for (DyeColor colour : DyeColor.values()) {
            ROTATING_ELEVATORS.put(colour, register(colour.getName() + "_rotating_elevator",
                    () -> new RotatingElevatorBlock(colour, elevatorProperties(colour))));
        }
        for (BigButtonMaterial material : BigButtonMaterial.ALL) {
            BIG_BUTTONS.put(material, register(material.blockId(),
                    () -> new BigButtonBlock(material.setType(), stamp(material.properties()))));
        }

        ROPE_LADDER = register("rope_ladder",
                () -> new RopeLadderBlock(props()
                        .mapColor(MapColor.WOOD)
                        .strength(0.4F)
                        .sound(SoundType.LADDER)
                        .noOcclusion()
                        .pushReaction(PushReaction.DESTROY)));

        FAN = register("fan",
                () -> new FanBlock(props()
                        .mapColor(MapColor.METAL)
                        .strength(1.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()));

        BEAR_TRAP = register("bear_trap",
                () -> new BearTrapBlock(props()
                        .mapColor(MapColor.METAL)
                        .strength(2.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()));

        TANK = register("tank",
                () -> new TankBlock(props()
                        .mapColor(MapColor.NONE)
                        .strength(0.5F)
                        .sound(SoundType.GLASS)
                        .noOcclusion()
                        .isViewBlocking((state, level, pos) -> false)
                        .isSuffocating((state, level, pos) -> false)
                        .isRedstoneConductor((state, level, pos) -> false)));

        XP_DRAIN = register("xp_drain",
                () -> new XpDrainBlock(props()
                        .mapColor(MapColor.METAL)
                        .strength(3.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()));

        XP_SHOWER = register("xp_shower",
                () -> new XpShowerBlock(props()
                        .mapColor(MapColor.METAL)
                        .strength(1.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()));

        XP_BOTTLER = register("xp_bottler",
                () -> new XpBottlerBlock(props()
                        .mapColor(MapColor.METAL)
                        .strength(3.0F)
                        .sound(SoundType.METAL)));

        VACUUM_HOPPER = register("vacuum_hopper",
                () -> new VacuumHopperBlock(machineProperties().noOcclusion()));
        ITEM_DROPPER = register("item_dropper",
                () -> new ItemDropperBlock(machineProperties()));
        BLOCK_BREAKER = register("block_breaker",
                () -> new BlockBreakerBlock(machineProperties()));
        BLOCK_PLACER = register("block_placer",
                () -> new BlockPlacerBlock(machineProperties()));

        AUTO_ANVIL = register("auto_anvil",
                () -> new AutoAnvilBlock(props()
                        .mapColor(MapColor.METAL)
                        .requiresCorrectToolForDrops()
                        .strength(5.0F, 1200.0F)
                        .sound(SoundType.ANVIL)
                        .noOcclusion()));

        AUTO_ENCHANTMENT_TABLE = register("auto_enchanting_table",
                () -> new AutoEnchantmentTableBlock(props()
                        .mapColor(MapColor.COLOR_RED)
                        .strength(5.0F, 1200.0F)
                        .sound(SoundType.STONE)));

        GRAVE = register("grave",
                () -> new GraveBlock(props()
                        .mapColor(MapColor.STONE)
                        .strength(5.0F, 2000.0F)
                        .sound(SoundType.STONE)
                        .noOcclusion()));

        TROPHY = register("trophy",
                () -> new TrophyBlock(props()
                        .mapColor(MapColor.STONE)
                        .strength(1.5F, 6.0F)
                        .sound(SoundType.STONE)
                        .noOcclusion()));

        CANVAS = register("canvas",
                () -> new CanvasBlock(props()
                        .mapColor(MapColor.SNOW)
                        .strength(0.5F)
                        .sound(SoundType.WOOL)));

        GLASS_CANVAS = register("glass_canvas",
                () -> new GlassCanvasBlock(props()
                        .mapColor(MapColor.NONE)
                        .strength(0.3F)
                        .sound(SoundType.GLASS)
                        .noOcclusion()));

        PAINT_CAN = register("paint_can",
                () -> new PaintCanBlock(props()
                        .mapColor(MapColor.METAL)
                        .strength(1.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()));

        PAINT_MIXER = register("paint_mixer",
                () -> new PaintMixerBlock(props()
                        .mapColor(MapColor.METAL)
                        .strength(2.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()));

        DRAWING_TABLE = register("drawing_table",
                () -> new DrawingTableBlock(props()
                        .mapColor(MapColor.WOOD)
                        .strength(2.0F)
                        .sound(SoundType.WOOD)));

        BUILDING_GUIDE = register("building_guide",
                () -> new BuildingGuideBlock(props()
                        .mapColor(MapColor.NONE)
                        .strength(1.0F)
                        .sound(SoundType.GLASS)
                        .lightLevel(state -> 10)
                        .noOcclusion()));

        ENHANCED_BUILDING_GUIDE = register("enhanced_building_guide",
                () -> new EnhancedBuildingGuideBlock(props()
                        .mapColor(MapColor.NONE)
                        .strength(1.0F)
                        .sound(SoundType.GLASS)
                        .lightLevel(state -> 10)
                        .noOcclusion()));

        HEIGHT_MAP_PROJECTOR = register("height_map_projector",
                () -> new HeightMapProjectorBlock(props()
                        .mapColor(MapColor.METAL)
                        .strength(2.0F)
                        .sound(SoundType.METAL)
                        // Light amount comes from config at runtime via getLightEmission.
                        .lightLevel(state -> state.getValue(HeightMapProjectorBlock.ACTIVE) ? 10 : 0)
                        .noOcclusion()));

        IMAGINARY = register("imaginary_block",
                () -> new ImaginaryBlock(props()
                        .mapColor(MapColor.NONE)
                        .strength(0.0F)
                        .sound(SoundType.SNOW)
                        .noOcclusion()
                        .pushReaction(PushReaction.DESTROY)));

        SKY_BLOCK = register("sky_block",
                () -> new SkyBlock(props()
                        .mapColor(MapColor.METAL)
                        .strength(1.5F)
                        .sound(SoundType.METAL)
                        .noOcclusion(), false));

        INVERTED_SKY_BLOCK = register("inverted_sky_block",
                () -> new SkyBlock(props()
                        .mapColor(MapColor.METAL)
                        .strength(1.5F)
                        .sound(SoundType.METAL)
                        .noOcclusion(), true));

        TEMPORARY_SCAFFOLDING = register("temporary_scaffolding",
                () -> new TemporaryScaffoldingBlock(props()
                        .mapColor(MapColor.WOOD)
                        .strength(0.1F)
                        .sound(SoundType.SCAFFOLDING)
                        .noOcclusion()
                        .noCollision()
                        .dynamicShape()
                        .isViewBlocking((s, g, p) -> false)
                        .pushReaction(PushReaction.DESTROY)
                        .randomTicks()));

        LIQUID_SPONGE = register("liquid_sponge",
                () -> new SpongeBlock(props()
                        .mapColor(MapColor.COLOR_YELLOW)
                        .strength(0.6F)
                        .sound(SoundType.GRASS)));

        HEALER = register("healer",
                () -> new HealerBlock(props()
                        .mapColor(MapColor.COLOR_PINK)
                        .strength(1.5F)
                        .sound(SoundType.STONE)));

        SPRINKLER = register("sprinkler",
                () -> new SprinklerBlock(props()
                        .mapColor(MapColor.METAL)
                        .strength(1.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()));

        ARCHERY_TARGET = register("archery_target",
                () -> new ArcheryTargetBlock(props()
                        .mapColor(MapColor.WOOD)
                        .strength(1.0F)
                        .sound(SoundType.WOOD)
                        .noOcclusion()
                        .lightLevel(s -> 5)));

        ITEM_CANNON = register("item_cannon",
                () -> new ItemCannonBlock(props()
                        .mapColor(MapColor.METAL)
                        .strength(2.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()));

        GOLDEN_EGG = register("golden_egg",
                () -> new GoldenEggBlock(props()
                        .mapColor(MapColor.GOLD)
                        .strength(1.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()
                        .lightLevel(s -> 4)));

        VILLAGE_HIGHLIGHTER = register("village_highlighter",
                () -> new VillageHighlighterBlock(props()
                        .mapColor(MapColor.METAL)
                        .strength(1.5F)
                        .sound(SoundType.METAL)
                        .noOcclusion()));

        for (DyeColor colour : DyeColor.values()) {
            FLAGS.put(colour, register(colour.getName() + "_flag",
                    () -> new FlagBlock(colour, props()
                            .mapColor(colour.getMapColor())
                            .strength(0.0F)
                            .sound(SoundType.WOOL)
                            .noOcclusion()
                            .noCollision()
                            .pushReaction(PushReaction.DESTROY))));
        }

        for (DyeColor colour : DyeColor.values()) {
            WOOL_SLABS.put(colour, register(colour.getName() + "_wool_slab",
                    () -> new SlabBlock(woolProperties(colour))));
        }
        for (DyeColor colour : DyeColor.values()) {
            WOOL_STAIRS.put(colour, register(colour.getName() + "_wool_stairs",
                    () -> new StairBlock(wool(colour).defaultBlockState(), woolProperties(colour))));
        }
    }

    private OCBlocks() {}

    private static BlockBehaviour.Properties elevatorProperties(DyeColor colour) {
        return stamp(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                .mapColor(colour.getMapColor())
                .sound(SoundType.STONE)
                .strength(1.5F, 6.0F));
    }

    public static Block wool(DyeColor colour) {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(
                net.minecraft.resources.Identifier.withDefaultNamespace(colour.getName() + "_wool"));
    }

    private static BlockBehaviour.Properties woolProperties(DyeColor colour) {
        return stamp(BlockBehaviour.Properties.ofFullCopy(wool(colour)).mapColor(colour.getMapColor()));
    }

    private static BlockBehaviour.Properties machineProperties() {
        return props()
                .mapColor(MapColor.METAL)
                .strength(3.0F)
                .sound(SoundType.METAL);
    }

    private static final ThreadLocal<ResourceKey<Block>> REGISTERING_ID = new ThreadLocal<>();

    /** Properties for the block currently being registered. 26.1 requires the registry id before construction. */
    private static BlockBehaviour.Properties props() {
        return stamp(BlockBehaviour.Properties.of());
    }

    private static BlockBehaviour.Properties stamp(BlockBehaviour.Properties properties) {
        ResourceKey<Block> id = REGISTERING_ID.get();
        if (id != null) {
            properties.setId(id);
        }
        return properties;
    }

    private static <B extends Block> DeferredBlock<B> register(String name, java.util.function.Supplier<B> supplier) {
        DeferredBlock<B> block = REGISTRY.register(name, id -> {
            REGISTERING_ID.set(ResourceKey.create(Registries.BLOCK, id));
            try {
                return supplier.get();
            } finally {
                REGISTERING_ID.remove();
            }
        });
        ORDERED.add(block);
        return block;
    }

    public static List<DeferredBlock<? extends Block>> ordered() {
        return Collections.unmodifiableList(ORDERED);
    }

    @Nullable
    public static Block recolour(Block block, DyeColor colour) {
        if (block instanceof RotatingElevatorBlock) {
            return ROTATING_ELEVATORS.get(colour).get();
        }
        if (block instanceof ElevatorBlock) {
            return ELEVATORS.get(colour).get();
        }
        if (block instanceof FlagBlock) {
            return FLAGS.get(colour).get();
        }
        return null;
    }

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
