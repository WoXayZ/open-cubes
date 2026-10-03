package dev.opencubes.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.opencubes.OCConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

/**
 * Editable widget / side-config sprites under {@code textures/gui/sprites/}, same idea as
 * vanilla's {@code textures/gui/sprites} folder.
 */
public final class GuiSprites {

    /** GUI atlas sprite; nine-slice borders are set in {@code side_config/panel.png.mcmeta}. */
    public static final ResourceLocation SIDE_PANEL_SPRITE = OCConstants.id("side_config/panel");
    public static final ResourceLocation FACE = sprite("side_config/face");
    public static final ResourceLocation FACE_OFF = sprite("side_config/face_off");
    public static final ResourceLocation OPTION = sprite("side_config/option");
    public static final ResourceLocation OPTION_ON = sprite("side_config/option_on");
    public static final ResourceLocation INFO = sprite("widget/info");
    public static final ResourceLocation INFO_HIGHLIGHTED = sprite("widget/info_highlighted");
    public static final ResourceLocation CONFIG = sprite("widget/config");
    public static final ResourceLocation CONFIG_OPEN = sprite("widget/config_open");
    public static final ResourceLocation CONFIG_HIGHLIGHTED = sprite("widget/config_highlighted");
    public static final ResourceLocation BUTTON = sprite("widget/button");
    public static final ResourceLocation BUTTON_HIGHLIGHTED = sprite("widget/button_highlighted");

    /** Drawing table pattern list; nine-slice borders are set in {@code drawing_table/panel.png.mcmeta}. */
    public static final ResourceLocation DRAWING_PANEL_SPRITE = OCConstants.id("drawing_table/panel");
    public static final ResourceLocation DRAWING_TOGGLE = sprite("drawing_table/toggle");
    public static final ResourceLocation DRAWING_TOGGLE_OPEN = sprite("drawing_table/toggle_open");
    public static final ResourceLocation DRAWING_TOGGLE_HIGHLIGHTED = sprite("drawing_table/toggle_highlighted");
    /** 44×18; the label is drawn on top. */
    public static final ResourceLocation DRAWING_CUT = sprite("drawing_table/cut");
    public static final ResourceLocation DRAWING_CUT_HIGHLIGHTED = sprite("drawing_table/cut_highlighted");
    public static final ResourceLocation DRAWING_CUT_DISABLED = sprite("drawing_table/cut_disabled");
    /** 22×20; the tab icon is drawn on top. */
    public static final ResourceLocation DRAWING_TAB = sprite("drawing_table/tab");
    public static final ResourceLocation DRAWING_TAB_SELECTED = sprite("drawing_table/tab_selected");
    public static final ResourceLocation DRAWING_TAB_HIGHLIGHTED = sprite("drawing_table/tab_highlighted");
    /** 18×18 cells under the item; the highlighted one is an overlay drawn over the item. */
    public static final ResourceLocation DRAWING_ENTRY = sprite("drawing_table/entry");
    public static final ResourceLocation DRAWING_ENTRY_SELECTED = sprite("drawing_table/entry_selected");
    public static final ResourceLocation DRAWING_ENTRY_HIGHLIGHTED = sprite("drawing_table/entry_highlighted");

    /** Native size of {@link #OPTION} / {@link #OPTION_ON}. */
    public static final int OPTION_WIDTH = 108;
    public static final int OPTION_HEIGHT = 16;

    private GuiSprites() {}

    public static ResourceLocation sprite(String path) {
        return OCConstants.id("textures/gui/sprites/" + path + ".png");
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height) {
        graphics.blit(texture, x, y, 0, 0, width, height, width, height);
    }

    /** Blits a sprite stretched to the target size from a source texture size. */
    public static void blitStretched(GuiGraphics graphics, ResourceLocation texture,
                                     int x, int y, int width, int height, int texW, int texH) {
        graphics.blit(texture, x, y, width, height, 0, 0, texW, texH, texW, texH);
    }

    /** Tints a white/grey sprite by multiplying with an ARGB colour. */
    public static void blitTinted(GuiGraphics graphics, ResourceLocation texture,
                                  int x, int y, int width, int height, int argb) {
        blitTintedStretched(graphics, texture, x, y, width, height, width, height, argb);
    }

    public static void blitTintedStretched(GuiGraphics graphics, ResourceLocation texture,
                                           int x, int y, int width, int height,
                                           int texW, int texH, int argb) {
        float a = FastColor.ARGB32.alpha(argb) / 255.0F;
        float r = FastColor.ARGB32.red(argb) / 255.0F;
        float g = FastColor.ARGB32.green(argb) / 255.0F;
        float b = FastColor.ARGB32.blue(argb) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(r, g, b, a <= 0.0F ? 1.0F : a);
        blitStretched(graphics, texture, x, y, width, height, texW, texH);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
