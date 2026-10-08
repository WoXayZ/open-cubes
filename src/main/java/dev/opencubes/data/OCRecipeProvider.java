package dev.opencubes.data;

import dev.opencubes.OCConstants;
import dev.opencubes.content.button.BigButtonMaterial;
import dev.opencubes.content.flight.PaintedSmithingRecipe;
import dev.opencubes.content.imaginary.ColouredShapelessRecipe;
import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

public class OCRecipeProvider extends RecipeProvider {

    public OCRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        elevators(output);
        woolFamilies(output);
        bigButtons(output);
        ladders(output);
        fan(output);
        bearTrap(output);
        flags(output);
        tank(output);
        xpMachines(output);
        automation(output);
        autoAnvilAndEnchanter(output);
        phase9Items(output);
        painting(output);
        buildingGuides(output);
        heightMaps(output);
        crane(output);
        flightAndVision(output);
        phase15(output);
        tomfoolery(output);
        infoBook(output);
    }

    /** Saves shapeless recipes as {@link ColouredShapelessRecipe}, which passes the colour on. */
    private static RecipeOutput coloured(RecipeOutput output) {
        return new RecipeOutput() {
            @Override
            public Advancement.Builder advancement() {
                return output.advancement();
            }

            @Override
            public void accept(Identifier id, Recipe<?> recipe, @Nullable AdvancementHolder advancement,
                               ICondition... conditions) {
                output.accept(id, new ColouredShapelessRecipe((ShapelessRecipe) recipe), advancement, conditions);
            }
        };
    }

    /** Saves a smithing transform as {@link PaintedSmithingRecipe}, which passes the addition's paint on. */
    private static RecipeOutput painted(RecipeOutput output, Ingredient template, Ingredient base,
                                        Ingredient addition, ItemStack result) {
        return new RecipeOutput() {
            @Override
            public Advancement.Builder advancement() {
                return output.advancement();
            }

            @Override
            public void accept(Identifier id, Recipe<?> recipe, @Nullable AdvancementHolder advancement,
                               ICondition... conditions) {
                output.accept(id, new PaintedSmithingRecipe(template, base, addition, result), advancement, conditions);
            }
        };
    }

    private void infoBook(RecipeOutput output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, OCItems.INFO_BOOK.get())
                .requires(Items.BOOK)
                .requires(Items.CLAY_BALL)
                .unlockedBy("has_book", has(Items.BOOK))
                .save(output);
    }

    private void tomfoolery(RecipeOutput output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, OCItems.TASTY_CLAY.get(), 2)
                .requires(Items.CLAY_BALL)
                .requires(Items.MILK_BUCKET)
                .requires(Items.COCOA_BEANS)
                .unlockedBy("has_clay", has(Items.CLAY_BALL))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, OCItems.EPIC_ERASER.get())
                .requires(Tags.Items.GEMS_LAPIS)
                .requires(Tags.Items.SLIME_BALLS)
                .requires(Items.PINK_WOOL)
                .unlockedBy("has_lapis", has(Tags.Items.GEMS_LAPIS))
                .save(output);

        SpecialRecipeBuilder.special(dev.opencubes.content.tomfoolery.EpicEraserRecipe::new)
                .save(output, "epic_eraser_strip");
    }

    private void phase15(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, OCBlocks.TEMPORARY_SCAFFOLDING.get(), 2)
                .pattern(" s ")
                .pattern("sfs")
                .pattern(" s ")
                .define('s', Tags.Items.RODS_WOODEN)
                .define('f', Blocks.SCAFFOLDING)
                .unlockedBy("has_scaffolding", has(Blocks.SCAFFOLDING))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, OCBlocks.LIQUID_SPONGE.get())
                .pattern(" b ")
                .pattern("bsb")
                .pattern(" b ")
                .define('b', Tags.Items.SLIME_BALLS)
                .define('s', Items.SPONGE)
                .unlockedBy("has_sponge", has(Items.SPONGE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, OCItems.LIQUID_SPONGE_ON_A_STICK.get())
                .pattern("  s")
                .pattern(" t ")
                .define('s', OCBlocks.LIQUID_SPONGE.get())
                .define('t', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_liquid_sponge", has(OCBlocks.LIQUID_SPONGE.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, OCItems.SPONGE_ON_A_STICK.get())
                .pattern("  s")
                .pattern(" t ")
                .define('s', Items.SPONGE)
                .define('t', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_sponge", has(Items.SPONGE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, OCItems.WET_SPONGE_ON_A_STICK.get())
                .pattern("  s")
                .pattern(" t ")
                .define('s', Items.WET_SPONGE)
                .define('t', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_wet_sponge", has(Items.WET_SPONGE))
                .save(output);

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(OCItems.WET_SPONGE_ON_A_STICK.get()),
                        RecipeCategory.TOOLS, OCItems.SPONGE_ON_A_STICK.get(), 0.15F, 200)
                .unlockedBy("has_wet_sponge_on_a_stick", has(OCItems.WET_SPONGE_ON_A_STICK.get()))
                .save(output, OCConstants.id("sponge_on_a_stick_from_smelting"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OCBlocks.HEALER.get())
                .pattern("ggg")
                .pattern("gdg")
                .pattern("ggg")
                .define('g', Tags.Items.INGOTS_GOLD)
                .define('d', Items.GOLDEN_APPLE)
                .unlockedBy("has_golden_apple", has(Items.GOLDEN_APPLE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OCBlocks.SPRINKLER.get())
                .pattern("ccc")
                .pattern(" c ")
                .pattern("crc")
                .define('c', Tags.Items.INGOTS_COPPER)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_copper", has(Tags.Items.INGOTS_COPPER))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, OCBlocks.ARCHERY_TARGET.get())
                .requires(Blocks.TARGET)
                .requires(Items.BOW)
                .unlockedBy("has_target", has(Blocks.TARGET))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, OCBlocks.ITEM_CANNON.get())
                .pattern("ccc")
                .pattern("cpc")
                .pattern("ccc")
                .define('c', Tags.Items.INGOTS_COPPER)
                .define('p', Items.PISTON)
                .unlockedBy("has_piston", has(Items.PISTON))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, OCItems.POINTER.get())
                .pattern(" e ")
                .pattern(" s ")
                .pattern(" s ")
                .define('e', Items.ENDER_EYE)
                .define('s', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OCBlocks.GOLDEN_EGG.get())
                .pattern("ggg")
                .pattern("geg")
                .pattern("ggg")
                .define('g', Tags.Items.INGOTS_GOLD)
                .define('e', Items.EGG)
                .unlockedBy("has_egg", has(Items.EGG))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, OCBlocks.VILLAGE_HIGHLIGHTER.get())
                .pattern("ebe")
                .pattern("bgb")
                .pattern("ebe")
                .define('e', Tags.Items.GEMS_EMERALD)
                .define('b', Tags.Items.BRICKS)
                .define('g', Items.GLOWSTONE)
                .unlockedBy("has_emerald", has(Tags.Items.GEMS_EMERALD))
                .save(output);
    }

    private void flightAndVision(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OCItems.GLIDER_WING_LEFT.get())
                .pattern("dsl")
                .pattern("sll")
                .pattern("lll")
                .define('d', Tags.Items.DYES_PURPLE)
                .define('s', Tags.Items.RODS_WOODEN)
                .define('l', Tags.Items.LEATHERS)
                .unlockedBy("has_leather", has(Tags.Items.LEATHERS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OCItems.GLIDER_WING_RIGHT.get())
                .pattern("lsd")
                .pattern("lls")
                .pattern("lll")
                .define('d', Tags.Items.DYES_PURPLE)
                .define('s', Tags.Items.RODS_WOODEN)
                .define('l', Tags.Items.LEATHERS)
                .unlockedBy("has_leather", has(Tags.Items.LEATHERS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, OCItems.HANG_GLIDER.get())
                .pattern("lsr")
                .define('l', OCItems.GLIDER_WING_LEFT.get())
                .define('r', OCItems.GLIDER_WING_RIGHT.get())
                .define('s', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_glider_wing", has(OCItems.GLIDER_WING_LEFT.get()))
                .save(output);

        // Smithing: membrane (template) + elytra + hang glider → Thermal Elytra, in the glider's colour.
        Ingredient membrane = Ingredient.of(Items.PHANTOM_MEMBRANE);
        Ingredient elytra = Ingredient.of(Items.ELYTRA);
        Ingredient glider = Ingredient.of(OCItems.HANG_GLIDER.get());
        SmithingTransformRecipeBuilder.smithing(membrane, elytra, glider, RecipeCategory.COMBAT,
                        OCItems.THERMAL_ELYTRA.get())
                .unlocks("has_elytra", has(Items.ELYTRA))
                .save(painted(output, membrane, elytra, glider, new ItemStack(OCItems.THERMAL_ELYTRA.get())),
                        OCConstants.id("thermal_elytra_smithing"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, OCItems.SONIC_GLASSES.get())
                .pattern("ihi")
                .pattern("oso")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('h', Items.IRON_HELMET)
                .define('o', Tags.Items.OBSIDIANS)
                .define('s', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_obsidian", has(Tags.Items.OBSIDIANS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, OCItems.CURSOR.get())
                .pattern("w  ")
                .pattern("www")
                .pattern("www")
                .define('w', Items.WHITE_WOOL)
                .unlockedBy("has_wool", has(Items.WHITE_WOOL))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, OCItems.PENCIL.get())
                .requires(ItemTags.COALS)
                .requires(Tags.Items.RODS_WOODEN)
                .requires(Items.ENDER_EYE)
                .requires(Tags.Items.SLIME_BALLS)
                .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, OCItems.PENCIL_GLASSES.get())
                .requires(OCItems.PENCIL.get())
                .requires(Items.PAPER)
                .unlockedBy("has_pencil", has(OCItems.PENCIL.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, OCItems.CRAYON.get())
                .requires(OCItems.PENCIL.get())
                .requires(Tags.Items.DYES)
                .unlockedBy("has_pencil", has(OCItems.PENCIL.get()))
                .save(coloured(output));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, OCItems.CRAYON_GLASSES.get())
                .requires(OCItems.CRAYON.get())
                .requires(Items.PAPER)
                .unlockedBy("has_crayon", has(OCItems.CRAYON.get()))
                .save(coloured(output));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OCBlocks.SKY_BLOCK.get(), 6)
                .pattern("geg")
                .pattern("gsg")
                .pattern("geg")
                .define('g', Tags.Items.GLASS_BLOCKS_COLORLESS)
                .define('e', Items.ENDER_EYE)
                .define('s', Items.END_STONE)
                .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, OCBlocks.INVERTED_SKY_BLOCK.get())
                .requires(OCBlocks.SKY_BLOCK.get())
                .requires(Items.REDSTONE_TORCH)
                .unlockedBy("has_sky_block", has(OCBlocks.SKY_BLOCK.get()))
                .save(output);
    }

    private void crane(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OCItems.BEAM.get(), 2)
                .pattern("ibi")
                .pattern("yby")
                .pattern("ibi")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('b', Items.IRON_BARS)
                .define('y', Tags.Items.DYES_YELLOW)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OCItems.LINE.get(), 2)
                .pattern(" s ")
                .pattern("sxs")
                .pattern(" s ")
                .define('s', Tags.Items.STRINGS)
                .define('x', Tags.Items.SLIME_BALLS)
                .unlockedBy("has_slime", has(Tags.Items.SLIME_BALLS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OCItems.CRANE_ENGINE.get())
                .pattern("iii")
                .pattern("isi")
                .pattern("iri")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('s', Tags.Items.RODS_WOODEN)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OCItems.CRANE_MAGNET.get())
                .pattern("iyi")
                .pattern("iri")
                .pattern("ibi")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('y', Tags.Items.DYES_YELLOW)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('b', Tags.Items.DYES_BLACK)
                .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, OCItems.CRANE_BACKPACK.get())
                .requires(OCItems.CRANE_ENGINE.get())
                .requires(OCItems.CRANE_MAGNET.get())
                .requires(OCItems.BEAM.get())
                .requires(OCItems.BEAM.get())
                .requires(OCItems.LINE.get())
                .requires(OCItems.LINE.get())
                .requires(OCItems.LINE.get())
                .requires(Tags.Items.LEATHERS)
                .unlockedBy("has_crane_engine", has(OCItems.CRANE_ENGINE.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, OCItems.CRANE_CONTROL.get())
                .pattern("ili")
                .pattern("grg")
                .pattern("iri")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('l', Tags.Items.NUGGETS_GOLD)
                .define('g', Tags.Items.DUSTS_GLOWSTONE)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_crane_backpack", has(OCItems.CRANE_BACKPACK.get()))
                .save(output);
    }

    private void heightMaps(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OCItems.EMPTY_MAP.get())
                .pattern("grg")
                .pattern("rmr")
                .pattern("grg")
                .define('g', Tags.Items.NUGGETS_GOLD)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('m', Items.MAP)
                .unlockedBy("has_map", has(Items.MAP))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OCItems.CARTOGRAPHER.get())
                .pattern("iri")
                .pattern("rer")
                .pattern("iri")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('e', Items.ENDER_EYE)
                .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OCBlocks.HEIGHT_MAP_PROJECTOR.get())
                .pattern("grl")
                .pattern("iri")
                .pattern("srs")
                .define('g', Tags.Items.DUSTS_GLOWSTONE)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('l', Tags.Items.GEMS_LAPIS)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('s', ItemTags.SLABS)
                .unlockedBy("has_lapis", has(Tags.Items.GEMS_LAPIS))
                .save(output);

        SpecialRecipeBuilder.special(dev.opencubes.content.heightmap.MapCloneRecipe::new)
                .save(output, "map_clone");
        SpecialRecipeBuilder.special(dev.opencubes.content.heightmap.MapResizeRecipe::new)
                .save(output, "map_resize");
    }

    private void buildingGuides(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OCBlocks.BUILDING_GUIDE.get())
                .pattern("grg")
                .pattern("gtg")
                .pattern("grg")
                .define('g', Tags.Items.GLASS_BLOCKS)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('t', Items.TORCH)
                .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OCBlocks.ENHANCED_BUILDING_GUIDE.get())
                .pattern(" e ")
                .pattern("ege")
                .pattern(" e ")
                .define('g', OCBlocks.BUILDING_GUIDE.get())
                .define('e', Tags.Items.ENDER_PEARLS)
                .unlockedBy("has_building_guide", has(OCBlocks.BUILDING_GUIDE.get()))
                .save(output);
    }

    private void painting(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OCBlocks.CANVAS.get(), 9)
                .pattern("ppp")
                .pattern("pfp")
                .pattern("ppp")
                .define('p', Items.PAPER)
                .define('f', Tags.Items.STRINGS)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OCBlocks.GLASS_CANVAS.get())
                .pattern("ggg")
                .pattern("gcg")
                .pattern("ggg")
                .define('g', Tags.Items.GLASS_BLOCKS)
                .define('c', OCBlocks.CANVAS.get())
                .unlockedBy("has_canvas", has(OCBlocks.CANVAS.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OCBlocks.PAINT_MIXER.get())
                .pattern("oio")
                .pattern("ibi")
                .pattern("ooo")
                .define('o', Tags.Items.OBSIDIANS)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('b', OCItems.PAINT_BRUSH.get())
                .unlockedBy("has_paint_brush", has(OCItems.PAINT_BRUSH.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OCBlocks.DRAWING_TABLE.get())
                .pattern("sss")
                .pattern("pcp")
                .pattern("ppp")
                .define('s', Tags.Items.RODS_WOODEN)
                .define('p', ItemTags.PLANKS)
                .define('c', Blocks.CRAFTING_TABLE)
                .unlockedBy("has_crafting_table", has(Blocks.CRAFTING_TABLE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, OCItems.PAINT_BRUSH.get())
                .pattern(" w")
                .pattern("s ")
                .define('w', Items.WHITE_WOOL)
                .define('s', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_wool", has(Items.WHITE_WOOL))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, OCItems.SQUEEGEE.get())
                .pattern("sss")
                .pattern(" t ")
                .pattern(" t ")
                .define('s', Items.SPONGE)
                .define('t', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_sponge", has(Items.SPONGE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OCItems.UNPREPARED_STENCIL.get())
                .pattern("ppp")
                .pattern("pip")
                .pattern("ppp")
                .define('p', Items.PAPER)
                .define('i', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_iron_ingot", has(Tags.Items.INGOTS_IRON))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OCItems.SKETCHING_PENCIL.get())
                .pattern("c")
                .pattern("s")
                .pattern("s")
                .define('c', ItemTags.COALS)
                .define('s', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_coal", has(ItemTags.COALS))
                .save(output);
    }

    private void phase9Items(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, OCItems.WRENCH.get())
                .pattern(" r ")
                .pattern(" cb")
                .pattern("s  ")
                .define('r', Tags.Items.DYES_RED)
                .define('b', Tags.Items.DYES_BLUE)
                .define('c', Tags.Items.INGOTS_COPPER)
                .define('s', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_copper_ingot", has(Tags.Items.INGOTS_COPPER))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, OCItems.SLIMALYZER.get())
                .pattern("cgc")
                .pattern("csc")
                .pattern("crc")
                .define('c', Tags.Items.INGOTS_COPPER)
                .define('g', Tags.Items.GLASS_PANES)
                .define('s', Tags.Items.SLIME_BALLS)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_slime_ball", has(Tags.Items.SLIME_BALLS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, OCItems.PEDOMETER.get())
                .pattern("iii")
                .pattern("rcr")
                .pattern("iii")
                .define('i', Tags.Items.INGOTS_COPPER)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('c', Items.CLOCK)
                .unlockedBy("has_clock", has(Items.CLOCK))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, OCItems.DEV_NULL.get())
                .requires(Tags.Items.COBBLESTONES)
                .requires(Items.APPLE)
                .unlockedBy("has_apple", has(Items.APPLE))
                .save(output);

        for (DyeColor colour : DyeColor.values()) {
            Block slab = OCBlocks.WOOL_SLABS.get(colour).get();
            ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, OCItems.SLEEPING_BAGS.get(colour).get())
                    .pattern("sss")
                    .define('s', slab)
                    .group("opencubes:sleeping_bag")
                    .unlockedBy("has_wool_slab", has(slab))
                    .save(output);
        }

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, OCItems.GOLDEN_EYE.get())
                .pattern("ggg")
                .pattern("geg")
                .pattern("ggg")
                .define('g', Tags.Items.NUGGETS_GOLD)
                .define('e', Items.ENDER_EYE)
                .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, OCItems.LUGGAGE.get())
                .pattern("sds")
                .pattern("scs")
                .pattern("sss")
                .define('s', Tags.Items.RODS_WOODEN)
                .define('d', Tags.Items.GEMS_DIAMOND)
                .define('c', Tags.Items.CHESTS_WOODEN)
                .unlockedBy("has_diamond", has(Tags.Items.GEMS_DIAMOND))
                .save(output);

        SpecialRecipeBuilder.special(dev.opencubes.content.goldeneye.GoldenEyeRechargeRecipe::new)
                .save(output, "opencubes:golden_eye_recharge");
    }

    private void tank(RecipeOutput output) {
        // Two tanks from glass panes and obsidian, as the original.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OCBlocks.TANK.get(), 2)
                .pattern("ogo")
                .pattern("ggg")
                .pattern("ogo")
                .define('g', Tags.Items.GLASS_PANES)
                .define('o', Tags.Items.OBSIDIANS)
                .unlockedBy("has_obsidian", has(Tags.Items.OBSIDIANS))
                .save(output);
    }

    private void xpMachines(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OCBlocks.XP_DRAIN.get())
                .pattern("iii")
                .pattern("iii")
                .pattern("iii")
                .define('i', Items.IRON_BARS)
                .unlockedBy("has_iron_bars", has(Items.IRON_BARS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OCBlocks.XP_SHOWER.get())
                .pattern("iii")
                .pattern("  o")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('o', Tags.Items.OBSIDIANS)
                .unlockedBy("has_obsidian", has(Tags.Items.OBSIDIANS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OCBlocks.XP_BOTTLER.get())
                .pattern("iii")
                .pattern("ibi")
                .pattern("iii")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('b', Items.GLASS_BOTTLE)
                .unlockedBy("has_glass_bottle", has(Items.GLASS_BOTTLE))
                .save(output);
    }

    private void automation(RecipeOutput output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, OCBlocks.VACUUM_HOPPER.get())
                .requires(Blocks.HOPPER)
                .requires(Tags.Items.OBSIDIANS)
                .requires(Items.ENDER_EYE)
                .unlockedBy("has_hopper", has(Blocks.HOPPER))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, OCBlocks.ITEM_DROPPER.get())
                .pattern(" g ")
                .pattern("gdg")
                .pattern(" g ")
                .define('g', Tags.Items.INGOTS_GOLD)
                .define('d', Blocks.DROPPER)
                .unlockedBy("has_dropper", has(Blocks.DROPPER))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, OCBlocks.BLOCK_BREAKER.get())
                .pattern("icc")
                .pattern("src")
                .pattern("icc")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('c', Tags.Items.COBBLESTONES)
                .define('s', Items.DIAMOND_PICKAXE)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_diamond_pickaxe", has(Items.DIAMOND_PICKAXE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, OCBlocks.BLOCK_PLACER.get())
                .pattern("icc")
                .pattern("src")
                .pattern("icc")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('c', Tags.Items.COBBLESTONES)
                .define('s', Blocks.PISTON)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_piston", has(Blocks.PISTON))
                .save(output);
    }

    private void autoAnvilAndEnchanter(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OCBlocks.AUTO_ANVIL.get())
                .pattern("iii")
                .pattern("iai")
                .pattern("rrr")
                .define('i', Tags.Items.GEMS_DIAMOND)
                .define('a', Blocks.ANVIL)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_anvil", has(Blocks.ANVIL))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OCBlocks.AUTO_ENCHANTMENT_TABLE.get())
                .pattern("iii")
                .pattern("iei")
                .pattern("rrr")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('e', Blocks.ENCHANTING_TABLE)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_enchanting_table", has(Blocks.ENCHANTING_TABLE))
                .save(output);
    }

    private void elevators(RecipeOutput output) {
        Ingredient anyElevator = Ingredient.of(OCBlocks.ELEVATORS.values().stream()
                .map(block -> new net.minecraft.world.item.ItemStack(block.get())));
        Ingredient anyRotatingElevator = Ingredient.of(OCBlocks.ROTATING_ELEVATORS.values().stream()
                .map(block -> new net.minecraft.world.item.ItemStack(block.get())));

        for (DyeColor colour : DyeColor.values()) {
            Block wool = woolFor(colour);

            ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, OCBlocks.ELEVATORS.get(colour).get())
                    .pattern("www")
                    .pattern("wew")
                    .pattern("www")
                    .define('w', wool)
                    .define('e', Tags.Items.ENDER_PEARLS)
                    .group("opencubes:elevator")
                    .unlockedBy("has_ender_pearl", has(Tags.Items.ENDER_PEARLS))
                    .save(output);

            ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, OCBlocks.ROTATING_ELEVATORS.get(colour).get())
                    .pattern("wiw")
                    .pattern("wew")
                    .pattern("wiw")
                    .define('w', wool)
                    .define('i', Tags.Items.INGOTS_IRON)
                    .define('e', Tags.Items.ENDER_PEARLS)
                    .group("opencubes:rotating_elevator")
                    .unlockedBy("has_ender_pearl", has(Tags.Items.ENDER_PEARLS))
                    .save(output);

            // Recolour in the crafting grid: any elevator of the same kind plus the matching dye.
            ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION, OCBlocks.ELEVATORS.get(colour).get())
                    .requires(anyElevator)
                    .requires(dyeFor(colour))
                    .group("opencubes:elevator")
                    .unlockedBy("has_elevator", has(OCBlocks.ELEVATORS.get(colour).get()))
                    .save(output, OCConstants.id(colour.getName() + "_elevator_dye"));

            ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION,
                            OCBlocks.ROTATING_ELEVATORS.get(colour).get())
                    .requires(anyRotatingElevator)
                    .requires(dyeFor(colour))
                    .group("opencubes:rotating_elevator")
                    .unlockedBy("has_rotating_elevator", has(OCBlocks.ROTATING_ELEVATORS.get(colour).get()))
                    .save(output, OCConstants.id(colour.getName() + "_rotating_elevator_dye"));
        }
    }

    private void woolFamilies(RecipeOutput output) {
        for (DyeColor colour : DyeColor.values()) {
            Block wool = woolFor(colour);
            Block slab = OCBlocks.WOOL_SLABS.get(colour).get();
            Block stairs = OCBlocks.WOOL_STAIRS.get(colour).get();

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, slab, 6)
                    .pattern("www")
                    .define('w', wool)
                    .group("opencubes:wool_slab")
                    .unlockedBy("has_wool", has(wool))
                    .save(output);

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, stairs, 4)
                    .pattern("w  ")
                    .pattern("ww ")
                    .pattern("www")
                    .define('w', wool)
                    .group("opencubes:wool_stairs")
                    .unlockedBy("has_wool", has(wool))
                    .save(output);

            // Two slabs back into a full block, like vanilla stone families allow.
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, wool)
                    .pattern("s")
                    .pattern("s")
                    .define('s', slab)
                    .group("opencubes:wool_from_slabs")
                    .unlockedBy("has_wool_slab", has(slab))
                    .save(output, OCConstants.id(colour.getName() + "_wool_from_slabs"));
        }
    }

    private void bigButtons(RecipeOutput output) {
        for (BigButtonMaterial material : BigButtonMaterial.ALL) {
            Block button = material.vanillaButton();
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, OCBlocks.BIG_BUTTONS.get(material).get())
                    .pattern("bb")
                    .pattern("bb")
                    .define('b', button)
                    .group("opencubes:big_button")
                    .unlockedBy("has_button", has(button))
                    .save(output);
        }
    }

    private void ladders(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OCBlocks.ROPE_LADDER.get(), 3)
                .pattern("sls")
                .pattern("sls")
                .pattern("sls")
                .define('s', Tags.Items.STRINGS)
                .define('l', Items.LADDER)
                .unlockedBy("has_ladder", has(Items.LADDER))
                .save(output);
    }

    private void fan(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, OCBlocks.FAN.get())
                .pattern("f")
                .pattern("i")
                .pattern("s")
                .define('f', Items.IRON_BARS)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('s', Blocks.SMOOTH_STONE_SLAB)
                .unlockedBy("has_iron_bars", has(Items.IRON_BARS))
                .save(output);
    }

    private void bearTrap(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, OCBlocks.BEAR_TRAP.get())
                .pattern("fif")
                .pattern("fif")
                .pattern("fif")
                .define('f', Items.IRON_BARS)
                .define('i', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_iron_bars", has(Items.IRON_BARS))
                .save(output);
    }

    private void flags(RecipeOutput output) {
        for (DyeColor colour : DyeColor.values()) {
            Block carpet = BuiltInRegistries.BLOCK.get(
                    Identifier.withDefaultNamespace(colour.getName() + "_carpet"));
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OCBlocks.FLAGS.get(colour).get())
                    .pattern("scc")
                    .pattern("sc ")
                    .pattern("s  ")
                    .define('c', carpet)
                    .define('s', Tags.Items.RODS_WOODEN)
                    .group("opencubes:flag")
                    .unlockedBy("has_carpet", has(carpet))
                    .save(output);
        }
    }

    private static net.minecraft.world.item.Item dyeFor(DyeColor colour) {
        return net.minecraft.world.item.DyeItem.byColor(colour);
    }

    private static Block woolFor(DyeColor colour) {
        return BuiltInRegistries.BLOCK.get(Identifier.withDefaultNamespace(colour.getName() + "_wool"));
    }
}
