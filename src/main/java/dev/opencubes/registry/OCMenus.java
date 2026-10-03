package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import dev.opencubes.content.automation.AutoAnvilMenu;
import dev.opencubes.content.automation.AutoEnchantmentTableMenu;
import dev.opencubes.content.automation.BlockPlacerMenu;
import dev.opencubes.content.automation.ItemDropperMenu;
import dev.opencubes.content.automation.VacuumHopperMenu;
import dev.opencubes.content.devnull.DevNullMenu;
import dev.opencubes.content.guide.BuildingGuideMenu;
import dev.opencubes.content.luggage.LuggageMenu;
import dev.opencubes.content.heightmap.HeightMapProjectorMenu;
import dev.opencubes.content.sprinkler.SprinklerMenu;
import dev.opencubes.content.paint.DrawingTableMenu;
import dev.opencubes.content.paint.PaintMixerMenu;
import dev.opencubes.content.xp.XpBottlerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class OCMenus {

    private static final DeferredRegister<MenuType<?>> REGISTRY =
            DeferredRegister.create(Registries.MENU, OCConstants.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<XpBottlerMenu>> XP_BOTTLER =
            REGISTRY.register("xp_bottler", () -> IMenuTypeExtension.create(XpBottlerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BlockPlacerMenu>> BLOCK_PLACER =
            REGISTRY.register("block_placer", () -> IMenuTypeExtension.create(BlockPlacerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ItemDropperMenu>> ITEM_DROPPER =
            REGISTRY.register("item_dropper", () -> IMenuTypeExtension.create(ItemDropperMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<VacuumHopperMenu>> VACUUM_HOPPER =
            REGISTRY.register("vacuum_hopper", () -> IMenuTypeExtension.create(VacuumHopperMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<AutoAnvilMenu>> AUTO_ANVIL =
            REGISTRY.register("auto_anvil", () -> IMenuTypeExtension.create(AutoAnvilMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<AutoEnchantmentTableMenu>> AUTO_ENCHANTMENT_TABLE =
            REGISTRY.register("auto_enchanting_table",
                    () -> IMenuTypeExtension.create(AutoEnchantmentTableMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<DevNullMenu>> DEV_NULL =
            REGISTRY.register("dev_null", () -> IMenuTypeExtension.create(DevNullMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<LuggageMenu>> LUGGAGE =
            REGISTRY.register("luggage", () -> IMenuTypeExtension.create(LuggageMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<PaintMixerMenu>> PAINT_MIXER =
            REGISTRY.register("paint_mixer", () -> IMenuTypeExtension.create(PaintMixerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<DrawingTableMenu>> DRAWING_TABLE =
            REGISTRY.register("drawing_table", () -> IMenuTypeExtension.create(DrawingTableMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<HeightMapProjectorMenu>> HEIGHT_MAP_PROJECTOR =
            REGISTRY.register("height_map_projector",
                    () -> IMenuTypeExtension.create(HeightMapProjectorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<SprinklerMenu>> SPRINKLER =
            REGISTRY.register("sprinkler", () -> IMenuTypeExtension.create(SprinklerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BuildingGuideMenu>> BUILDING_GUIDE =
            REGISTRY.register("building_guide", () -> IMenuTypeExtension.create(BuildingGuideMenu::new));

    private OCMenus() {}

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
