package dev.opencubes.data;

import dev.opencubes.registry.OCBlocks;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class OCBlockLootProvider extends BlockLootSubProvider {

    public OCBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        Set<Object> woolSlabs = Set.copyOf(OCBlocks.WOOL_SLABS.values());
        OCBlocks.ordered().forEach(block -> {
            if (woolSlabs.contains(block)) {
                // A double slab has to give both halves back.
                add(block.get(), createSlabItemTable(block.get()));
                return;
            }
            if (block == OCBlocks.GRAVE) {
                // Contents spill from the block entity; the grave stone itself never drops.
                add(block.get(), noDrop());
            } else if (block == OCBlocks.TROPHY) {
                // Typed stack comes from TrophyBlock.getDrops / the block entity.
                add(block.get(), noDrop());
            } else if (block == OCBlocks.IMAGINARY) {
                add(block.get(), noDrop());
            } else if (block == OCBlocks.TEMPORARY_SCAFFOLDING) {
                // Disposable by design - breaking it or letting it rot away gives nothing back.
                add(block.get(), noDrop());
            } else {
                dropSelf(block.get());
            }
        });
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return OCBlocks.ordered().stream().map(block -> (Block) block.get()).collect(Collectors.toList());
    }
}
