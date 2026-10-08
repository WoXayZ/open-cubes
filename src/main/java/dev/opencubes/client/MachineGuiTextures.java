package dev.opencubes.client;

import dev.opencubes.OCConstants;
import net.minecraft.resources.Identifier;

/** Editable PNG backgrounds under {@code assets/opencubes/textures/gui/}. */
public final class MachineGuiTextures {

    public static final Identifier XP_BOTTLER = gui("xp_bottler");
    public static final Identifier VACUUM_HOPPER = gui("vacuum_hopper");
    public static final Identifier SPRINKLER = gui("sprinkler");
    public static final Identifier AUTO_ANVIL = gui("auto_anvil");
    public static final Identifier AUTO_ENCHANTMENT_TABLE = gui("auto_enchantment_table");
    public static final Identifier PAINT_MIXER = gui("paint_mixer");
    public static final Identifier DRAWING_TABLE = gui("drawing_table");
    public static final Identifier DEV_NULL = gui("dev_null");
    public static final Identifier BUILDING_GUIDE = gui("building_guide");
    public static final Identifier HEIGHT_MAP_PROJECTOR = gui("height_map_projector");
    public static final Identifier DISPENSER = gui("dispenser");
    public static final Identifier ITEM_DROPPER = gui("item_dropper");
    public static final Identifier BLOCK_PLACER = gui("block_placer");
    public static final Identifier LUGGAGE = gui("luggage");

    private MachineGuiTextures() {}

    public static Identifier gui(String name) {
        return OCConstants.id("textures/gui/" + name + ".png");
    }
}
