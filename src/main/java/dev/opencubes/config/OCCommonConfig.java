package dev.opencubes.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-authoritative settings. Values are read on the logical server only; anything the
 * client needs to know is sent to it rather than read from here.
 */
public final class OCCommonConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue ELEVATOR_TRAVEL_DISTANCE;
    public static final ModConfigSpec.IntValue ELEVATOR_MAX_BLOCKS_PASSED;
    public static final ModConfigSpec.BooleanValue ELEVATOR_IGNORE_BLOCKS;
    public static final ModConfigSpec.BooleanValue ELEVATOR_MATCH_COLOUR;
    public static final ModConfigSpec.BooleanValue ELEVATOR_CENTRE_ON_BLOCK;
    public static final ModConfigSpec.DoubleValue ELEVATOR_XP_PER_BLOCK;

    public static final ModConfigSpec.BooleanValue ROPE_LADDER_INFINITE;

    public static final ModConfigSpec.DoubleValue FAN_FORCE;
    public static final ModConfigSpec.DoubleValue FAN_RANGE;
    public static final ModConfigSpec.BooleanValue FAN_NEEDS_REDSTONE;

    public static final ModConfigSpec.IntValue XP_MILLIBUCKETS_PER_POINT;

    public static final ModConfigSpec.BooleanValue TANK_UPDATE;
    public static final ModConfigSpec.IntValue TANK_BALANCE_THRESHOLD;
    public static final ModConfigSpec.IntValue TANK_BUCKETS_PER_TANK;
    public static final ModConfigSpec.BooleanValue TANK_EMIT_LIGHT;
    public static final ModConfigSpec.BooleanValue TANK_ALLOW_BUCKET_DRAIN;

    public static final ModConfigSpec.DoubleValue ITEM_DROPPER_MAX_SPEED;
    public static final ModConfigSpec.IntValue BLOCK_BREAKER_ACTION_LIMIT;
    public static final ModConfigSpec.IntValue BLOCK_PLACER_ACTION_LIMIT;

    public static final ModConfigSpec.IntValue VACUUM_HOPPER_RANGE;
    public static final ModConfigSpec.IntValue VACUUM_HOPPER_TICK_INTERVAL;

    public static final ModConfigSpec.IntValue ITEM_CANNON_FIRE_INTERVAL;

    public static final ModConfigSpec.IntValue AUTO_ANVIL_COOLDOWN;
    public static final ModConfigSpec.IntValue AUTO_ANVIL_MAX_LEVELS;
    public static final ModConfigSpec.IntValue AUTO_ENCHANTMENT_TABLE_MAX_LEVELS;

    public static final ModConfigSpec.BooleanValue GRAVES_ENABLED;
    public static final ModConfigSpec.BooleanValue GRAVES_REQUIRE_ITEM;
    public static final ModConfigSpec.BooleanValue GRAVES_DESTRUCTIVE;
    public static final ModConfigSpec.IntValue GRAVES_SPAWN_RANGE;
    public static final ModConfigSpec.BooleanValue GRAVES_BACKUP;
    public static final ModConfigSpec.BooleanValue GRAVES_BASE;
    public static final ModConfigSpec.BooleanValue GRAVES_SPAWN_SKELETONS;
    public static final ModConfigSpec.DoubleValue GRAVES_SKELETON_RATE;
    public static final ModConfigSpec.DoubleValue GRAVES_SPECIAL_ACTION;
    public static final ModConfigSpec.IntValue GRAVES_MIN_Y;
    public static final ModConfigSpec.IntValue GRAVES_MAX_Y;
    public static final ModConfigSpec.BooleanValue DUMP_DEAD_PLAYER_INVENTORIES;

    public static final ModConfigSpec.DoubleValue TROPHY_DROP_CHANCE;

    public static final ModConfigSpec.IntValue GUIDE_REDSTONE_SENSITIVITY;
    public static final ModConfigSpec.IntValue GUIDE_MAX_MARKERS;

    public static final ModConfigSpec.BooleanValue CRANE_PICK_ENTITIES;
    public static final ModConfigSpec.BooleanValue CRANE_PICK_BLOCKS;
    public static final ModConfigSpec.BooleanValue CRANE_SHIFT_CONTROL;
    public static final ModConfigSpec.BooleanValue CRANE_COLLISION_CHECK;

    public static final ModConfigSpec.BooleanValue HANG_GLIDER_THERMAL;

    public static final ModConfigSpec.IntValue SCAFFOLDING_DESPAWN_RATE;
    public static final ModConfigSpec.IntValue SCAFFOLDING_PLAYER_PROTECT_RADIUS;

    public static final ModConfigSpec.IntValue SPONGE_RANGE;
    public static final ModConfigSpec.IntValue SPONGE_STICK_RANGE;
    public static final ModConfigSpec.IntValue SPONGE_STICK_DURABILITY;
    public static final ModConfigSpec.BooleanValue SPONGE_BLOCK_UPDATE;
    public static final ModConfigSpec.BooleanValue SPONGE_STICK_UPDATE;

    public static final ModConfigSpec.IntValue HEALER_XP_MB_PER_PULSE;

    public static final ModConfigSpec.IntValue SPRINKLER_RANGE;
    public static final ModConfigSpec.IntValue SPRINKLER_INTERNAL_TANK;
    public static final ModConfigSpec.IntValue SPRINKLER_WATER_CONSUME_RATE;
    public static final ModConfigSpec.IntValue SPRINKLER_BONEMEAL_CONSUME_RATE;
    public static final ModConfigSpec.IntValue SPRINKLER_FERTILIZE_CHANCE;
    public static final ModConfigSpec.IntValue SPRINKLER_BONEMEAL_FERTILIZE_CHANCE;

    public static final ModConfigSpec.BooleanValue UNSTABLE_ENABLED;
    public static final ModConfigSpec.BooleanValue UNSTABLE_GRIEF;
    public static final ModConfigSpec.BooleanValue LAST_STAND_ENABLED;
    public static final ModConfigSpec.DoubleValue LAST_STAND_XP_PER_DAMAGE;
    public static final ModConfigSpec.BooleanValue FLIM_FLAM_ENCHANT_ENABLED;

    public static final ModConfigSpec.BooleanValue TOMFOOLERY_ENABLED;
    public static final ModConfigSpec.BooleanValue WE_ARE_SERIOUS_PEOPLE;
    public static final ModConfigSpec.BooleanValue FLIM_FLAM_SAFE_ONLY;
    public static final ModConfigSpec.BooleanValue FLIM_FLAM_WHITELIST;
    public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> FLIM_FLAM_LIST;

    public static final ModConfigSpec.BooleanValue SPAM_INFO_BOOK;

    public static final ModConfigSpec.IntValue CURSOR_MAX_DISTANCE;
    public static final ModConfigSpec.BooleanValue TECHNICOLOR_GLASSES_LOOT;

    public static final ModConfigSpec.DoubleValue IMAGINARY_USES;
    public static final ModConfigSpec.IntValue PAINT_BRUSH_USES;
    public static final ModConfigSpec.BooleanValue PAINT_BRUSH_REPLACE_BLOCKS;
    public static final ModConfigSpec.BooleanValue GLYPHS_SHOW_IN_CREATIVE;
    public static final ModConfigSpec.BooleanValue XP_BUCKET_SHOW_IN_CREATIVE;
    public static final ModConfigSpec.BooleanValue DEV_NULL_SNEAK_TO_OPEN;

    public static final ModConfigSpec.IntValue GOLDEN_EYE_SEARCH_RADIUS;
    public static final ModConfigSpec.IntValue GOLDEN_EYE_MAX_DAMAGE;
    public static final ModConfigSpec.IntValue GOLDEN_EYE_PEARL_REPAIR;

    public static final ModConfigSpec.IntValue PROJECTOR_LIGHT_LEVEL;

    public static final ModConfigSpec.DoubleValue LUGGAGE_COLLECT_RANGE;
    public static final ModConfigSpec.DoubleValue LUGGAGE_FOLLOW_START;
    public static final ModConfigSpec.DoubleValue LUGGAGE_FOLLOW_STOP;
    public static final ModConfigSpec.BooleanValue LUGGAGE_COLLECT_ITEMS;
    public static final ModConfigSpec.BooleanValue LUGGAGE_PLAY_WALKING_SOUND;

    public static final ModConfigSpec.IntValue BIG_BUTTON_EMPTY_DURATION;

    public static final ModConfigSpec.BooleanValue GOLDEN_EGG_PICK_BLOCKS;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.comment("Elevators").push("elevator");
        ELEVATOR_TRAVEL_DISTANCE = BUILDER
                .comment("How far an elevator looks for the next one, in blocks.")
                .defineInRange("travelDistance", 20, 1, 256);
        ELEVATOR_MAX_BLOCKS_PASSED = BUILDER
                .comment("How many solid blocks may sit between two elevators before the trip fails.")
                .defineInRange("maxBlocksPassed", 4, 0, 256);
        ELEVATOR_IGNORE_BLOCKS = BUILDER
                .comment("Ignore blocks between elevators entirely, as if maxBlocksPassed were unlimited.")
                .define("ignoreBlocks", false);
        ELEVATOR_MATCH_COLOUR = BUILDER
                .comment("Require both elevators to be the same colour.",
                        "Turning this off makes every elevator part of one shaft.")
                .define("matchColour", true);
        ELEVATOR_CENTRE_ON_BLOCK = BUILDER
                .comment("Drop the player in the middle of the destination block instead of keeping their",
                        "horizontal position.")
                .define("centreOnBlock", false);
        ELEVATOR_XP_PER_BLOCK = BUILDER
                .comment("Experience points consumed per block travelled. Zero makes elevators free.")
                .defineInRange("experiencePerBlock", 0.0D, 0.0D, 100.0D);
        BUILDER.pop();

        BUILDER.comment("Rope ladder").push("rope_ladder");
        ROPE_LADDER_INFINITE = BUILDER
                .comment("If true, a single item places a ladder all the way down, and broken",
                        "segments drop nothing.")
                .define("infinite", false);
        BUILDER.pop();

        BUILDER.comment("Fan").push("fan");
        FAN_FORCE = BUILDER
                .comment("Maximum force applied every tick to entities in the cone.")
                .defineInRange("force", 0.05D, 0.0D, 1.0D);
        FAN_RANGE = BUILDER
                .comment("Range of the fan, in blocks.")
                .defineInRange("range", 10.0D, 1.0D, 64.0D);
        FAN_NEEDS_REDSTONE = BUILDER
                .comment("If true, fan force scales with redstone power. If false, the fan",
                        "always runs at full strength.")
                .define("needsRedstone", true);
        BUILDER.pop();

        BUILDER.comment("Experience and tanks").push("xp");
        XP_MILLIBUCKETS_PER_POINT = BUILDER
                .comment("Millibuckets of XP juice equal to one experience point.")
                .defineInRange("millibucketsPerPoint", 20, 1, 1000);
        BUILDER.pop();

        BUILDER.comment("Tanks").push("tank");
        TANK_UPDATE = BUILDER
                .comment("If true, tanks drain downward and equalise with horizontal neighbours.")
                .define("update", true);
        TANK_BALANCE_THRESHOLD = BUILDER
                .comment("Skip a horizontal equalise when the difference is this many millibuckets or less.")
                .defineInRange("balanceThreshold", 1, 0, 1000);
        TANK_BUCKETS_PER_TANK = BUILDER
                .comment("Capacity of each tank in buckets (1000 mB each). Restart recommended after change.")
                .defineInRange("bucketsPerTank", 16, 1, 256);
        TANK_EMIT_LIGHT = BUILDER
                .comment("Tanks emit light based on the contained fluid (lava, glow fluids, …).")
                .define("emitLight", true);
        TANK_ALLOW_BUCKET_DRAIN = BUILDER
                .comment("Allow emptying a tank with buckets / fluid containers (including XP buckets).")
                .define("allowBucketDrain", true);
        BUILDER.pop();

        BUILDER.comment("Item dropper").push("item_dropper");
        ITEM_DROPPER_MAX_SPEED = BUILDER
                .comment("Maximum drop speed that can be set in the item dropper GUI.")
                .defineInRange("maxItemDropSpeed", 4.0D, 0.0D, 64.0D);
        BUILDER.pop();

        BUILDER.comment("Block breaker").push("block_breaker");
        BLOCK_BREAKER_ACTION_LIMIT = BUILDER
                .comment("How many break actions may be queued in one tick before later pulses wait.")
                .defineInRange("actionLimit", 16, 1, 256);
        BUILDER.pop();

        BUILDER.comment("Block placer").push("block_placer");
        BLOCK_PLACER_ACTION_LIMIT = BUILDER
                .comment("How many place actions may be queued in one tick before later pulses wait.")
                .defineInRange("actionLimit", 16, 1, 256);
        BUILDER.pop();

        BUILDER.comment("Vacuum hopper").push("vacuum_hopper");
        VACUUM_HOPPER_RANGE = BUILDER
                .comment("Suck radius around the hopper, in blocks.")
                .defineInRange("range", 3, 1, 16);
        VACUUM_HOPPER_TICK_INTERVAL = BUILDER
                .comment("Ticks between suck attempts.")
                .defineInRange("tickInterval", 10, 1, 200);
        BUILDER.pop();

        BUILDER.comment("Item cannon").push("item_cannon");
        ITEM_CANNON_FIRE_INTERVAL = BUILDER
                .comment("Ticks between shots while powered.")
                .defineInRange("fireInterval", 20, 1, 200);
        BUILDER.pop();

        BUILDER.comment("Auto Anvil").push("auto_anvil");
        AUTO_ANVIL_COOLDOWN = BUILDER
                .comment("Ticks between combine attempts.")
                .defineInRange("cooldown", 40, 1, 400);
        AUTO_ANVIL_MAX_LEVELS = BUILDER
                .comment("Maximum XP levels stored as liquid XP in the anvil tank.")
                .defineInRange("maxLevels", 45, 1, 200);
        BUILDER.pop();

        BUILDER.comment("Auto Enchanting Table").push("auto_enchanting_table");
        AUTO_ENCHANTMENT_TABLE_MAX_LEVELS = BUILDER
                .comment("Maximum XP levels stored as liquid XP in the table tank.")
                .defineInRange("maxLevels", 30, 1, 200);
        BUILDER.pop();

        BUILDER.comment("Graves").push("graves");
        GRAVES_ENABLED = BUILDER
                .comment("Spawn a grave holding the player's items and XP on death.")
                .define("enabled", true);
        GRAVES_REQUIRE_ITEM = BUILDER
                .comment("Require a grave block item in the player's inventory (consumed on death).")
                .define("requireItem", false);
        GRAVES_DESTRUCTIVE = BUILDER
                .comment("If no air/replaceable spot is found, overwrite solid blocks (no block entities).")
                .define("destructive", false);
        GRAVES_SPAWN_RANGE = BUILDER
                .comment("Search cube diameter around the death point for a grave spot.")
                .defineInRange("spawnRange", 10, 1, 64);
        GRAVES_BACKUP = BUILDER
                .comment("Write an inventory-*.dat backup when a grave is placed or fails to place.")
                .define("backup", true);
        GRAVES_BASE = BUILDER
                .comment("Place a dirt block under the grave when the space below is empty.")
                .define("base", true);
        GRAVES_SPAWN_SKELETONS = BUILDER
                .comment("Occasionally spawn skeletons or bats near graves.")
                .define("spawnSkeletons", true);
        GRAVES_SKELETON_RATE = BUILDER
                .comment("Chance per tick that a grave tries to spawn a mob.")
                .defineInRange("skeletonSpawnRate", 0.002D, 0.0D, 1.0D);
        GRAVES_SPECIAL_ACTION = BUILDER
                .comment("Chance that shovel-robbing a grave triggers thunder and the rob sound.")
                .defineInRange("specialAction", 0.03D, 0.0D, 1.0D);
        GRAVES_MIN_Y = BUILDER
                .comment("Lowest Y a grave may be placed at.")
                .defineInRange("minY", -64, -2048, 2048);
        GRAVES_MAX_Y = BUILDER
                .comment("Highest Y a grave may be placed at.")
                .defineInRange("maxY", 320, -2048, 2048);
        BUILDER.pop();

        BUILDER.comment("Inventory backups").push("inventory");
        DUMP_DEAD_PLAYER_INVENTORIES = BUILDER
                .comment("Write a full inventory dump on death (restorable with /opencubes inventory restore).")
                .define("dumpDeadPlayersInventories", true);
        BUILDER.pop();

        BUILDER.comment("Trophies").push("trophy");
        TROPHY_DROP_CHANCE = BUILDER
                .comment("Base chance that a player kill drops that mob's trophy.",
                        "Final roll: (looting + rand/4) * chance - rand > 0.")
                .defineInRange("dropChance", 0.001D, 0.0D, 1.0D);
        BUILDER.pop();

        BUILDER.comment("Building guides").push("guide");
        GUIDE_REDSTONE_SENSITIVITY = BUILDER
                .comment("Marker visibility vs redstone: 0 = always, 1 = when powered, -1 = when unpowered.")
                .defineInRange("redstoneSensitivity", 1, -1, 1);
        GUIDE_MAX_MARKERS = BUILDER
                .comment("Hard cap on shape markers to protect FPS / memory.")
                .defineInRange("maxMarkers", 10000, 100, 100000);
        BUILDER.pop();

        BUILDER.comment("Crane").push("crane");
        CRANE_PICK_ENTITIES = BUILDER
                .comment("Allow the crane magnet to pick up living entities, items, boats and minecarts.")
                .define("pickEntities", true);
        CRANE_PICK_BLOCKS = BUILDER
                .comment("Allow the crane magnet to pick up blocks into mounted-block entities.")
                .define("pickBlocks", true);
        CRANE_SHIFT_CONTROL = BUILDER
                .comment("If true, hold control lowers the arm and sneak+hold raises it; otherwise each use toggles direction.")
                .define("shiftControl", true);
        CRANE_COLLISION_CHECK = BUILDER
                .comment("If true, the magnet tip stops when the arm ray hits a solid block.")
                .define("collisionCheck", false);
        BUILDER.pop();

        BUILDER.comment("Flight").push("hang_glider");
        HANG_GLIDER_THERMAL = BUILDER
                .comment("Enable world thermal lift for the Hang Glider and Thermal Elytra.")
                .define("enableThermal", true);
        BUILDER.pop();

        BUILDER.comment("Temporary scaffolding").push("scaffolding");
        SCAFFOLDING_DESPAWN_RATE = BUILDER
                .comment("Random-tick denominator for despawn. 0 = always despawn on tick. Higher = lasts longer.")
                .defineInRange("despawnRate", 4, 0, 256);
        SCAFFOLDING_PLAYER_PROTECT_RADIUS = BUILDER
                .comment("Skip random-tick despawn when a player is within this many blocks.",
                        "0 = always despawn on a successful random tick, ignoring nearby players.")
                .defineInRange("playerProtectRadius", 10, 0, 128);
        BUILDER.pop();

        BUILDER.comment("Sponge").push("sponge");
        SPONGE_RANGE = BUILDER
                .comment("Soak radius of the sponge block, in blocks.")
                .defineInRange("range", 3, 1, 16);
        SPONGE_STICK_RANGE = BUILDER
                .comment("Soak radius of the sponge-on-a-stick, in blocks.")
                .defineInRange("stickRange", 3, 1, 16);
        SPONGE_STICK_DURABILITY = BUILDER
                .comment("Max durability of a newly crafted sponge-on-a-stick. Restart recommended after change.")
                .defineInRange("stickDurability", 256, 1, 4096);
        SPONGE_BLOCK_UPDATE = BUILDER
                .comment("Send full neighbour updates when the block soaks fluids.")
                .define("blockUpdate", false);
        SPONGE_STICK_UPDATE = BUILDER
                .comment("Send full neighbour updates when the stick soaks fluids.")
                .define("stickUpdate", false);
        BUILDER.pop();

        BUILDER.comment("Healer").push("healer");
        HEALER_XP_MB_PER_PULSE = BUILDER
                .comment("Millibuckets of liquid XP consumed per second while healing at least one player.")
                .defineInRange("xpMbPerPulse", 20, 1, 1000);
        BUILDER.pop();

        BUILDER.comment("Sprinkler").push("sprinkler");
        SPRINKLER_RANGE = BUILDER
                .comment("Hydration / growth radius around the sprinkler, in blocks.")
                .defineInRange("range", 4, 1, 16);
        SPRINKLER_INTERNAL_TANK = BUILDER
                .comment("Internal water buffer capacity in millibuckets.")
                .defineInRange("internalTank", 50, 1, 8000);
        SPRINKLER_WATER_CONSUME_RATE = BUILDER
                .comment("Ticks between consuming 1 mB of water (and enabling while water remains).")
                .defineInRange("waterConsumeRate", 20, 1, 200);
        SPRINKLER_BONEMEAL_CONSUME_RATE = BUILDER
                .comment("Ticks between consuming one bone meal from the internal inventory.")
                .defineInRange("bonemealConsumeRate", 600, 1, 10000);
        SPRINKLER_FERTILIZE_CHANCE = BUILDER
                .comment("1/N chance per tick to try fertilising without bone meal.")
                .defineInRange("fertilizeChance", 500, 1, 10000);
        SPRINKLER_BONEMEAL_FERTILIZE_CHANCE = BUILDER
                .comment("1/N chance per tick to try fertilising while bone meal is loaded.")
                .defineInRange("bonemealFertilizeChance", 200, 1, 10000);
        BUILDER.pop();

        BUILDER.comment("Enchantments").push("enchantments");
        UNSTABLE_ENABLED = BUILDER
                .comment("Allow Unstable enchantment behaviour (gunpowder detonations).")
                .define("unstableEnabled", true);
        UNSTABLE_GRIEF = BUILDER
                .comment("Level-3 Unstable explosions break blocks.")
                .define("unstableGrief", true);
        LAST_STAND_ENABLED = BUILDER
                .comment("Allow Last Stand enchantment behaviour.")
                .define("lastStandEnabled", true);
        LAST_STAND_XP_PER_DAMAGE = BUILDER
                .comment("Last Stand XP cost = ceil(postMitigationDamage * this / enchantLevel).")
                .defineInRange("lastStandXpPerDamage", 25.0D, 1.0D, 1000.0D);
        FLIM_FLAM_ENCHANT_ENABLED = BUILDER
                .comment("Allow Flim Flam enchantment to accumulate luck on PvP hits.",
                        "Commands still work when tomfoolery.enabled is true.")
                .define("flimFlamEnabled", true);
        BUILDER.pop();

        BUILDER.comment("Tomfoolery").push("tomfoolery");
        TOMFOOLERY_ENABLED = BUILDER
                .comment("Master switch for flim-flam karma delivery and related jokes.")
                .define("enabled", true);
        WE_ARE_SERIOUS_PEOPLE = BUILDER
                .comment("If true, the tasty-clay / B-key brick joke is disabled.")
                .define("weAreSeriousPeople", true);
        FLIM_FLAM_SAFE_ONLY = BUILDER
                .comment("Only allow safe flim-flam effects.")
                .define("safeOnly", false);
        FLIM_FLAM_WHITELIST = BUILDER
                .comment("If true, flimFlamList is a whitelist; otherwise a blacklist.")
                .define("flimFlamWhitelist", false);
        FLIM_FLAM_LIST = BUILDER
                .comment("Effect names to blacklist (or whitelist).")
                .defineListAllowEmpty("flimFlamList", java.util.List.of(), () -> "", o -> o instanceof String);
        BUILDER.pop();

        BUILDER.comment("Documentation").push("book");
        SPAM_INFO_BOOK = BUILDER
                .comment("Give every player an Info Book on first login.")
                .define("spamInfoBook", true);
        BUILDER.pop();

        BUILDER.comment("Cursor").push("cursor");
        CURSOR_MAX_DISTANCE = BUILDER
                .comment("Maximum reach of a bound Cursor, in blocks (same dimension only).")
                .defineInRange("maxDistance", 64, 1, 256);
        BUILDER.pop();

        BUILDER.comment("Loot").push("loot");
        TECHNICOLOR_GLASSES_LOOT = BUILDER
                .comment("Inject Amazing Technicolor Glasses into simple dungeon / stronghold / mansion chests.")
                .define("technicolorGlasses", true);
        BUILDER.pop();

        BUILDER.comment("Imaginary blocks").push("imaginary");
        IMAGINARY_USES = BUILDER
                .comment("Uses on a newly crafted pencil/crayon.")
                .defineInRange("uses", 10.0D, 1.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.comment("Painting").push("paint_brush");
        PAINT_BRUSH_USES = BUILDER
                .comment("Durability (uses) of a paint brush. Restart recommended after change.")
                .defineInRange("uses", 24, 1, 1024);
        PAINT_BRUSH_REPLACE_BLOCKS = BUILDER
                .comment("If true, the brush converts suitable blocks into canvas when they cannot be painted.")
                .define("replaceBlocks", true);
        BUILDER.pop();

        BUILDER.comment("Glyphs").push("glyphs");
        GLYPHS_SHOW_IN_CREATIVE = BUILDER
                .comment("Show the glyph item in the OpenCubes creative tab.")
                .define("showInCreativeSearch", true);
        BUILDER.pop();

        BUILDER.comment("XP bucket").push("xp_bucket");
        XP_BUCKET_SHOW_IN_CREATIVE = BUILDER
                .comment("Show the XP bucket in the OpenCubes creative tab.")
                .define("showInCreative", true);
        BUILDER.pop();

        BUILDER.comment("/dev/null").push("dev_null");
        DEV_NULL_SNEAK_TO_OPEN = BUILDER
                .comment("If true, right-click opens the GUI only while sneaking.")
                .define("sneakToOpen", false);
        BUILDER.pop();

        BUILDER.comment("Golden Eye").push("golden_eye");
        GOLDEN_EYE_SEARCH_RADIUS = BUILDER
                .comment("Structure search radius in chunks when locking on.")
                .defineInRange("searchRadius", 100, 1, 512);
        GOLDEN_EYE_MAX_DAMAGE = BUILDER
                .comment("Durability of a newly crafted Golden Eye. Restart recommended after change.")
                .defineInRange("maxDamage", 100, 1, 10000);
        GOLDEN_EYE_PEARL_REPAIR = BUILDER
                .comment("Durability restored per ender pearl in the recharge recipe.")
                .defineInRange("pearlRepair", 10, 1, 1000);
        BUILDER.pop();

        BUILDER.comment("Height map projector").push("height_map_projector");
        PROJECTOR_LIGHT_LEVEL = BUILDER
                .comment("Block light emitted by an active projector (0-15).")
                .defineInRange("lightLevel", 10, 0, 15);
        BUILDER.pop();

        BUILDER.comment("Luggage").push("luggage");
        LUGGAGE_COLLECT_RANGE = BUILDER
                .comment("How far luggage reaches for ground items, in blocks.")
                .defineInRange("collectRange", 10.0D, 1.0D, 64.0D);
        LUGGAGE_FOLLOW_START = BUILDER
                .comment("Distance at which luggage starts following its owner.")
                .defineInRange("followStart", 10.0D, 1.0D, 64.0D);
        LUGGAGE_FOLLOW_STOP = BUILDER
                .comment("Distance at which luggage stops following (closer than this).")
                .defineInRange("followStop", 2.0D, 0.5D, 32.0D);
        LUGGAGE_COLLECT_ITEMS = BUILDER
                .comment("Should luggage pick up nearby dropped items.")
                .define("collectItems", true);
        LUGGAGE_PLAY_WALKING_SOUND = BUILDER
                .comment("Should luggage play a footstep sound while walking.")
                .define("playWalkingSound", true);
        BUILDER.pop();

        BUILDER.comment("Big buttons").push("big_button");
        BIG_BUTTON_EMPTY_DURATION = BUILDER
                .comment("Press duration in ticks when the duration inventory is empty.")
                .defineInRange("emptyDuration", 20, 1, 1200);
        BUILDER.pop();

        BUILDER.comment("Golden egg").push("golden_egg");
        GOLDEN_EGG_PICK_BLOCKS = BUILDER
                .comment("During the float phase, pick up nearby magnet-liftable blocks.")
                .define("pickBlocks", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private OCCommonConfig() {}
}
