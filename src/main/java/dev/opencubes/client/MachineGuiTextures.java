package dev.opencubes.client;

import dev.opencubes.OCConstants;
import net.minecraft.resources.ResourceLocation;

/** Editable PNG backgrounds under {@code assets/opencubes/textures/gui/}. */
public final class MachineGuiTextures {

    public static final ResourceLocation XP_BOTTLER = gui("xp_bottler");
    public static final ResourceLocation VACUUM_HOPPER = gui("vacuum_hopper");
    public static final ResourceLocation SPRINKLER = gui("sprinkler");
    public static final ResourceLocation AUTO_ANVIL = gui("auto_anvil");
    public static final ResourceLocation AUTO_ENCHANTMENT_TABLE = gui("auto_enchantment_table");
    public static final ResourceLocation PAINT_MIXER = gui("paint_mixer");
    public static final ResourceLocation DRAWING_TABLE = gui("drawing_table");
    public static final ResourceLocation DEV_NULL = gui("dev_null");
    public static final ResourceLocation BUILDING_GUIDE = gui("building_guide");
    public static final ResourceLocation HEIGHT_MAP_PROJECTOR = gui("height_map_projector");
    public static final ResourceLocation DISPENSER = gui("dispenser");
    public static final ResourceLocation ITEM_DROPPER = gui("item_dropper");
    public static final ResourceLocation BLOCK_PLACER = gui("block_placer");
    public static final ResourceLocation LUGGAGE = gui("luggage");

    private MachineGuiTextures() {}

    public static ResourceLocation gui(String name) {
        return OCConstants.id("textures/gui/" + name + ".png");
    }
}
