package dev.opencubes.data;

import dev.opencubes.OCConstants;
import java.util.List;
import java.util.Set;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class OCDataGenerators {

    private OCDataGenerators() {}

    @SubscribeEvent
    public static void gather(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        var registries = event.getLookupProvider();
        var helper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new OCBlockStateProvider(output, helper));
        generator.addProvider(event.includeClient(), new OCLanguageProvider(output));
        generator.addProvider(event.includeClient(), new OCFrenchLanguageProvider(output));
        generator.addProvider(event.includeClient(), new OCSoundProvider(output, helper));

        generator.addProvider(event.includeServer(), new OCRecipeProvider(output, registries));
        OCBlockTagsProvider blockTags = new OCBlockTagsProvider(output, registries, helper);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(),
                new OCItemTagsProvider(output, registries, blockTags.contentsGetter(), helper));
        generator.addProvider(event.includeServer(), new OCFluidTagsProvider(output, registries, helper));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(OCBlockLootProvider::new, LootContextParamSets.BLOCK)),
                registries));
    }
}
