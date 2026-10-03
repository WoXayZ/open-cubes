package dev.opencubes.data;

import dev.opencubes.OCConstants;
import dev.opencubes.registry.OCItems;
import dev.opencubes.registry.OCTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

/**
 * Item tags that mirror the fluid tags, so recipe viewers and other mods can find the XP bucket
 * from the {@code c:experience} fluid it carries.
 */
public class OCItemTagsProvider extends ItemTagsProvider {

    public OCItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                              CompletableFuture<TagsProvider.TagLookup<Block>> blockTags,
                              @Nullable ExistingFileHelper helper) {
        super(output, registries, blockTags, OCConstants.MOD_ID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(OCTags.Items.BUCKETS).add(OCItems.XP_BUCKET.get());
        tag(OCTags.Items.BUCKETS_EXPERIENCE).add(OCItems.XP_BUCKET.get());
    }
}
