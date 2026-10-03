package dev.opencubes;

import dev.opencubes.config.OCClientConfig;
import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCArmorMaterials;
import dev.opencubes.registry.OCAttachments;
import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCCreativeTabs;
import dev.opencubes.registry.OCCriteria;
import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.registry.OCEntities;
import dev.opencubes.registry.OCFluids;
import dev.opencubes.registry.OCGameRules;
import dev.opencubes.registry.OCItems;
import dev.opencubes.registry.OCLootModifiers;
import dev.opencubes.registry.OCMenus;
import dev.opencubes.registry.OCRecipeSerializers;
import dev.opencubes.registry.OCSounds;
import dev.opencubes.registry.OCStats;
import dev.opencubes.registry.OCTrophyBehaviors;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(OCConstants.MOD_ID)
public final class OpenCubes {

    public OpenCubes(IEventBus modBus, ModContainer container) {
        OCGameRules.bootstrap();
        OCDataComponents.register(modBus);
        OCAttachments.register(modBus);
        OCTrophyBehaviors.register(modBus);
        OCArmorMaterials.register(modBus);
        OCFluids.register(modBus);
        OCBlocks.register(modBus);
        OCItems.register(modBus);
        OCEntities.register(modBus);
        OCBlockEntities.register(modBus);
        OCMenus.register(modBus);
        OCSounds.register(modBus);
        OCRecipeSerializers.register(modBus);
        OCCreativeTabs.register(modBus);
        OCCriteria.register(modBus);
        OCStats.register(modBus);
        OCLootModifiers.register(modBus);

        modBus.addListener(OpenCubes::commonSetup);

        container.registerConfig(ModConfig.Type.COMMON, OCCommonConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, OCClientConfig.SPEC);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(OCStats::bindFormatters);
    }
}
