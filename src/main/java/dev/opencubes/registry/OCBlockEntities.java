package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import dev.opencubes.content.grave.GraveBlockEntity;
import dev.opencubes.content.automation.AutoAnvilBlockEntity;
import dev.opencubes.content.automation.AutoEnchantmentTableBlockEntity;
import dev.opencubes.content.automation.BlockBreakerBlockEntity;
import dev.opencubes.content.automation.BlockPlacerBlockEntity;
import dev.opencubes.content.automation.ItemDropperBlockEntity;
import dev.opencubes.content.automation.VacuumHopperBlockEntity;
import dev.opencubes.content.button.BigButtonBlockEntity;
import dev.opencubes.content.fan.FanBlockEntity;
import dev.opencubes.content.tank.TankBlockEntity;
import dev.opencubes.content.trap.BearTrapBlockEntity;
import dev.opencubes.content.guide.BuildingGuideBlockEntity;
import dev.opencubes.content.guide.EnhancedBuildingGuideBlockEntity;
import dev.opencubes.content.heightmap.HeightMapProjectorBlockEntity;
import dev.opencubes.content.imaginary.ImaginaryBlockEntity;
import dev.opencubes.content.healer.HealerBlockEntity;
import dev.opencubes.content.sprinkler.SprinklerBlockEntity;
import dev.opencubes.content.sky.SkyBlockEntity;
import dev.opencubes.content.target.ArcheryTargetBlockEntity;
import dev.opencubes.content.cannon.ItemCannonBlockEntity;
import dev.opencubes.content.egg.GoldenEggBlockEntity;
import dev.opencubes.content.village.VillageHighlighterBlockEntity;
import dev.opencubes.content.paint.CanvasBlockEntity;
import dev.opencubes.content.paint.DrawingTableBlockEntity;
import dev.opencubes.content.paint.PaintCanBlockEntity;
import dev.opencubes.content.paint.PaintMixerBlockEntity;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import dev.opencubes.content.xp.XpBottlerBlockEntity;
import dev.opencubes.content.xp.XpDrainBlockEntity;
import dev.opencubes.content.xp.XpShowerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class OCBlockEntities {

    private static final DeferredRegister<BlockEntityType<?>> REGISTRY =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, OCConstants.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BigButtonBlockEntity>> BIG_BUTTON =
            REGISTRY.register("big_button", () -> BlockEntityType.Builder.of(
                    BigButtonBlockEntity::new,
                    OCBlocks.BIG_BUTTONS.values().stream().map(holder -> (Block) holder.get()).toArray(Block[]::new)
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FanBlockEntity>> FAN =
            REGISTRY.register("fan", () -> BlockEntityType.Builder.of(
                    FanBlockEntity::new, OCBlocks.FAN.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BearTrapBlockEntity>> BEAR_TRAP =
            REGISTRY.register("bear_trap", () -> BlockEntityType.Builder.of(
                    BearTrapBlockEntity::new, OCBlocks.BEAR_TRAP.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TankBlockEntity>> TANK =
            REGISTRY.register("tank", () -> BlockEntityType.Builder.of(
                    TankBlockEntity::new, OCBlocks.TANK.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<XpDrainBlockEntity>> XP_DRAIN =
            REGISTRY.register("xp_drain", () -> BlockEntityType.Builder.of(
                    XpDrainBlockEntity::new, OCBlocks.XP_DRAIN.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<XpShowerBlockEntity>> XP_SHOWER =
            REGISTRY.register("xp_shower", () -> BlockEntityType.Builder.of(
                    XpShowerBlockEntity::new, OCBlocks.XP_SHOWER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<XpBottlerBlockEntity>> XP_BOTTLER =
            REGISTRY.register("xp_bottler", () -> BlockEntityType.Builder.of(
                    XpBottlerBlockEntity::new, OCBlocks.XP_BOTTLER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VacuumHopperBlockEntity>> VACUUM_HOPPER =
            REGISTRY.register("vacuum_hopper", () -> BlockEntityType.Builder.of(
                    VacuumHopperBlockEntity::new, OCBlocks.VACUUM_HOPPER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemDropperBlockEntity>> ITEM_DROPPER =
            REGISTRY.register("item_dropper", () -> BlockEntityType.Builder.of(
                    ItemDropperBlockEntity::new, OCBlocks.ITEM_DROPPER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockBreakerBlockEntity>> BLOCK_BREAKER =
            REGISTRY.register("block_breaker", () -> BlockEntityType.Builder.of(
                    BlockBreakerBlockEntity::new, OCBlocks.BLOCK_BREAKER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockPlacerBlockEntity>> BLOCK_PLACER =
            REGISTRY.register("block_placer", () -> BlockEntityType.Builder.of(
                    BlockPlacerBlockEntity::new, OCBlocks.BLOCK_PLACER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoAnvilBlockEntity>> AUTO_ANVIL =
            REGISTRY.register("auto_anvil", () -> BlockEntityType.Builder.of(
                    AutoAnvilBlockEntity::new, OCBlocks.AUTO_ANVIL.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoEnchantmentTableBlockEntity>> AUTO_ENCHANTMENT_TABLE =
            REGISTRY.register("auto_enchanting_table", () -> BlockEntityType.Builder.of(
                    AutoEnchantmentTableBlockEntity::new, OCBlocks.AUTO_ENCHANTMENT_TABLE.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GraveBlockEntity>> GRAVE =
            REGISTRY.register("grave", () -> BlockEntityType.Builder.of(
                    GraveBlockEntity::new, OCBlocks.GRAVE.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TrophyBlockEntity>> TROPHY =
            REGISTRY.register("trophy", () -> BlockEntityType.Builder.of(
                    TrophyBlockEntity::new, OCBlocks.TROPHY.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CanvasBlockEntity>> CANVAS =
            REGISTRY.register("canvas", () -> BlockEntityType.Builder.of(
                    CanvasBlockEntity::new, OCBlocks.CANVAS.get(), OCBlocks.GLASS_CANVAS.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PaintCanBlockEntity>> PAINT_CAN =
            REGISTRY.register("paint_can", () -> BlockEntityType.Builder.of(
                    PaintCanBlockEntity::new, OCBlocks.PAINT_CAN.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PaintMixerBlockEntity>> PAINT_MIXER =
            REGISTRY.register("paint_mixer", () -> BlockEntityType.Builder.of(
                    PaintMixerBlockEntity::new, OCBlocks.PAINT_MIXER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DrawingTableBlockEntity>> DRAWING_TABLE =
            REGISTRY.register("drawing_table", () -> BlockEntityType.Builder.of(
                    DrawingTableBlockEntity::new, OCBlocks.DRAWING_TABLE.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BuildingGuideBlockEntity>> BUILDING_GUIDE =
            REGISTRY.register("building_guide", () -> BlockEntityType.Builder.of(
                    BuildingGuideBlockEntity::new, OCBlocks.BUILDING_GUIDE.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnhancedBuildingGuideBlockEntity>> ENHANCED_BUILDING_GUIDE =
            REGISTRY.register("enhanced_building_guide", () -> BlockEntityType.Builder.of(
                    EnhancedBuildingGuideBlockEntity::new, OCBlocks.ENHANCED_BUILDING_GUIDE.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeightMapProjectorBlockEntity>> HEIGHT_MAP_PROJECTOR =
            REGISTRY.register("height_map_projector", () -> BlockEntityType.Builder.of(
                    HeightMapProjectorBlockEntity::new, OCBlocks.HEIGHT_MAP_PROJECTOR.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ImaginaryBlockEntity>> IMAGINARY =
            REGISTRY.register("imaginary", () -> BlockEntityType.Builder.of(
                    ImaginaryBlockEntity::new, OCBlocks.IMAGINARY.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HealerBlockEntity>> HEALER =
            REGISTRY.register("healer", () -> BlockEntityType.Builder.of(
                    HealerBlockEntity::new, OCBlocks.HEALER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SprinklerBlockEntity>> SPRINKLER =
            REGISTRY.register("sprinkler", () -> BlockEntityType.Builder.of(
                    SprinklerBlockEntity::new, OCBlocks.SPRINKLER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcheryTargetBlockEntity>> ARCHERY_TARGET =
            REGISTRY.register("archery_target", () -> BlockEntityType.Builder.of(
                    ArcheryTargetBlockEntity::new, OCBlocks.ARCHERY_TARGET.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemCannonBlockEntity>> ITEM_CANNON =
            REGISTRY.register("item_cannon", () -> BlockEntityType.Builder.of(
                    ItemCannonBlockEntity::new, OCBlocks.ITEM_CANNON.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GoldenEggBlockEntity>> GOLDEN_EGG =
            REGISTRY.register("golden_egg", () -> BlockEntityType.Builder.of(
                    GoldenEggBlockEntity::new, OCBlocks.GOLDEN_EGG.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VillageHighlighterBlockEntity>> VILLAGE_HIGHLIGHTER =
            REGISTRY.register("village_highlighter", () -> BlockEntityType.Builder.of(
                    VillageHighlighterBlockEntity::new, OCBlocks.VILLAGE_HIGHLIGHTER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SkyBlockEntity>> SKY_BLOCK =
            REGISTRY.register("sky_block", () -> BlockEntityType.Builder.of(
                    SkyBlockEntity::new, OCBlocks.SKY_BLOCK.get(), OCBlocks.INVERTED_SKY_BLOCK.get()
            ).build(null));

    private OCBlockEntities() {}

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
