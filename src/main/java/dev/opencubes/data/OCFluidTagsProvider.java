package dev.opencubes.data;

import dev.opencubes.OCConstants;
import dev.opencubes.registry.OCFluids;
import dev.opencubes.registry.OCTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class OCFluidTagsProvider extends FluidTagsProvider {

    public OCFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                               @Nullable ExistingFileHelper helper) {
        super(output, registries, OCConstants.MOD_ID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(OCTags.Fluids.EXPERIENCE).add(OCFluids.XP_JUICE.get(), OCFluids.FLOWING_XP_JUICE.get());
    }
}
