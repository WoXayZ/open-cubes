package dev.opencubes.data;

import dev.opencubes.OCConstants;
import dev.opencubes.content.button.BigButtonMaterial;
import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class OCLanguageProvider extends LanguageProvider {

    public OCLanguageProvider(PackOutput output) {
        super(output, OCConstants.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + OCConstants.MOD_ID + ".main", "OpenCubes");
        add(OCItems.INFO_BOOK.get(), "World Domination");
        add("book.opencubes.world_domination.landing",
                "Elevators, cranes, liquid XP, paint, gliders and the rest.$(br2)"
                        + "Each entry covers the craft and how it behaves.");
        add("book.opencubes.world_domination.subtitle", "A practical field manual");
        add("opencubes.misc.info_book_missing_patchouli",
                "Install Patchouli to read this book.");

        for (DyeColor colour : DyeColor.values()) {
            String name = titleCase(colour.getName());
            add(OCBlocks.ELEVATORS.get(colour).get(), name + " Elevator");
            add(OCBlocks.ROTATING_ELEVATORS.get(colour).get(), name + " Rotating Elevator");
            add(OCBlocks.FLAGS.get(colour).get(), name + " Flag");
            add(OCBlocks.WOOL_SLABS.get(colour).get(), name + " Wool Slab");
            add(OCBlocks.WOOL_STAIRS.get(colour).get(), name + " Wool Stairs");
            add(OCItems.SLEEPING_BAGS.get(colour).get(), name + " Sleeping Bag");
        }

        for (BigButtonMaterial material : BigButtonMaterial.ALL) {
            add(OCBlocks.BIG_BUTTONS.get(material).get(), titleCase(material.id()) + " Big Button");
        }

        add(OCBlocks.ROPE_LADDER.get(), "Rope Ladder");
        add(OCBlocks.FAN.get(), "Fan");
        add(OCBlocks.BEAR_TRAP.get(), "Bear Trap");
        add(OCBlocks.TANK.get(), "Tank");
        add(OCBlocks.XP_DRAIN.get(), "XP Drain");
        add(OCBlocks.XP_SHOWER.get(), "XP Shower");
        add(OCBlocks.XP_BOTTLER.get(), "XP Bottler");
        add(OCBlocks.VACUUM_HOPPER.get(), "Vacuum Hopper");
        add(OCBlocks.ITEM_DROPPER.get(), "Advanced Dropper");
        add(OCBlocks.BLOCK_BREAKER.get(), "Block Breaker");
        add(OCBlocks.BLOCK_PLACER.get(), "Block Placer");
        add(OCBlocks.AUTO_ANVIL.get(), "Auto Anvil");
        add(OCBlocks.AUTO_ENCHANTMENT_TABLE.get(), "Auto Enchanting Table");
        add(OCBlocks.GRAVE.get(), "Grave");
        add(OCBlocks.TROPHY.get(), "Trophy");
        add("block.opencubes.trophy.entity", "%s Trophy");
        add("opencubes.jade.trophy.type", "Type: %s");
        add("opencubes.jade.trophy.cooldown", "Cooldown: %ss");
        add("opencubes.jade.trophy.ready", "Ready");
        add("opencubes.jade.trophy.drop", "Drop: %sx %s");
        add("config.jade.plugin_opencubes.trophy", "Trophy");
        add("opencubes.jade.tank.fluid", "%s: %sB / %sB");
        add("opencubes.jade.tank.empty", "Empty: 0B / %sB");
        add("opencubes.jade.tank.count", "%s connected tanks");
        add("config.jade.plugin_opencubes.tank", "Tank");
        add("opencubes.jade.luggage.special", "Special");
        add("opencubes.jade.luggage.slots", "%s / %s slots");
        add("config.jade.plugin_opencubes.luggage", "Luggage");
        add("opencubes.luggage.tooltip.entry", "%sx %s");
        add("opencubes.luggage.tooltip.more", "... and %s more");
        add(OCItems.XP_BUCKET.get(), "XP Bucket");
        add(OCItems.WRENCH.get(), "Wrench");
        add(OCItems.SLIMALYZER.get(), "Slimalyzer");
        add(OCItems.PEDOMETER.get(), "Pedometer");
        add(OCItems.DEV_NULL.get(), "/dev/null");
        add(OCItems.GOLDEN_EYE.get(), "Golden Eye");
        add(OCItems.LUGGAGE.get(), "Luggage");
        add(OCBlocks.CANVAS.get(), "Canvas");
        add(OCBlocks.GLASS_CANVAS.get(), "Glass Canvas");
        add(OCBlocks.PAINT_CAN.get(), "Paint Can");
        add(OCBlocks.PAINT_MIXER.get(), "Paint Mixer");
        add(OCBlocks.DRAWING_TABLE.get(), "Drawing Table");
        add(OCBlocks.BUILDING_GUIDE.get(), "Building Guide");
        add(OCBlocks.ENHANCED_BUILDING_GUIDE.get(), "Enhanced Building Guide");
        add(OCBlocks.HEIGHT_MAP_PROJECTOR.get(), "Height Map Projector");
        add(OCItems.EMPTY_MAP.get(), "Empty Map");
        add(OCItems.HEIGHT_MAP.get(), "Height Map");
        add(OCItems.CARTOGRAPHER.get(), "Cartographer");
        add("entity.opencubes.cartographer", "Cartographer");
        add("opencubes.misc.cartographer_progress", "Mapping: %s%%");
        add(OCItems.BEAM.get(), "Beam");
        add(OCItems.LINE.get(), "Line");
        add(OCItems.CRANE_ENGINE.get(), "Crane Engine");
        add(OCItems.CRANE_MAGNET.get(), "Crane Magnet");
        add(OCItems.CRANE_BACKPACK.get(), "Crane Backpack");
        add(OCItems.CRANE_CONTROL.get(), "Crane Control");
        add("opencubes.tooltip.crane_control",
                "Wear the Crane Backpack. Hold to lower, sneak+hold to raise, left-click to grab/release.");
        add("opencubes.tooltip.sky", "Shows the sky while powered.");
        add("opencubes.tooltip.sky_inverted", "Shows the sky until powered, upside down.");
        add(OCItems.GLIDER_WING_LEFT.get(), "Left Glider Wing");
        add(OCItems.GLIDER_WING_RIGHT.get(), "Right Glider Wing");
        add(OCItems.HANG_GLIDER.get(), "Hang Glider");
        add(OCItems.THERMAL_ELYTRA.get(), "Thermal Elytra");
        add(OCItems.SONIC_GLASSES.get(), "Sonic Glasses");
        add(OCItems.PENCIL.get(), "Pencil");
        add(OCItems.CRAYON.get(), "Crayon");
        add(OCItems.PENCIL_GLASSES.get(), "Pencil Glasses");
        add(OCItems.CRAYON_GLASSES.get(), "Crayon Glasses");
        add(OCItems.TECHNICOLOR_GLASSES.get(), "Amazing Technicolor Glasses");
        add(OCItems.ADMIN_GLASSES.get(), "Badass Glasses");
        add(OCBlocks.IMAGINARY.get(), "Imaginary Block");
        add(OCBlocks.SKY_BLOCK.get(), "Sky Block");
        add(OCBlocks.INVERTED_SKY_BLOCK.get(), "Inverted Sky Block");
        add("opencubes.misc.glider_engaged", "Hang glider engaged");
        add("opencubes.misc.glider_stowed", "Hang glider stowed");
        add("opencubes.misc.imaginary_uses", "Uses: %s");
        add("opencubes.misc.color", "Colour: %s");
        add("opencubes.misc.mode.block", "Mode: Block");
        add("opencubes.misc.mode.panel", "Mode: Panel");
        add("opencubes.misc.mode.half_panel", "Mode: Half Panel");
        add("opencubes.misc.mode.stairs", "Mode: Stairs");
        add("opencubes.misc.mode.inverted_block", "Mode: Inverted Block");
        add("opencubes.misc.mode.inverted_panel", "Mode: Inverted Panel");
        add("opencubes.misc.mode.inverted_half_panel", "Mode: Inverted Half Panel");
        add("opencubes.misc.mode.inverted_stairs", "Mode: Inverted Stairs");
        add("key.categories.opencubes", "OpenCubes");
        add("key.opencubes.variometer", "Toggle Variometer");
        add("key.opencubes.variometer_volume_up", "Variometer Volume Up");
        add("key.opencubes.variometer_volume_down", "Variometer Volume Down");
        add("opencubes.misc.variometer_volume", "Variometer volume: %s%%");
        add("opencubes.misc.variometer_on", "Variometer: ON");
        add("opencubes.misc.variometer_off", "Variometer: OFF");
        add(OCItems.CURSOR.get(), "Cursor");
        add("opencubes.misc.cursor_bound", "Bound to %s, %s, %s");
        add("opencubes.misc.cursor_unbound", "Not bound - sneak-use a block");
        add("opencubes.misc.cursor_wrong_dim", "Cursor target is in another dimension");
        add("opencubes.misc.cursor_too_far", "Cursor target is too far away");
        add("opencubes.misc.cursor_no_xp", "Not enough experience to use the Cursor");
        add("advancements.opencubes.root.title", "OpenCubes");
        add("advancements.opencubes.root.description", "Install OpenCubes and get started");
        add("advancements.opencubes.brick_dropped.title", "That's Just Gross");
        add("advancements.opencubes.brick_dropped.description", "Drop a brick");
        add("advancements.opencubes.stack_overflow.title", "Stack Overflow");
        add("advancements.opencubes.stack_overflow.description", "Put a /dev/null inside a /dev/null");
        add("stat.opencubes.bricks_dropped", "Bricks Dropped");
        add(OCBlocks.TEMPORARY_SCAFFOLDING.get(), "Temporary Scaffolding");
        add(OCBlocks.LIQUID_SPONGE.get(), "Liquid Sponge");
        add(OCItems.LIQUID_SPONGE_ON_A_STICK.get(), "Liquid Sponge on a Stick");
        add(OCItems.SPONGE_ON_A_STICK.get(), "Sponge on a Stick");
        add(OCItems.WET_SPONGE_ON_A_STICK.get(), "Wet Sponge on a Stick");
        add(OCBlocks.HEALER.get(), "Healer");
        add(OCBlocks.SPRINKLER.get(), "Sprinkler");
        add("container.opencubes.sprinkler", "Sprinkler");
        add("container.opencubes.sprinkler.water", "Water: %s / %s mB");
        add(OCBlocks.ARCHERY_TARGET.get(), "Archery Target");
        add(OCBlocks.ITEM_CANNON.get(), "Item Cannon");
        add(OCItems.POINTER.get(), "Pointer");
        add(OCBlocks.GOLDEN_EGG.get(), "Golden Egg");
        add(OCBlocks.VILLAGE_HIGHLIGHTER.get(), "Village Highlighter");
        add("entity.opencubes.mini_me", "Mini Me");
        add("enchantment.opencubes.unstable", "Unstable");
        add("enchantment.opencubes.flim_flam", "Flim Flam");
        add("enchantment.opencubes.last_stand", "Last Stand");
        add(OCItems.EPIC_ERASER.get(), "Epic Eraser");
        add(OCItems.TASTY_CLAY.get(), "Tasty Clay");
        add("key.opencubes.drop_brick", "Drop Brick");
        add("opencubes.misc.flim_flammed", "You have been flim-flammed!");
        add("commands.opencubes.flimflam.ok", "Flim-flammed %s with %s");
        add("commands.opencubes.flimflam.fail", "Could not flim-flam %s with %s");
        add("commands.opencubes.luck.read", "%s luck: %s");
        add("commands.opencubes.luck.set", "%s luck is now %s");
        add("opencubes.misc.pointer_selected", "Cannon selected at %s, %s, %s");
        add("opencubes.misc.pointer_aimed", "Aimed at %s, %s, %s");
        add("entity.opencubes.magnet", "Magnet");
        add("entity.opencubes.mounted_block", "Mounted Block");
        add("container.opencubes.height_map_projector", "Height Map Projector");
        add("opencubes.misc.map_scale", "Scale: 1:%s");
        add("opencubes.misc.map_id", "Map #%s");
        add("opencubes.misc.map_center", "Center: %s, %s");
        add(OCItems.PAINT_BRUSH.get(), "Paint Brush");
        add(OCItems.SQUEEGEE.get(), "Squeegee");
        add(OCItems.STENCIL.get(), "Stencil");
        add(OCItems.UNPREPARED_STENCIL.get(), "Unprepared Stencil");
        add(OCItems.SKETCHING_PENCIL.get(), "Sketching Pencil");
        add(OCItems.GLYPH.get(), "Glyph");
        add("item.opencubes.glyph.named", "'%s' Glyph");
        add("entity.opencubes.luggage", "Luggage");
        add("entity.opencubes.golden_eye", "Golden Eye");
        add("entity.opencubes.glyph", "Glyph");
        add("container.opencubes.paint_mixer", "Paint Mixer");
        add("container.opencubes.drawing_table", "Drawing Table");
        add("opencubes.gui.paint_mixer.mix", "Mix");
        add("opencubes.gui.paint_mixer.color", "Colour");
        add("opencubes.gui.paint_mixer.slot.milk", "Milk bucket or empty paint can");
        add("opencubes.gui.paint_mixer.slot.milk_short", "Milk");
        add("opencubes.gui.paint_mixer.slot.ink", "Ink");
        add("opencubes.gui.paint_mixer.slot.cyan", "Cyan dye");
        add("opencubes.gui.paint_mixer.slot.magenta", "Magenta dye");
        add("opencubes.gui.paint_mixer.slot.yellow", "Yellow dye");
        add("opencubes.gui.paint_mixer.slot.black", "Black dye");
        add("opencubes.gui.paint_mixer.slot.output", "Mixed paint can output");
        add("opencubes.gui.paint_mixer.slot.output_short", "Can");
        add("opencubes.misc.village_highlighter.status",
                "Village: %s villagers, %s beds, signal %s");
        add("opencubes.misc.paint_amount", "Paint: %s");
        add("opencubes.misc.tank_contents", "%s: %s mB");
        add("opencubes.misc.change_box_size", "Box size: (%s, %s, %s) → (%s, %s, %s)");
        add("opencubes.misc.change_mode", "Shape: %s");
        add("opencubes.misc.total_blocks", "Total blocks: %s");
        for (dev.opencubes.content.guide.GuideShape shape : dev.opencubes.content.guide.GuideShape.VALUES) {
            add(shape.translationKey(), titleCase(shape.getSerializedName().replace('_', ' ')));
        }
        stencil(dev.opencubes.content.paint.StencilPattern.CREEPER_FACE, "Creeper Face");
        stencil(dev.opencubes.content.paint.StencilPattern.BORDER, "Border");
        stencil(dev.opencubes.content.paint.StencilPattern.STRIPES, "Stripes");
        stencil(dev.opencubes.content.paint.StencilPattern.CORNER, "Corner");
        stencil(dev.opencubes.content.paint.StencilPattern.CORNER2, "Solid Corner");
        stencil(dev.opencubes.content.paint.StencilPattern.CORNER3, "Double Corner");
        stencil(dev.opencubes.content.paint.StencilPattern.HOLE, "Hole");
        stencil(dev.opencubes.content.paint.StencilPattern.SPIRAL, "Spiral");
        stencil(dev.opencubes.content.paint.StencilPattern.THICKSTRIPES, "Thick Stripes");
        stencil(dev.opencubes.content.paint.StencilPattern.SPLAT, "Splat");
        stencil(dev.opencubes.content.paint.StencilPattern.STORAGE, "Storage");
        stencil(dev.opencubes.content.paint.StencilPattern.HEART, "Heart");
        stencil(dev.opencubes.content.paint.StencilPattern.HEART2, "Solid Heart");
        stencil(dev.opencubes.content.paint.StencilPattern.MUSIC, "Music Note");
        stencil(dev.opencubes.content.paint.StencilPattern.BALLOON, "Balloon");
        add("fluid_type.opencubes.xp_juice", "Liquid XP");
        add("container.opencubes.dev_null", "/dev/null");
        add("container.opencubes.luggage", "Luggage");
        add("opencubes.misc.oh_no_ground", "This place is not safe enough to sleep.");
        add("opencubes.misc.no_nearby_structures", "No nearby structures found.");
        add("opencubes.misc.locked_on_nearest_structure", "Locked on %s");
        add("opencubes.misc.pedometer.tracking_started", "Pedometer tracking started.");
        add("opencubes.misc.pedometer.tracking_reset", "Pedometer tracking reset.");
        add("opencubes.misc.pedometer.start_point", "Start: %s");
        add("opencubes.misc.pedometer.speed", "Speed: %s");
        add("opencubes.misc.pedometer.avg_speed", "Average speed: %s");
        add("opencubes.misc.pedometer.total_distance", "Total distance: %s");
        add("opencubes.misc.pedometer.straight_line_distance", "Straight-line distance: %s");
        add("opencubes.misc.pedometer.straight_line_speed", "Straight-line speed: %s");
        add("opencubes.misc.pedometer.last_check_speed", "Since last check speed: %s");
        add("opencubes.misc.pedometer.last_check_distance", "Since last check distance: %s");
        add("opencubes.misc.pedometer.last_check_time", "Since last check time: %s ticks");
        add("opencubes.misc.pedometer.total_time", "Total time: %s ticks");
        add("subtitles.opencubes.item.slimalyzer.ping", "Slimalyzer pings");
        add("subtitles.opencubes.item.pedometer.use", "Pedometer beeps");
        add("opencubes.misc.get_witched", "You have been witched!");

        add("container.opencubes.big_button", "Big Button");
        add("container.opencubes.xp_bottler", "XP Bottler");
        add("container.opencubes.xp_bottler.fluid", "Liquid XP: %s / %s mB");
        add("container.opencubes.block_placer", "Block Placer");
        add("container.opencubes.item_dropper", "Advanced Dropper");
        add("container.opencubes.item_dropper.speed", "Speed: %s");
        add("container.opencubes.item_dropper.redstone", "RS ×");
        add("container.opencubes.item_dropper.redstone_on", "Scaled by signal");
        add("container.opencubes.vacuum_hopper", "Vacuum Hopper");
        add("container.opencubes.vacuum_hopper.items", "Items →");
        add("container.opencubes.vacuum_hopper.xp", "XP →");
        add("container.opencubes.vacuum_hopper.off", "OFF");
        add("container.opencubes.side_config.input", "In ←");
        add("container.opencubes.side_config.output", "Out →");
        add("container.opencubes.side_config.xp", "XP ←");
        add("container.opencubes.side_config.auto_pull", "Auto pull");
        add("container.opencubes.side_config.auto_push", "Auto push");
        add("container.opencubes.side_config.auto_xp", "Auto XP");
        add("container.opencubes.auto_anvil", "Auto Anvil");
        add("container.opencubes.auto_enchanting_table", "Auto Enchanting Table");
        add("container.opencubes.auto_enchanting_table.level", "Slot L%s");
        add("container.opencubes.auto_enchanting_table.power", "Cap %s / shelves %s");

        // Side configuration panel and the machine screens that open it.
        add("container.opencubes.side_config.toggle", "Configure sides");
        add("container.opencubes.side_config.title", "Side config");
        add("container.opencubes.side_config.on", "ON");
        add("container.opencubes.side_config.off", "OFF");
        add("container.opencubes.side_config.face_tooltip", "%s: %s");
        add("container.opencubes.side_config.face.down", "Bottom");
        add("container.opencubes.side_config.face.up", "Top");
        add("container.opencubes.side_config.face.north", "North");
        add("container.opencubes.side_config.face.south", "South");
        add("container.opencubes.side_config.face.west", "West");
        add("container.opencubes.side_config.face.east", "East");
        add("container.opencubes.side_config.row.item_in", "Item input");
        add("container.opencubes.side_config.row.item_out", "Item output");
        add("container.opencubes.side_config.row.xp_in", "Liquid XP input");
        add("container.opencubes.side_config.row.xp_out", "Liquid XP output");
        add("container.opencubes.side_config.auto_pull.tip",
                "Pull items from the neighbours on the input faces.");
        add("container.opencubes.side_config.auto_push.tip",
                "Push finished items to the neighbours on the output faces.");
        add("container.opencubes.side_config.auto_xp.tip",
                "Exchange liquid XP with the neighbours on the XP faces.");
        add("container.opencubes.vacuum_hopper.suction", "Suction: %s");
        add("container.opencubes.vacuum_hopper.buffer", "XP %s%%");
        add("container.opencubes.vacuum_hopper.toggle_hint",
                "Sneak-click the block with an empty hand to switch the suction on or off.");
        add("container.opencubes.item_dropper.speed_label", "Speed");
        add("container.opencubes.item_dropper.speed_tip",
                "Throw speed of the dropped items. Hold Shift for ±10 steps.");
        add("container.opencubes.item_dropper.redstone_label", "Redstone");
        add("container.opencubes.item_dropper.redstone_short", "RS");
        add("container.opencubes.item_dropper.redstone_tip",
                "Scale the throw speed with the redstone signal strength.");
        add("container.opencubes.auto_enchanting_table.slot", "L%s");
        add("container.opencubes.auto_enchanting_table.cap", "Cap %s");
        add("container.opencubes.auto_enchanting_table.shelves", "Shelves %s");
        add("container.opencubes.auto_enchanting_table.next", "Next");
        add("container.opencubes.auto_enchanting_table.next.tip",
                "Cycle through the three enchantment offers.");
        add("container.opencubes.auto_enchanting_table.cap.tip",
                "Limit how many bookshelves the table may use. Hold Shift for ±10.");
        add("container.opencubes.drawing_table.cut", "Cut");
        add("container.opencubes.drawing_table.cut.tip",
                "Cut the unprepared stencil on the left into the selection shown on the right.");
        add("container.opencubes.drawing_table.selected", "Selected:");
        add("container.opencubes.drawing_table.need_input", "Insert an unprepared stencil");
        add("container.opencubes.drawing_table.output_full", "Take the result out first");
        add("container.opencubes.drawing_table.tab.stencils", "Stencils");
        add("container.opencubes.drawing_table.tab.stencils.tip",
                "Paint masks: lay one on a block, then paint over it with a brush.");
        add("container.opencubes.drawing_table.tab.glyphs", "Glyphs");
        add("container.opencubes.drawing_table.tab.glyphs.tip",
                "Letters and signs to stick on walls, to write names and labels.");
        add("container.opencubes.drawing_table.toggle", "Show or hide the pattern list");
        add("opencubes.misc.glyph_place_tip",
                "Right-click a wall to stick it where you aim; punch it to take it back.");
        add("opencubes.misc.stencil_place_tip",
                "Right-click a block face to lay it down, again to turn it, sneak to take it back.");
        add("opencubes.misc.stencil_brush_tip",
                "Paint over it with a Paint Brush: only the holes take the colour.");

        add("container.opencubes.side_config.info", "Machine info");
        add("container.opencubes.auto_anvil.info",
                "Combines tools and modifiers using Liquid XP from the internal tank. Configure which faces accept items and XP.");
        add("container.opencubes.auto_enchanting_table.info",
                "Enchants items with Liquid XP. Bookshelves nearby raise the power cap; pick an offer slot and limit.");
        add("container.opencubes.xp_bottler.info",
                "Fills glass bottles with Liquid XP to make Bottles o' Enchanting. Pull bottles in, push bottles out.");
        add("container.opencubes.vacuum_hopper.info",
                "Sucks nearby items and XP orbs, then pushes them out the configured faces. Sneak-click empty-handed to toggle suction.");
        add("container.opencubes.building_guide.info",
                "Projects a ghost shape for building. Adjust size per axis, rotate, and pick a marker colour.");
        add("container.opencubes.enhanced_building_guide.info",
                "Like the Building Guide. Right-click with a block while powered to place one cell; creative with obsidian on top fills the shape. Materials come from adjacent inventories.");
        add("container.opencubes.item_dropper.info",
                "Ejects items from its inventory on a redstone pulse. Use +/- to set throw speed; hold Shift for larger steps.");
        add("container.opencubes.block_placer.info",
                "Places a block from its 3x3 inventory on a redstone pulse into the faced space.");
        add("container.opencubes.sprinkler.info",
                "Draws water from below and keeps farmland moist while speeding crop growth. Bone meal in the grid boosts growth.");
        add("container.opencubes.paint_mixer.info",
                "Mixes milk or a paint can with cyan, magenta, yellow and black dyes into a full paint can of the chosen colour.");
        add("container.opencubes.drawing_table.info",
                "Turns an unprepared stencil into a paint stencil or a wall glyph. Pick one in the panel on the right, then press Cut.");
        add("container.opencubes.height_map_projector.info",
                "Projects a Height Map as a hologram above the block. Use the arrows to rotate the display.");

        add("subtitles.opencubes.block.elevator.activate", "Elevator whooshes");
        add("subtitles.opencubes.block.bear_trap.open", "Bear trap opens");
        add("subtitles.opencubes.block.bear_trap.close", "Bear trap snaps");
        add("subtitles.opencubes.block.bottler.done", "Bottler finishes");
        add("subtitles.opencubes.block.grave.rob", "Grave is robbed");
        add("opencubes.misc.grave_msg", "%s on day %s");
        add("opencubes.misc.grave_of", "Here lies %s");
        add("commands.opencubes.inventory.stored", "Stored inventory for %s as %s");
        add("commands.opencubes.inventory.restored", "Restored inventory for %s from %s");
        add("commands.opencubes.inventory.missing", "No inventory dump named %s");

        add("block.opencubes.xp_juice", "Liquid XP");

        // Building Guide screen, which replaced the old click-the-right-pixel controls.
        add("container.opencubes.building_guide.shape", "Shape");
        add("container.opencubes.building_guide.size", "Size");
        add("container.opencubes.building_guide.colour", "Marker colour");
        add("container.opencubes.building_guide.mirror", "Copy this extent to the opposite side");
        add("container.opencubes.building_guide.rotate_ccw", "Rotate counter-clockwise");
        add("container.opencubes.building_guide.rotate_cw", "Rotate clockwise");
        add("container.opencubes.building_guide.facing", "Facing: %s");
        add("container.opencubes.building_guide.blocks", "Markers: %s");
        add("container.opencubes.direction.down", "Down");
        add("container.opencubes.direction.up", "Up");
        add("container.opencubes.direction.north", "North");
        add("container.opencubes.direction.south", "South");
        add("container.opencubes.direction.west", "West");
        add("container.opencubes.direction.east", "East");

        add("opencubes.misc.vacuum_on", "Suction on");
        add("opencubes.misc.vacuum_off", "Suction off");
    }

    private void stencil(dev.opencubes.content.paint.StencilPattern pattern, String name) {
        add("item.opencubes.stencil." + pattern.id(), name + " Stencil");
    }

    private static String titleCase(String id) {
        StringBuilder builder = new StringBuilder(id.length());
        boolean capitalise = true;
        for (char c : id.toCharArray()) {
            if (c == '_') {
                builder.append(' ');
                capitalise = true;
            } else {
                builder.append(capitalise ? Character.toUpperCase(c) : c);
                capitalise = false;
            }
        }
        return builder.toString();
    }
}
