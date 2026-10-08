package dev.opencubes.client;

import dev.opencubes.OCConstants;
import dev.opencubes.client.cannon.ItemCannonRenderer;
import dev.opencubes.client.crane.CraneBackpackLayer;
import dev.opencubes.client.crane.MagnetRenderer;
import dev.opencubes.client.guide.BuildingGuideScreen;
import dev.opencubes.client.crane.MountedBlockRenderer;
import dev.opencubes.client.heightmap.CartographerRenderer;
import dev.opencubes.client.heightmap.HeightMapProjectorRenderer;
import dev.opencubes.client.heightmap.HeightMapProjectorScreen;
import dev.opencubes.client.imaginary.ImaginaryRenderer;
import dev.opencubes.client.luggage.LuggageItemRenderer;
import dev.opencubes.client.luggage.LuggageModel;
import dev.opencubes.client.egg.MiniMeRenderer;
import dev.opencubes.client.egg.GoldenEggRenderer;
import dev.opencubes.client.flight.HangGliderLayer;
import dev.opencubes.client.flight.ThermalElytraLayer;
import dev.opencubes.client.sprinkler.SprinklerRenderer;
import dev.opencubes.client.sprinkler.SprinklerScreen;
import dev.opencubes.client.sky.SkyBlockRenderer;
import dev.opencubes.client.village.VillageHighlighterRenderer;
import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCEntities;
import dev.opencubes.registry.OCFluids;
import dev.opencubes.registry.OCItems;
import dev.opencubes.registry.OCMenus;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = OCConstants.MOD_ID, value = Dist.CLIENT)
public final class OCClientSetup {

