package dev.opencubes.client;

import dev.opencubes.OCConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/**
 * Editable widget / side-config sprites under {@code textures/gui/sprites/}, same idea as
 * vanilla's {@code textures/gui/sprites} folder.
 */
public final class GuiSprites {

    /** GUI atlas sprite; nine-slice borders are set in {@code side_config/panel.png.mcmeta}. */
    public static final Identifier SIDE_PANEL_SPRITE = OCConstants.id("side_config/panel");
    public static final Identifier FACE = sprite("side_config/face");
    public static final Identifier FACE_OFF = sprite("side_config/face_off");
    public static final Identifier OPTION = sprite("side_config/option");
    public static final Identifier OPTION_ON = sprite("side_config/option_on");
    public static final Identifier INFO = sprite("widget/info");
    public static final Identifier INFO_HIGHLIGHTED = sprite("widget/info_highlighted");
    public static final Identifier CONFIG = sprite("widget/config");
    public static final Identifier CONFIG_OPEN = sprite("widget/config_open");
    public static final Identifier CONFIG_HIGHLIGHTED = sprite("widget/config_highlighted");
    public static final Identifier BUTTON = sprite("widget/button");
    public static final Identifier BUTTON_HIGHLIGHTED = sprite("widget/button_highlighted");

    /** Drawing table pattern list; nine-slice borders are set in {@code drawing_table/panel.png.mcmeta}. */
    public static final Identifier DRAWING_PANEL_SPRITE = OCConstants.id("drawing_table/panel");
    public static final Identifier DRAWING_TOGGLE = sprite("drawing_table/toggle");
    public static final Identifier DRAWING_TOGGLE_OPEN = sprite("drawing_table/toggle_open");
    public static final Identifier DRAWING_TOGGLE_HIGHLIGHTED = sprite("drawing_table/toggle_highlighted");
    /** 44×18; the label is drawn on top. */
    public static final Identifier DRAWING_CUT = sprite("drawing_table/cut");
    public static final Identifier DRAWING_CUT_HIGHLIGHTED = sprite("drawing_table/cut_highlighted");
    public static final Identifier DRAWING_CUT_DISABLED = sprite("drawing_table/cut_disabled");
    /** 22×20; the tab icon is drawn on top. */
    public static final Identifier DRAWING_TAB = sprite("drawing_table/tab");
    public static final Identifier DRAWING_TAB_SELECTED = sprite("drawing_table/tab_selected");
    public static final Identifier DRAWING_TAB_HIGHLIGHTED = sprite("drawing_table/tab_highlighted");
    /** 18×18 cells under the item; the highlighted one is an overlay drawn over the item. */
    public static final Identifier DRAWING_ENTRY = sprite("drawing_table/entry");
    public static final Identifier DRAWING_ENTRY_SELECTED = sprite("drawing_table/entry_selected");
    public static final Identifier DRAWING_ENTRY_HIGHLIGHTED = sprite("drawing_table/entry_highlighted");

    /** Native size of {@link #OPTION} / {@link #OPTION_ON}. */
    public static final int OPTION_WIDTH = 108;
    public static final int OPTION_HEIGHT = 16;

    private GuiSprites() {}

    public static Identifier sprite(String path) {
        return OCConstants.id("textures/gui/sprites/" + path + ".png");
    }

    public static void blit(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int width, int height) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, width, height, width, height);
    }

    /** Blits a sprite stretched to the target size from a source texture size. */
    public static void blitStretched(GuiGraphicsExtractor graphics, Identifier texture,
                                     int x, int y, int width, int height, int texW, int texH) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, width, height, texW, texH, texW, texH);
    }

    /** Tints a white/grey sprite by multiplying with an ARGB colour. */
    public static void blitTinted(GuiGraphicsExtractor graphics, Identifier texture,
                                  int x, int y, int width, int height, int argb) {
        blitTintedStretched(graphics, texture, x, y, width, height, width, height, argb);
    }

    public static void blitTintedStretched(GuiGraphicsExtractor graphics, Identifier texture,
                                           int x, int y, int width, int height,
                                           int texW, int texH, int argb) {
        int color = ARGB.alpha(argb) <= 0 ? ARGB.opaque(argb) : argb;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F,
                width, height, texW, texH, texW, texH, color);
    }
}
