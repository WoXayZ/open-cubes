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
            REGISTRY.register("big_button", () -> new BlockEntityType<>(
                    BigButtonBlockEntity::new,
                    OCBlocks.BIG_BUTTONS.values().stream().map(holder -> (Block) holder.get()).toArray(Block[]::new)
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FanBlockEntity>> FAN =
            REGISTRY.register("fan", () -> new BlockEntityType<>(
                    FanBlockEntity::new, OCBlocks.FAN.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BearTrapBlockEntity>> BEAR_TRAP =
            REGISTRY.register("bear_trap", () -> new BlockEntityType<>(
                    BearTrapBlockEntity::new, OCBlocks.BEAR_TRAP.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TankBlockEntity>> TANK =
            REGISTRY.register("tank", () -> new BlockEntityType<>(
                    TankBlockEntity::new, OCBlocks.TANK.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<XpDrainBlockEntity>> XP_DRAIN =
            REGISTRY.register("xp_drain", () -> new BlockEntityType<>(
                    XpDrainBlockEntity::new, OCBlocks.XP_DRAIN.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<XpShowerBlockEntity>> XP_SHOWER =
            REGISTRY.register("xp_shower", () -> new BlockEntityType<>(
                    XpShowerBlockEntity::new, OCBlocks.XP_SHOWER.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<XpBottlerBlockEntity>> XP_BOTTLER =
            REGISTRY.register("xp_bottler", () -> new BlockEntityType<>(
                    XpBottlerBlockEntity::new, OCBlocks.XP_BOTTLER.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VacuumHopperBlockEntity>> VACUUM_HOPPER =
            REGISTRY.register("vacuum_hopper", () -> new BlockEntityType<>(
                    VacuumHopperBlockEntity::new, OCBlocks.VACUUM_HOPPER.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemDropperBlockEntity>> ITEM_DROPPER =
            REGISTRY.register("item_dropper", () -> new BlockEntityType<>(
                    ItemDropperBlockEntity::new, OCBlocks.ITEM_DROPPER.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockBreakerBlockEntity>> BLOCK_BREAKER =
            REGISTRY.register("block_breaker", () -> new BlockEntityType<>(
                    BlockBreakerBlockEntity::new, OCBlocks.BLOCK_BREAKER.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockPlacerBlockEntity>> BLOCK_PLACER =
            REGISTRY.register("block_placer", () -> new BlockEntityType<>(
                    BlockPlacerBlockEntity::new, OCBlocks.BLOCK_PLACER.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoAnvilBlockEntity>> AUTO_ANVIL =
            REGISTRY.register("auto_anvil", () -> new BlockEntityType<>(
                    AutoAnvilBlockEntity::new, OCBlocks.AUTO_ANVIL.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoEnchantmentTableBlockEntity>> AUTO_ENCHANTMENT_TABLE =
            REGISTRY.register("auto_enchanting_table", () -> new BlockEntityType<>(
                    AutoEnchantmentTableBlockEntity::new, OCBlocks.AUTO_ENCHANTMENT_TABLE.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GraveBlockEntity>> GRAVE =
            REGISTRY.register("grave", () -> new BlockEntityType<>(
                    GraveBlockEntity::new, OCBlocks.GRAVE.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TrophyBlockEntity>> TROPHY =
            REGISTRY.register("trophy", () -> new BlockEntityType<>(
                    TrophyBlockEntity::new, OCBlocks.TROPHY.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CanvasBlockEntity>> CANVAS =
            REGISTRY.register("canvas", () -> new BlockEntityType<>(
                    CanvasBlockEntity::new, OCBlocks.CANVAS.get(), OCBlocks.GLASS_CANVAS.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PaintCanBlockEntity>> PAINT_CAN =
            REGISTRY.register("paint_can", () -> new BlockEntityType<>(
                    PaintCanBlockEntity::new, OCBlocks.PAINT_CAN.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PaintMixerBlockEntity>> PAINT_MIXER =
            REGISTRY.register("paint_mixer", () -> new BlockEntityType<>(
                    PaintMixerBlockEntity::new, OCBlocks.PAINT_MIXER.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DrawingTableBlockEntity>> DRAWING_TABLE =
            REGISTRY.register("drawing_table", () -> new BlockEntityType<>(
                    DrawingTableBlockEntity::new, OCBlocks.DRAWING_TABLE.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BuildingGuideBlockEntity>> BUILDING_GUIDE =
            REGISTRY.register("building_guide", () -> new BlockEntityType<>(
                    BuildingGuideBlockEntity::new, OCBlocks.BUILDING_GUIDE.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnhancedBuildingGuideBlockEntity>> ENHANCED_BUILDING_GUIDE =
            REGISTRY.register("enhanced_building_guide", () -> new BlockEntityType<>(
                    EnhancedBuildingGuideBlockEntity::new, OCBlocks.ENHANCED_BUILDING_GUIDE.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeightMapProjectorBlockEntity>> HEIGHT_MAP_PROJECTOR =
            REGISTRY.register("height_map_projector", () -> new BlockEntityType<>(
                    HeightMapProjectorBlockEntity::new, OCBlocks.HEIGHT_MAP_PROJECTOR.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ImaginaryBlockEntity>> IMAGINARY =
            REGISTRY.register("imaginary", () -> new BlockEntityType<>(
                    ImaginaryBlockEntity::new, OCBlocks.IMAGINARY.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HealerBlockEntity>> HEALER =
            REGISTRY.register("healer", () -> new BlockEntityType<>(
                    HealerBlockEntity::new, OCBlocks.HEALER.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SprinklerBlockEntity>> SPRINKLER =
            REGISTRY.register("sprinkler", () -> new BlockEntityType<>(
                    SprinklerBlockEntity::new, OCBlocks.SPRINKLER.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcheryTargetBlockEntity>> ARCHERY_TARGET =
            REGISTRY.register("archery_target", () -> new BlockEntityType<>(
                    ArcheryTargetBlockEntity::new, OCBlocks.ARCHERY_TARGET.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemCannonBlockEntity>> ITEM_CANNON =
            REGISTRY.register("item_cannon", () -> new BlockEntityType<>(
                    ItemCannonBlockEntity::new, OCBlocks.ITEM_CANNON.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GoldenEggBlockEntity>> GOLDEN_EGG =
            REGISTRY.register("golden_egg", () -> new BlockEntityType<>(
                    GoldenEggBlockEntity::new, OCBlocks.GOLDEN_EGG.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VillageHighlighterBlockEntity>> VILLAGE_HIGHLIGHTER =
            REGISTRY.register("village_highlighter", () -> new BlockEntityType<>(
                    VillageHighlighterBlockEntity::new, OCBlocks.VILLAGE_HIGHLIGHTER.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SkyBlockEntity>> SKY_BLOCK =
            REGISTRY.register("sky_block", () -> new BlockEntityType<>(
                    SkyBlockEntity::new, OCBlocks.SKY_BLOCK.get(), OCBlocks.INVERTED_SKY_BLOCK.get()
            ));

    private OCBlockEntities() {}

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