    private OCClientSetup() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(OCBlockEntities.FAN.get(), FanRenderer::new);
        event.registerBlockEntityRenderer(OCBlockEntities.TANK.get(), TankRenderer::new);
        event.registerBlockEntityRenderer(OCBlockEntities.TROPHY.get(), TrophyRenderer::new);
        event.registerBlockEntityRenderer(OCBlockEntities.CANVAS.get(), CanvasRenderer::new);
        event.registerBlockEntityRenderer(OCBlockEntities.BUILDING_GUIDE.get(), GuideMarkerRenderer::new);
        event.registerBlockEntityRenderer(OCBlockEntities.ENHANCED_BUILDING_GUIDE.get(), GuideMarkerRenderer::new);
        event.registerBlockEntityRenderer(OCBlockEntities.HEIGHT_MAP_PROJECTOR.get(), HeightMapProjectorRenderer::new);
        event.registerBlockEntityRenderer(OCBlockEntities.ITEM_CANNON.get(), ItemCannonRenderer::new);
        event.registerBlockEntityRenderer(OCBlockEntities.IMAGINARY.get(), ImaginaryRenderer::new);
        event.registerBlockEntityRenderer(OCBlockEntities.GOLDEN_EGG.get(), GoldenEggRenderer::new);
        event.registerBlockEntityRenderer(OCBlockEntities.VILLAGE_HIGHLIGHTER.get(), VillageHighlighterRenderer::new);
        event.registerBlockEntityRenderer(OCBlockEntities.SPRINKLER.get(), SprinklerRenderer::new);
        event.registerBlockEntityRenderer(OCBlockEntities.SKY_BLOCK.get(), SkyBlockRenderer::new);
        event.registerEntityRenderer(OCEntities.LUGGAGE.get(), LuggageRenderer::new);
        event.registerEntityRenderer(OCEntities.MINI_ME.get(), MiniMeRenderer::new);
        event.registerEntityRenderer(OCEntities.CARTOGRAPHER.get(), CartographerRenderer::new);
        event.registerEntityRenderer(OCEntities.MAGNET.get(), MagnetRenderer::new);
        event.registerEntityRenderer(OCEntities.MOUNTED_BLOCK.get(), MountedBlockRenderer::new);
        event.registerEntityRenderer(OCEntities.GOLDEN_EYE.get(), GoldenEyeRenderer::new);
        event.registerEntityRenderer(OCEntities.GLYPH.get(), GlyphRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LuggageModel.NORMAL_LAYER, () -> LuggageModel.createBodyLayer(64, 64, 16));
        event.registerLayerDefinition(LuggageModel.SPECIAL_LAYER, () -> LuggageModel.createBodyLayer(128, 64, 23));
        event.registerLayerDefinition(GlassesModel.LAYER, GlassesModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerStandaloneModels(ModelEvent.RegisterStandalone event) {
        event.register(FanRenderer.BLADES_MODEL, FanRenderer.BLADES_BAKER);
        event.register(FanRenderer.FRAME_MODEL, FanRenderer.FRAME_BAKER);
        event.register(SprinklerRenderer.ARM_MODEL, SprinklerRenderer.ARM_BAKER);
    }

    @SubscribeEvent
    public static void registerSpecialModels(RegisterSpecialModelRendererEvent event) {
        event.register(OCConstants.id("luggage"), LuggageItemRenderer.Unbaked.CODEC);
        event.register(OCConstants.id("trophy"), TrophyItemRenderer.Unbaked.CODEC);
    }

    @SubscribeEvent
    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        event.register(new FluidModel.Unbaked(
                new Material(OCConstants.id("block/xp_juice_still")),
                new Material(OCConstants.id("block/xp_juice_flowing")),
                null,
                null), OCFluids.XP_JUICE, OCFluids.FLOWING_XP_JUICE);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(OCMenus.XP_BOTTLER.get(), XpBottlerScreen::new);
        event.register(OCMenus.BLOCK_PLACER.get(), BlockPlacerScreen::new);
        event.register(OCMenus.ITEM_DROPPER.get(), ItemDropperScreen::new);
        event.register(OCMenus.VACUUM_HOPPER.get(), VacuumHopperScreen::new);
        event.register(OCMenus.AUTO_ANVIL.get(), AutoAnvilScreen::new);
        event.register(OCMenus.AUTO_ENCHANTMENT_TABLE.get(), AutoEnchantmentTableScreen::new);
        event.register(OCMenus.DEV_NULL.get(), DevNullScreen::new);
        event.register(OCMenus.LUGGAGE.get(), LuggageScreen::new);
        event.register(OCMenus.PAINT_MIXER.get(), PaintMixerScreen::new);
        event.register(OCMenus.DRAWING_TABLE.get(), DrawingTableScreen::new);
        event.register(OCMenus.HEIGHT_MAP_PROJECTOR.get(), HeightMapProjectorScreen::new);
        event.register(OCMenus.SPRINKLER.get(), SprinklerScreen::new);
        event.register(OCMenus.BUILDING_GUIDE.get(), BuildingGuideScreen::new);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {}, OCFluids.XP_JUICE_TYPE.get());

        event.registerBlock(new IClientBlockExtensions() {
            // Vanilla hit particles read the context-free shape, which is empty unless the block is
            // inverted, and crash on it.
            @Override
            public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
                return target instanceof BlockHitResult hit
                        && state.getShape(level, hit.getBlockPos()).isEmpty();
            }
        }, OCBlocks.IMAGINARY.get());
    }

    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            AvatarRenderer<AbstractClientPlayer> renderer = event.getPlayerRenderer(skin);
            if (renderer != null) {
                renderer.addLayer(new GlassesLayer<>(renderer, event.getContext().getModelSet()));
                renderer.addLayer(new HangGliderLayer(renderer));
                renderer.addLayer(new ThermalElytraLayer(renderer, event.getContext().getModelSet()));
                renderer.addLayer(new CraneBackpackLayer(renderer));
            }
        }
        if (event.getRenderer(EntityType.ARMOR_STAND) instanceof ArmorStandRenderer standRenderer) {
            standRenderer.addLayer(new GlassesLayer<>(standRenderer, event.getContext().getModelSet()));
        }
    }
}
