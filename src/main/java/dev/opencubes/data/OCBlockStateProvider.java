package dev.opencubes.data;

import dev.opencubes.OCConstants;
import dev.opencubes.content.button.BigButtonMaterial;
import dev.opencubes.content.flag.FlagBlock;
import dev.opencubes.content.grave.GraveBlock;
import dev.opencubes.content.target.ArcheryTargetBlock;
import dev.opencubes.content.trap.BearTrapBlock;
import dev.opencubes.content.village.VillageHighlighterBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import dev.opencubes.registry.OCBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class OCBlockStateProvider extends BlockStateProvider {

    private static final String CUTOUT = "minecraft:cutout";
    private static final String TRANSLUCENT = "minecraft:translucent";

    public OCBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, OCConstants.MOD_ID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        elevators();
        bigButtons();
        woolSlabsAndStairs();
        ropeLadder();
        fan();
        bearTrap();
        flags();
        tank();
        xpMachines();
        automation();
        autoAnvilAndEnchanter();
        grave();
        trophy();
        painting();
        buildingGuides();
        heightMapProjector();
        imaginaryAndSky();
        phase15();
    }

    private void phase15() {
        temporaryScaffolding();

        // Registered as liquid_sponge but the only texture in the pack is block/sponge.
        ModelFile sponge = models().cubeAll("block/liquid_sponge", modLoc("block/sponge"));
        simpleBlock(OCBlocks.LIQUID_SPONGE.get(), sponge);
        itemModels().withExistingParent("liquid_sponge", sponge.getLocation());

        simpleBlock(OCBlocks.HEALER.get());
        itemModels().withExistingParent("healer", modLoc("block/healer"));

        ModelFile sprinkler = models().getExistingFile(modLoc("block/sprinkler"));
        horizontalBlock(OCBlocks.SPRINKLER.get(), sprinkler);
        itemModels().withExistingParent("sprinkler", sprinkler.getLocation());

        archeryTarget();

        ModelFile cannon = models().getExistingFile(modLoc("block/item_cannon"));
        horizontalBlock(OCBlocks.ITEM_CANNON.get(), cannon);
        itemModels().withExistingParent("item_cannon", cannon.getLocation());

        ModelFile egg = models()
                .withExistingParent("block/golden_egg", mcLoc("block/dragon_egg"))
                .texture("all", modLoc("block/golden_egg"))
                .texture("particle", modLoc("block/golden_egg"));
        simpleBlock(OCBlocks.GOLDEN_EGG.get(), egg);
        itemModels().withExistingParent("golden_egg", egg.getLocation());

        villageHighlighter();
    }

    /**
     * Small house model adapted from OpenBlocks, front door to the north. Powered swaps the side
     * and back textures to their lit variants; front/top/bottom stay the same either way.
     */
    private void villageHighlighter() {
        ModelFile inactive = models().getExistingFile(modLoc("block/village"));
        ModelFile active = models().getExistingFile(modLoc("block/village_active"));
        getVariantBuilder(OCBlocks.VILLAGE_HIGHLIGHTER.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(VillageHighlighterBlock.POWERED) ? active : inactive)
                .rotationY(((int) state.getValue(VillageHighlighterBlock.FACING).toYRot() + 180) % 360)
                .build());
        itemModels().withExistingParent("village_highlighter", inactive.getLocation());
    }

    /**
     * Vanilla scaffolding: a stable variant standing on something and an unstable one hanging in
     * the air, keyed on BOTTOM. The property is looked up rather than assumed so this still
     * generates something usable if the block ever stops extending {@code ScaffoldingBlock}.
     */
    private void temporaryScaffolding() {
        Block block = OCBlocks.TEMPORARY_SCAFFOLDING.get();
        ModelFile stable = models().getExistingFile(modLoc("block/temporary_scaffolding"));
        ModelFile unstable = models().getExistingFile(modLoc("block/temporary_scaffolding_unstable"));

        if (block.defaultBlockState().hasProperty(BlockStateProperties.BOTTOM)) {
            getVariantBuilder(block).forAllStatesExcept(state -> ConfiguredModel.builder()
                            .modelFile(state.getValue(BlockStateProperties.BOTTOM) ? unstable : stable)
                            .build(),
                    BlockStateProperties.STABILITY_DISTANCE, BlockStateProperties.WATERLOGGED);
        } else {
            simpleBlock(block, stable);
        }
        itemModels().withExistingParent("temporary_scaffolding", stable.getLocation());
    }

    /**
     * Flat against the attach face until powered, then upright for archery. Floor/ceiling use the
     * slab model when folded; walls stay as a flush board (same upright mesh) until powered.
     */
    private void archeryTarget() {
        ModelFile flat = models().getExistingFile(modLoc("block/archery_target_flat"));
        ModelFile upright = models().getExistingFile(modLoc("block/archery_target_upright"));
        getVariantBuilder(OCBlocks.ARCHERY_TARGET.get()).forAllStates(state -> {
            AttachFace face = state.getValue(ArcheryTargetBlock.FACE);
            Direction facing = state.getValue(ArcheryTargetBlock.FACING);
            boolean powered = state.getValue(ArcheryTargetBlock.POWERED);

            int yRot = ((int) facing.toYRot() + 180) % 360;
            int xRot = 0;
            ModelFile model;

            if (face == AttachFace.WALL && !powered) {
                // Folded flat against the wall (slab tipped up), same attachment trick as big buttons.
                model = flat;
                xRot = 90;
            } else if (face != AttachFace.WALL && !powered) {
                model = flat;
                if (face == AttachFace.CEILING) {
                    xRot = 180;
                    yRot = (yRot + 180) % 360;
                }
            } else {
                model = upright;
            }

            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(xRot)
                    .rotationY(yRot)
                    .uvLock(face == AttachFace.WALL && !powered)
                    .build();
        });
        itemModels().withExistingParent("archery_target", upright.getLocation());
    }

    private void imaginaryAndSky() {
        ModelFile imaginary = models().getBuilder("block/imaginary_block")
                .texture("particle", modLoc("block/pencil_block"));
        getVariantBuilder(OCBlocks.IMAGINARY.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(imaginary).build());
        // Item models for pencil/crayon are handwritten under models/item.

        ModelFile sky = models().cubeAll("block/sky_block", modLoc("block/sky_inactive"))
                .renderType(CUTOUT);
        getVariantBuilder(OCBlocks.SKY_BLOCK.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(sky).build());
        itemModels().withExistingParent("sky_block", sky.getLocation());

        ModelFile invertedSky = models().getExistingFile(modLoc("block/sky_block_inverted"));
        getVariantBuilder(OCBlocks.INVERTED_SKY_BLOCK.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(invertedSky).build());
        itemModels().withExistingParent("inverted_sky_block", invertedSky.getLocation());
    }

    private void heightMapProjector() {
        var side = modLoc("block/height_map_projector_side");
        var bottom = mcLoc("block/furnace_top");
        ModelFile inactive = models().slab("block/height_map_projector",
                side,
                bottom,
                modLoc("block/height_map_projector"));
        ModelFile active = models().slab("block/height_map_projector_active",
                side,
                bottom,
                modLoc("block/height_map_projector_active"));
        getVariantBuilder(OCBlocks.HEIGHT_MAP_PROJECTOR.get())
                .partialState().with(dev.opencubes.content.heightmap.HeightMapProjectorBlock.ACTIVE, false)
                .modelForState().modelFile(inactive).addModel()
                .partialState().with(dev.opencubes.content.heightmap.HeightMapProjectorBlock.ACTIVE, true)
                .modelForState().modelFile(active).addModel();
        itemModels().withExistingParent("height_map_projector", inactive.getLocation());
    }

    private void buildingGuides() {
        ModelFile guide = models().cubeAll("block/building_guide", modLoc("block/building_guide"))
                .renderType(TRANSLUCENT);
        horizontalBlock(OCBlocks.BUILDING_GUIDE.get(), guide);
        itemModels().withExistingParent("building_guide", guide.getLocation());

        // The enhanced texture is fully covered but partly see-through, so it needs blending.
        ModelFile enhanced = models().cubeAll("block/enhanced_building_guide", modLoc("block/enhanced_building_guide"))
                .renderType(TRANSLUCENT);
        horizontalBlock(OCBlocks.ENHANCED_BUILDING_GUIDE.get(), enhanced);
        itemModels().withExistingParent("enhanced_building_guide", enhanced.getLocation());
    }

    private void trophy() {
        ModelFile model = models().getExistingFile(modLoc("block/trophy_base"));
        getVariantBuilder(OCBlocks.TROPHY.get()).forAllStates(state -> {
            Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY(((int) facing.toYRot() + 180) % 360)
                    .build();
        });
        // Item model (builtin/entity + displays for BEWLR) is hand-authored under main resources.
    }

    private void painting() {
        simpleBlock(OCBlocks.CANVAS.get(), models().cubeAll("block/canvas", modLoc("block/canvas")));
        itemModels().withExistingParent("canvas", modLoc("block/canvas"));

        ModelFile glass = models().cubeAll("block/glass_canvas", modLoc("block/glass_canvas"))
                .renderType(TRANSLUCENT);
        simpleBlock(OCBlocks.GLASS_CANVAS.get(), glass);
        itemModels().withExistingParent("glass_canvas", glass.getLocation());

        ModelFile paintCan = models().getExistingFile(modLoc("block/paint_can"));
        horizontalBlock(OCBlocks.PAINT_CAN.get(), paintCan);
        itemModels().withExistingParent("paint_can", paintCan.getLocation());

        // Front + tinted overlay (paint_mixer_front_overlay) driven by the BE target colour.
        ModelFile mixer = models().getExistingFile(modLoc("block/paint_mixer"));
        horizontalBlock(OCBlocks.PAINT_MIXER.get(), mixer);
        itemModels().withExistingParent("paint_mixer", mixer.getLocation());

        // block/cube leaves "particle" unset, which is what gave the drawing table
        // missing-texture break particles.
        ModelFile table = models().cube(
                        "block/drawing_table",
                        modLoc("block/drawing_table"),
                        modLoc("block/drawing_table_top"),
                        modLoc("block/drawing_table_front"),
                        modLoc("block/drawing_table"),
                        modLoc("block/drawing_table"),
                        modLoc("block/drawing_table"))
                .texture("particle", modLoc("block/drawing_table"));
        horizontalBlock(OCBlocks.DRAWING_TABLE.get(), table);
        itemModels().withExistingParent("drawing_table", table.getLocation());
    }

    private void tank() {
        ModelFile model = models().getExistingFile(modLoc("block/tank"));
        simpleBlock(OCBlocks.TANK.get(), model);
        // Item uses a non-CTM inventory texture (hand-authored models/item/tank.json).
    }

    private void xpMachines() {
        ModelFile drain = models().getExistingFile(modLoc("block/xp_drain"));
        simpleBlock(OCBlocks.XP_DRAIN.get(), drain);
        itemModels().withExistingParent("xp_drain", drain.getLocation());

        ModelFile shower = models().getExistingFile(modLoc("block/xp_shower"));
        // Default model attaches to the north wall and points south.
        getVariantBuilder(OCBlocks.XP_SHOWER.get()).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(shower)
                .rotationY((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot())
                .build(), BlockStateProperties.POWERED);
        itemModels().withExistingParent("xp_shower", shower.getLocation());

        // Horizontal dropper-style body; front texture is authored separately (no vertical).
        // Furnace-like LIT toggle swaps the front texture between off/on, side and top stay put.
        ModelFile bottlerOff = models().orientable("block/xp_bottler",
                mcLoc("block/furnace_side"),
                modLoc("block/xp_bottler_front"),
                mcLoc("block/furnace_top"));
        ModelFile bottlerOn = models().orientable("block/xp_bottler_on",
                mcLoc("block/furnace_side"),
                modLoc("block/xp_bottler_front_on"),
                mcLoc("block/furnace_top"));
        getVariantBuilder(OCBlocks.XP_BOTTLER.get()).forAllStates(state -> {
            Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
            boolean lit = state.getValue(BlockStateProperties.LIT);
            return ConfiguredModel.builder()
                    .modelFile(lit ? bottlerOn : bottlerOff)
                    .rotationY(((int) facing.toYRot() + 180) % 360)
                    .build();
        });
        itemModels().withExistingParent("xp_bottler", bottlerOff.getLocation());
    }

    /**
     * Advanced dropper, breaker and placer follow the vanilla dropper layout: horizontal
     * {@code orientable} (furnace top/side + front) and vertical {@code orientable_vertical}.
     */
    private void automation() {
        // Hand-authored multipart block + inventory model (body with preview ports).
        // Do not overwrite models/item/vacuum_hopper.json from datagen.

        ResourceLocation furnaceSide = mcLoc("block/furnace_side");
        ResourceLocation furnaceTop = mcLoc("block/furnace_top");

        ModelFile dropper = models().orientable("block/item_dropper",
                furnaceSide, modLoc("block/advanced_dropper_front"), furnaceTop);
        ModelFile dropperVertical = models().orientableVertical("block/item_dropper_vertical",
                furnaceTop, modLoc("block/advanced_dropper_front_vertical"));
        getVariantBuilder(OCBlocks.ITEM_DROPPER.get()).forAllStates(state -> dispenserFacing(
                state.getValue(BlockStateProperties.FACING), dropper, dropperVertical));
        itemModels().withExistingParent("item_dropper", dropper.getLocation());

        ModelFile breakerOff = models().orientable("block/block_breaker",
                furnaceSide, modLoc("block/block_breaker_front_inactive"), furnaceTop);
        ModelFile breakerOn = models().orientable("block/block_breaker_powered",
                furnaceSide, modLoc("block/block_breaker_front_active"), furnaceTop);
        ModelFile breakerOffVertical = models().orientableVertical("block/block_breaker_vertical",
                furnaceTop, modLoc("block/block_breaker_front_vertical_inactive"));
        ModelFile breakerOnVertical = models().orientableVertical("block/block_breaker_vertical_powered",
                furnaceTop, modLoc("block/block_breaker_front_vertical_active"));
        getVariantBuilder(OCBlocks.BLOCK_BREAKER.get()).forAllStates(state -> dispenserFacing(
                state.getValue(BlockStateProperties.FACING),
                state.getValue(BlockStateProperties.POWERED) ? breakerOn : breakerOff,
                state.getValue(BlockStateProperties.POWERED) ? breakerOnVertical : breakerOffVertical));
        itemModels().withExistingParent("block_breaker", breakerOff.getLocation());

        ModelFile placer = models().orientable("block/block_placer",
                furnaceSide, modLoc("block/block_placer_front"), furnaceTop);
        ModelFile placerVertical = models().orientableVertical("block/block_placer_vertical",
                furnaceTop, modLoc("block/block_placer_front_vertical"));
        getVariantBuilder(OCBlocks.BLOCK_PLACER.get()).forAllStates(state -> dispenserFacing(
                state.getValue(BlockStateProperties.FACING), placer, placerVertical));
        itemModels().withExistingParent("block_placer", placer.getLocation());
    }

    /** Same facing rotations as the vanilla dropper / dispenser blockstates. */
    private static ConfiguredModel[] dispenserFacing(Direction facing, ModelFile horizontal, ModelFile vertical) {
        if (facing.getAxis().isVertical()) {
            return ConfiguredModel.builder()
                    .modelFile(vertical)
                    .rotationX(facing == Direction.DOWN ? 180 : 0)
                    .build();
        }
        return ConfiguredModel.builder()
                .modelFile(horizontal)
                .rotationY(((int) facing.toYRot() + 180) % 360)
                .build();
    }

    private void autoAnvilAndEnchanter() {
        ModelFile anvil = models().getExistingFile(modLoc("block/auto_anvil"));
        horizontalBlock(OCBlocks.AUTO_ANVIL.get(), anvil);
        itemModels().withExistingParent("auto_anvil", anvil.getLocation());

        ModelFile table = models().getExistingFile(modLoc("block/auto_enchanting_table"));
        simpleBlock(OCBlocks.AUTO_ENCHANTMENT_TABLE.get(), table);
        itemModels().withExistingParent("auto_enchanting_table", table.getLocation());
    }

    private void grave() {
        ModelFile withBase = models().getExistingFile(modLoc("block/grave_ground"));
        ModelFile free = models().getExistingFile(modLoc("block/grave_free"));
        getVariantBuilder(OCBlocks.GRAVE.get()).forAllStates(state -> {
            boolean base = state.getValue(GraveBlock.HAS_BASE);
            Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
            return ConfiguredModel.builder()
                    .modelFile(base ? withBase : free)
                    .rotationY((int) facing.toYRot())
                    .build();
        });
        itemModels().withExistingParent("grave", withBase.getLocation());
    }

    private void elevators() {
        ModelFile plain = models().withExistingParent("block/elevator", OCConstants.id("block/cube_all_tinted"))
                .texture("all", modLoc("block/elevator"));

        ModelFile rotating = models().withExistingParent("block/rotating_elevator", OCConstants.id("block/cube_top_tinted"))
                .texture("side", modLoc("block/elevator"))
                .texture("top", modLoc("block/elevator_rot"));

        for (DyeColor colour : DyeColor.values()) {
            simpleBlock(OCBlocks.ELEVATORS.get(colour).get(), plain);
            itemModels().withExistingParent(colour.getName() + "_elevator", plain.getLocation());

            horizontalBlock(OCBlocks.ROTATING_ELEVATORS.get(colour).get(), rotating);
            itemModels().withExistingParent(colour.getName() + "_rotating_elevator", rotating.getLocation());
        }
    }

    private void bigButtons() {
        for (BigButtonMaterial material : BigButtonMaterial.ALL) {
            Block block = OCBlocks.BIG_BUTTONS.get(material).get();
            ResourceLocation texture = ResourceLocation.parse(material.texture());

            ModelFile raised = models()
                    .withExistingParent("block/" + material.blockId(), OCConstants.id("block/big_button"))
                    .texture("texture", texture);
            ModelFile pressed = models()
                    .withExistingParent("block/" + material.blockId() + "_pressed", OCConstants.id("block/big_button_pressed"))
                    .texture("texture", texture);

            getVariantBuilder(block).forAllStates(state -> {
                AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
                Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                boolean powered = state.getValue(BlockStateProperties.POWERED);

                int yRot = ((int) facing.toYRot() + 180) % 360;
                int xRot = switch (face) {
                    case FLOOR -> 0;
                    case WALL -> 90;
                    case CEILING -> 180;
                };
                if (face == AttachFace.CEILING) {
                    yRot = (yRot + 180) % 360;
                }

                return ConfiguredModel.builder()
                        .modelFile(powered ? pressed : raised)
                        .rotationX(xRot)
                        .rotationY(yRot)
                        .uvLock(face == AttachFace.WALL)
                        .build();
            });

            itemModels().withExistingParent(material.blockId(), raised.getLocation());
        }
    }

    /** Plain vanilla wool cut into slabs and stairs, so they borrow the vanilla wool textures. */
    private void woolSlabsAndStairs() {
        for (DyeColor colour : DyeColor.values()) {
            ResourceLocation texture = mcLoc("block/" + colour.getName() + "_wool");

            slabBlock(OCBlocks.WOOL_SLABS.get(colour).get(), texture, texture);
            itemModels().withExistingParent(colour.getName() + "_wool_slab",
                    modLoc("block/" + colour.getName() + "_wool_slab"));

            stairsBlock(OCBlocks.WOOL_STAIRS.get(colour).get(), texture);
            itemModels().withExistingParent(colour.getName() + "_wool_stairs",
                    modLoc("block/" + colour.getName() + "_wool_stairs"));
        }
    }

    /**
     * Same convention as the vanilla ladder: FACING points away from the wall and the rung plane
     * sits on the far side of the block, so the model needs the same half turn vanilla applies.
     */
    private void ropeLadder() {
        ModelFile model = models().getExistingFile(modLoc("block/rope_ladder"));
        getVariantBuilder(OCBlocks.ROPE_LADDER.get()).forAllStatesExcept(state -> {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY(((int) facing.toYRot() + 180) % 360)
                    .build();
        }, BlockStateProperties.WATERLOGGED);
        itemModels().getBuilder("rope_ladder")
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc("block/rope_ladder"));
    }

    /**
     * Only the base plate and its post are a block model. The hoop and the blades turn with a
     * yaw that lives on the block entity, so {@code FanRenderer} draws those.
     */
    private void fan() {
        ModelFile base = models().getExistingFile(modLoc("block/fan_base"));
        simpleBlock(OCBlocks.FAN.get(), base);
        itemModels().withExistingParent("fan", modLoc("block/fan"));
    }

    private void bearTrap() {
        ModelFile open = models().getExistingFile(modLoc("block/bear_trap_open"));
        ModelFile shut = models().getExistingFile(modLoc("block/bear_trap_shut"));
        getVariantBuilder(OCBlocks.BEAR_TRAP.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(BearTrapBlock.SHUT) ? shut : open)
                .rotationY(((int) state.getValue(BearTrapBlock.FACING).toYRot() + 180) % 360)
                .build());
        itemModels().withExistingParent("bear_trap", shut.getLocation());
    }

    /**
     * Sixteen standing angles out of four models. A blockstate can only turn a model in ninety
     * degree steps, so the quarter turns come from the blockstate and the three angles in
     * between are baked into flag_1/2/3. Wall flags reuse the same rotation property, which only
     * ever holds a cardinal segment there, and get a bracketed model instead.
     */
    private void flags() {
        ModelFile[] standing = {
                models().getExistingFile(modLoc("block/flag")),
                models().getExistingFile(modLoc("block/flag_1")),
                models().getExistingFile(modLoc("block/flag_2")),
                models().getExistingFile(modLoc("block/flag_3"))
        };
        ModelFile[] tall = {
                models().getExistingFile(modLoc("block/flag_tall")),
                models().getExistingFile(modLoc("block/flag_tall_1")),
                models().getExistingFile(modLoc("block/flag_tall_2")),
                models().getExistingFile(modLoc("block/flag_tall_3"))
        };
        ModelFile wall = models().getExistingFile(modLoc("block/flag_wall"));
        ModelFile[] tallUpper = {
                models().getExistingFile(modLoc("block/flag_tall_upper")),
                models().getExistingFile(modLoc("block/flag_tall_upper_1")),
                models().getExistingFile(modLoc("block/flag_tall_upper_2")),
                models().getExistingFile(modLoc("block/flag_tall_upper_3"))
        };

        for (DyeColor colour : DyeColor.values()) {
            Block block = OCBlocks.FLAGS.get(colour).get();
            getVariantBuilder(block).forAllStatesExcept(state -> {
                int rotation = state.getValue(FlagBlock.ROTATION);
                if (state.getValue(FlagBlock.WALL)) {
                    return ConfiguredModel.builder()
                            .modelFile(wall)
                            .rotationY((int) FlagBlock.wallFacing(state).toYRot())
                            .build();
                }
                if (state.getValue(FlagBlock.HALF)
                        == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER) {
                    return ConfiguredModel.builder()
                            .modelFile(tallUpper[rotation % 4])
                            .rotationY(standingRotation(rotation))
                            .build();
                }
                ModelFile model = state.getValue(FlagBlock.TALL)
                        ? tall[rotation % 4]
                        : standing[rotation % 4];
                return ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(standingRotation(rotation))
                        .build();
            }, FlagBlock.WATERLOGGED);

            itemModels().withExistingParent(colour.getName() + "_flag", modLoc("block/flag"));
        }
    }

    /**
     * flag_1 and flag_2 lean the pennant back by a segment and by two, so they ride on the
     * quarter turn below. flag_3 leans it forward instead, so it rides on the quarter turn above.
     */
    private static int standingRotation(int segment) {
        int quarter = (segment / 4) * 90;
        if (segment % 4 == 3) {
            quarter += 90;
        }
        return quarter % 360;
    }
}
