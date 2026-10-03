package dev.opencubes.data;

import dev.opencubes.OCConstants;
import dev.opencubes.content.button.BigButtonMaterial;
import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class OCBlockTagsProvider extends BlockTagsProvider {

    public OCBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                               @Nullable ExistingFileHelper helper) {
        super(output, registries, OCConstants.MOD_ID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        elevators();
        bigButtons();
        ladders();
        fanAndTrap();
        flags();
        tank();
        buildingGuides();
        elevatorRules();
        miniMeBreakable();
        woolFamilies();
        machines();
    }

    private void machines() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                OCBlocks.XP_DRAIN.get(),
                OCBlocks.XP_SHOWER.get(),
                OCBlocks.XP_BOTTLER.get(),
                OCBlocks.VACUUM_HOPPER.get(),
                OCBlocks.ITEM_DROPPER.get(),
                OCBlocks.BLOCK_BREAKER.get(),
                OCBlocks.BLOCK_PLACER.get(),
                OCBlocks.AUTO_ANVIL.get(),
                OCBlocks.AUTO_ENCHANTMENT_TABLE.get(),
                OCBlocks.GRAVE.get(),
                OCBlocks.TROPHY.get(),
                OCBlocks.PAINT_CAN.get(),
                OCBlocks.PAINT_MIXER.get(),
                OCBlocks.SKY_BLOCK.get(),
                OCBlocks.INVERTED_SKY_BLOCK.get(),
                OCBlocks.HEALER.get(),
                OCBlocks.SPRINKLER.get(),
                OCBlocks.ITEM_CANNON.get(),
                OCBlocks.GOLDEN_EGG.get(),
                OCBlocks.VILLAGE_HIGHLIGHTER.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(
                OCBlocks.DRAWING_TABLE.get(),
                OCBlocks.ARCHERY_TARGET.get(),
                OCBlocks.TEMPORARY_SCAFFOLDING.get());
        tag(BlockTags.MINEABLE_WITH_HOE).add(OCBlocks.LIQUID_SPONGE.get());
    }

    private void buildingGuides() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                OCBlocks.BUILDING_GUIDE.get(),
                OCBlocks.ENHANCED_BUILDING_GUIDE.get(),
                OCBlocks.HEIGHT_MAP_PROJECTOR.get());
    }

    private void tank() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(OCBlocks.TANK.get());
        tag(BlockTags.IMPERMEABLE).add(OCBlocks.TANK.get());
    }

    private void elevators() {
        for (DyeColor colour : DyeColor.values()) {
            Block plain = OCBlocks.ELEVATORS.get(colour).get();
            Block rotating = OCBlocks.ROTATING_ELEVATORS.get(colour).get();
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(plain, rotating);
            tag(BlockTags.NEEDS_STONE_TOOL).add(plain, rotating);
        }
    }

    private void bigButtons() {
        for (BigButtonMaterial material : BigButtonMaterial.ALL) {
            Block button = OCBlocks.BIG_BUTTONS.get(material).get();
            tag(BlockTags.BUTTONS).add(button);
            if (material.isWooden()) {
                tag(BlockTags.WOODEN_BUTTONS).add(button);
                tag(BlockTags.MINEABLE_WITH_AXE).add(button);
            } else {
                tag(BlockTags.MINEABLE_WITH_PICKAXE).add(button);
            }
        }
    }

    private void ladders() {
        tag(BlockTags.CLIMBABLE).add(OCBlocks.ROPE_LADDER.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(OCBlocks.ROPE_LADDER.get());
    }

    private void woolFamilies() {
        for (DyeColor colour : DyeColor.values()) {
            Block slab = OCBlocks.WOOL_SLABS.get(colour).get();
            Block stairs = OCBlocks.WOOL_STAIRS.get(colour).get();
            tag(BlockTags.SLABS).add(slab);
            tag(BlockTags.STAIRS).add(stairs);
        }
    }

    private void fanAndTrap() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(OCBlocks.FAN.get(), OCBlocks.BEAR_TRAP.get());
    }

    private void flags() {
        for (DyeColor colour : DyeColor.values()) {
            tag(BlockTags.MINEABLE_WITH_AXE).add(OCBlocks.FLAGS.get(colour).get());
        }
    }

    private void elevatorRules() {
        tag(OCTags.Blocks.ELEVATOR_TRANSPARENT)
                .addTag(BlockTags.SLABS)
                .addTag(BlockTags.TRAPDOORS)
                .addTag(BlockTags.WOOL_CARPETS)
                .addTag(BlockTags.CLIMBABLE)
                .add(Blocks.SNOW);

        tag(OCTags.Blocks.ELEVATOR_BLOCKING)
                .add(Blocks.BEDROCK)
                .add(Blocks.BARRIER);

        // Intentionally empty: packs add blocks that must count even when also transparent.
        tag(OCTags.Blocks.ELEVATOR_INCREMENT);
    }

    private void miniMeBreakable() {
        tag(OCTags.Blocks.MINI_ME_BREAKABLE)
                .addTag(BlockTags.SMALL_FLOWERS)
                .addTag(BlockTags.CROPS)
                .add(Blocks.TORCH, Blocks.WALL_TORCH, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH)
                .add(Blocks.SHORT_GRASS, Blocks.FERN, Blocks.DEAD_BUSH, Blocks.TALL_GRASS, Blocks.LARGE_FERN)
                .add(Blocks.SUGAR_CANE, Blocks.BAMBOO, Blocks.BAMBOO_SAPLING);
    }
}
