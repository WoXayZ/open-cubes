package dev.opencubes.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.opencubes.OCConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Vanilla-looking container chrome drawn from plain fills so machine screens can lay out their
 * own slots instead of borrowing a vanilla texture that never quite fits.
 */
public final class SideConfigScreenHelper {

    public static final Direction[] SIDES = {
            Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    public static final String[] SIDE_LABELS = {"D", "U", "N", "S", "W", "E"};

    public static final String[] SIDE_KEYS = {"down", "up", "north", "south", "west", "east"};

    public static final int BODY = 0xFFC6C6C6;
    public static final int LIGHT_EDGE = 0xFFFFFFFF;
    public static final int DARK_EDGE = 0xFF555555;
    public static final int SLOT_BODY = 0xFF8B8B8B;
    public static final int SLOT_SHADOW = 0xFF373737;
    public static final int TEXT = 0x404040;

    public static final ResourceLocation XP_STILL = OCConstants.id("block/xp_juice_still");
    public static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");

    private SideConfigScreenHelper() {}

    /**
     * Blits an editable PNG background. Sheets follow the vanilla 256×256 convention
     * (artwork in the top-left); stretching a full sheet into {@code width}×{@code height}
     * was shifting inventory frames over the labels on dispenser-like GUIs.
     */
    public static void blitContainer(GuiGraphics graphics, ResourceLocation texture,
                                   int x, int y, int width, int height) {
        graphics.blit(texture, x, y, 0, 0, width, height, 256, 256);
    }

    /** Grey body with the vanilla two pixel bevel. */
    public static void drawContainerBody(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, BODY);
        graphics.fill(x, y, x + width - 2, y + 2, LIGHT_EDGE);
        graphics.fill(x, y, x + 2, y + height - 2, LIGHT_EDGE);
        graphics.fill(x + 2, y + height - 2, x + width, y + height, DARK_EDGE);
        graphics.fill(x + width - 2, y + 2, x + width, y + height, DARK_EDGE);
    }

    /** Draws the 18x18 frame around a slot; pass the slot position minus one pixel. */
    public static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, SLOT_BODY);
        graphics.fill(x, y, x + 18, y + 1, SLOT_SHADOW);
        graphics.fill(x, y, x + 1, y + 18, SLOT_SHADOW);
        graphics.fill(x + 17, y + 1, x + 18, y + 18, LIGHT_EDGE);
        graphics.fill(x + 1, y + 17, x + 18, y + 18, LIGHT_EDGE);
    }

    /** Frames for a rectangular block of slots, given the top left slot position. */
    public static void drawSlotGrid(GuiGraphics graphics, int x, int y, int columns, int rows) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                drawSlot(graphics, x + col * 18, y + row * 18);
            }
        }
    }

    /** The three rows plus hotbar every machine menu places at 8/84 and 8/142. */
    public static void drawPlayerInventory(GuiGraphics graphics, int left, int top) {
        drawSlotGrid(graphics, left + 7, top + 83, 9, 3);
        drawSlotGrid(graphics, left + 7, top + 141, 9, 1);
    }

    /** Vertical gauge filled with the liquid XP still sprite. */
    public static void drawFluidGauge(GuiGraphics graphics, int x, int y, int width, int height,
                                      int amount, int capacity) {
        int tint = IClientFluidTypeExtensions.of(dev.opencubes.registry.OCFluids.XP_JUICE_TYPE.get())
                .getTintColor();
        drawFluidGauge(graphics, x, y, width, height, amount, capacity, XP_STILL, tint);
    }

    /** Vertical gauge filled with the vanilla water still sprite. */
    public static void drawWaterFluidGauge(GuiGraphics graphics, int x, int y, int width, int height,
                                           int amount, int capacity) {
        FluidStack water = new FluidStack(Fluids.WATER, 1000);
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(water.getFluidType());
        ResourceLocation still = extensions.getStillTexture(water);
        if (still == null) {
            still = WATER_STILL;
        }
        drawFluidGauge(graphics, x, y, width, height, amount, capacity, still, extensions.getTintColor(water));
    }

    /** Vertical fluid gauge tiled from a block-atlas still texture. */
    public static void drawFluidGauge(GuiGraphics graphics, int x, int y, int width, int height,
                                      int amount, int capacity, ResourceLocation stillTexture, int argb) {
        graphics.fill(x, y, x + width, y + height, 0xFF20201E);
        int filled = capacity <= 0 ? 0 : Math.min(height, amount * height / capacity);
        if (filled > 0) {
            TextureAtlasSprite sprite = Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(stillTexture);
            blitTiledFluid(graphics, sprite, x, y + height - filled, width, filled, argb);
        }
        graphics.renderOutline(x - 1, y - 1, width + 2, height + 2, 0xFF373737);
    }

    private static void blitTiledFluid(GuiGraphics graphics, TextureAtlasSprite sprite,
                                       int x, int y, int width, int height, int argb) {
        float a = ((argb >> 24) & 0xFF) / 255.0F;
        if (a <= 0.0F) {
            a = 1.0F;
        }
        float r = ((argb >> 16) & 0xFF) / 255.0F;
        float g = ((argb >> 8) & 0xFF) / 255.0F;
        float b = (argb & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(r, g, b, a);
        graphics.enableScissor(x, y, x + width, y + height);

        int tileW = Math.max(1, sprite.contents().width());
        int tileH = Math.max(1, sprite.contents().height());
        int yMax = y + height;
        for (int ty = yMax - tileH; ty > y - tileH; ty -= tileH) {
            for (int tx = x; tx < x + width; tx += tileW) {
                int drawW = Math.min(tileW, x + width - tx);
                int drawH = tileH;
                graphics.blit(tx, ty, 0, drawW, drawH, sprite);
            }
        }

        graphics.disableScissor();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static int lighten(int colour, int amount) {
        int r = Math.min(255, ((colour >> 16) & 0xFF) + amount);
        int g = Math.min(255, ((colour >> 8) & 0xFF) + amount);
        int b = Math.min(255, (colour & 0xFF) + amount);
        return (colour & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    /** Clips a string to the given pixel width, appending an ellipsis when it had to cut. */
    public static String truncate(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        int room = Math.max(0, maxWidth - font.width("..."));
        return font.plainSubstrByWidth(text, room) + "...";
    }
}
