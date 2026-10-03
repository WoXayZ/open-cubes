package dev.opencubes.client;

import dev.opencubes.client.sideconfig.MachineInfoButton;
import dev.opencubes.client.sideconfig.SideConfigPanel;
import dev.opencubes.content.paint.DrawingTableBlockEntity;
import dev.opencubes.content.paint.DrawingTableMenu;
import dev.opencubes.content.paint.GlyphItem;
import dev.opencubes.content.paint.StencilItem;
import dev.opencubes.content.paint.StencilPattern;
import dev.opencubes.registry.OCItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Input on the left, result on the right, Cut in between. The pattern list slides out beside the
 * GUI like the side config panel: it lists every stencil pattern and glyph letter, and clicking
 * one selects what the next Cut produces. Every button is drawn from
 * {@code textures/gui/sprites/drawing_table/}.
 */
public class DrawingTableScreen extends AbstractContainerScreen<DrawingTableMenu> {

    private static final String KEY = "container.opencubes.drawing_table.";
    private static final int GHOST_OVERLAY = 0x808B8B8B;
    private static final int HINT = 0x8B3A3A;

    private static final int CONTENT_WIDTH = 176;
    private static final int PADDING = 7;
    private static final int COLUMNS = 7;
    private static final int PANEL_WIDTH = PADDING + COLUMNS * 18 + PADDING;
    private static final int TAB_Y = PADDING;
    private static final int TAB_WIDTH = 22;
    private static final int TAB_HEIGHT = 20;
    private static final int GRID_Y = TAB_Y + TAB_HEIGHT + 4;

    /** Open by default, since choosing a pattern is the point of the table; kept for the session. */
    private static boolean panelOpen = true;

    private CutButton cutButton;
    private boolean glyphTab;

    public DrawingTableScreen(DrawingTableMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageHeight = 186;
        inventoryLabelY = imageHeight - 94;
        imageWidth = CONTENT_WIDTH + exclusiveWidth();
        glyphTab = !DrawingTableBlockEntity.isStencil(menu.selection());
    }

    /** Extra width folded into {@code imageWidth} so JEI / EMI move aside. */
    private static int exclusiveWidth() {
        return panelOpen ? PANEL_WIDTH + SideConfigPanel.OFFSET : 0;
    }

    @Override
    protected void init() {
        imageWidth = CONTENT_WIDTH + exclusiveWidth();
        super.init();
        cutButton = addRenderableWidget(new CutButton(leftPos + 66, topPos + 35,
                () -> sendButton(DrawingTableMenu.BUTTON_CUT)));
        cutButton.active = hasInput() && outputFree();
        addRenderableWidget(MachineInfoButton.forMachine(leftPos + CONTENT_WIDTH - 44, topPos + 4, "drawing_table"));
        addRenderableWidget(new PanelToggleButton(leftPos + CONTENT_WIDTH - 24, topPos + 4, () -> {
            panelOpen = !panelOpen;
            rebuildWidgets();
        }));
    }

    private void sendButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private boolean hasInput() {
        return menu.getSlot(0).getItem().is(OCItems.UNPREPARED_STENCIL.get());
    }

    private boolean outputFree() {
        return !menu.getSlot(1).hasItem();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        cutButton.active = hasInput() && outputFree();
    }

    private int panelX() {
        return leftPos + CONTENT_WIDTH + SideConfigPanel.OFFSET;
    }

    private boolean isOverPanel(double mouseX, double mouseY) {
        return panelOpen
                && mouseX >= panelX() && mouseX < panelX() + PANEL_WIDTH
                && mouseY >= topPos && mouseY < topPos + imageHeight;
    }

    private int entryCount() {
        return glyphTab ? GlyphItem.CHARACTERS.length() : DrawingTableBlockEntity.PATTERN_COUNT;
    }

    private int entrySelection(int entry) {
        return glyphTab ? DrawingTableBlockEntity.PATTERN_COUNT + entry : entry;
    }

    private static ItemStack tabIcon(boolean glyphs) {
        return glyphs ? GlyphItem.create('A') : StencilItem.create(StencilPattern.CREEPER_FACE);
    }

    /** Entry under the mouse in the panel grid, or -1. */
    private int entryAt(double mouseX, double mouseY) {
        if (!panelOpen) {
            return -1;
        }
        int gx = (int) Math.floor(mouseX) - panelX() - PADDING;
        int gy = (int) Math.floor(mouseY) - topPos - GRID_Y;
        if (gx < 0 || gy < 0 || gx >= COLUMNS * 18) {
            return -1;
        }
        int entry = gy / 18 * COLUMNS + gx / 18;
        return entry < entryCount() ? entry : -1;
    }

    /** 0 for the stencil tab, 1 for the glyph tab, -1 elsewhere. */
    private int tabAt(double mouseX, double mouseY) {
        if (!panelOpen) {
            return -1;
        }
        double x = mouseX - panelX() - PADDING;
        double y = mouseY - topPos - TAB_Y;
        if (y < 0 || y >= TAB_HEIGHT || x < 0) {
            return -1;
        }
        if (x < TAB_WIDTH) {
            return 0;
        }
        if (x >= TAB_WIDTH + 2 && x < TAB_WIDTH * 2 + 2) {
            return 1;
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int tab = tabAt(mouseX, mouseY);
            if (tab >= 0) {
                glyphTab = tab == 1;
                playClick();
                return true;
            }
            int entry = entryAt(mouseX, mouseY);
            if (entry >= 0) {
                sendButton(DrawingTableMenu.BUTTON_SELECT + entrySelection(entry));
                playClick();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void playClick() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        return !isOverPanel(mouseX, mouseY) && super.hasClickedOutside(mouseX, mouseY, left, top, button);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        SideConfigScreenHelper.blitContainer(graphics, MachineGuiTextures.DRAWING_TABLE, leftPos, topPos,
                CONTENT_WIDTH, imageHeight);

        Slot output = menu.getSlot(1);
        if (!output.hasItem()) {
            int sx = leftPos + output.x;
            int sy = topPos + output.y;
            graphics.renderFakeItem(DrawingTableBlockEntity.result(menu.selection()), sx, sy);
            graphics.fill(RenderType.guiGhostRecipeOverlay(), sx, sy, sx + 16, sy + 16, GHOST_OVERLAY);
        }
        if (panelOpen) {
            renderPanel(graphics, mouseX, mouseY);
        }
    }

    private void renderPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        int px = panelX();
        int py = topPos;
        graphics.blitSprite(GuiSprites.DRAWING_PANEL_SPRITE, px, py, PANEL_WIDTH, imageHeight);

        int hoveredTab = tabAt(mouseX, mouseY);
        renderTab(graphics, px + PADDING, py + TAB_Y, false, hoveredTab == 0);
        renderTab(graphics, px + PADDING + TAB_WIDTH + 2, py + TAB_Y, true, hoveredTab == 1);
        Component title = Component.translatable(KEY + (glyphTab ? "tab.glyphs" : "tab.stencils"));
        graphics.drawString(font, title, px + PADDING + TAB_WIDTH * 2 + 8, py + TAB_Y + 6,
                SideConfigScreenHelper.TEXT, false);

        int selection = menu.selection();
        int hovered = entryAt(mouseX, mouseY);
        int count = entryCount();
        for (int entry = 0; entry < count; entry++) {
            int x = px + PADDING + entry % COLUMNS * 18;
            int y = py + GRID_Y + entry / COLUMNS * 18;
            boolean selected = entrySelection(entry) == selection;
            GuiSprites.blit(graphics, selected ? GuiSprites.DRAWING_ENTRY_SELECTED : GuiSprites.DRAWING_ENTRY,
                    x, y, 18, 18);
            graphics.renderFakeItem(DrawingTableBlockEntity.result(entrySelection(entry)), x + 1, y + 1);
            if (entry == hovered) {
                graphics.pose().pushPose();
                graphics.pose().translate(0.0F, 0.0F, 200.0F);
                GuiSprites.blit(graphics, GuiSprites.DRAWING_ENTRY_HIGHLIGHTED, x, y, 18, 18);
                graphics.pose().popPose();
            }
        }

        if (!glyphTab) {
            int rows = (count + COLUMNS - 1) / COLUMNS;
            int y = py + GRID_Y + rows * 18 + 8;
            List<FormattedCharSequence> lines = font.split(
                    Component.translatable(KEY + "tab.stencils.tip"), PANEL_WIDTH - PADDING * 2);
            for (FormattedCharSequence line : lines) {
                graphics.drawString(font, line, px + PADDING, y, SideConfigScreenHelper.TEXT, false);
                y += 10;
            }
        }
    }

    private void renderTab(GuiGraphics graphics, int x, int y, boolean glyphs, boolean hovered) {
        ResourceLocation sprite = glyphs == glyphTab ? GuiSprites.DRAWING_TAB_SELECTED
                : hovered ? GuiSprites.DRAWING_TAB_HIGHLIGHTED
                : GuiSprites.DRAWING_TAB;
        GuiSprites.blit(graphics, sprite, x, y, TAB_WIDTH, TAB_HEIGHT);
        graphics.renderFakeItem(tabIcon(glyphs), x + 3, y + 2);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        int selection = menu.selection();
        drawCentred(graphics, Component.translatable(KEY + "selected"), 58, SideConfigScreenHelper.TEXT);
        drawCentred(graphics, DrawingTableBlockEntity.result(selection).getHoverName(), 68, 0x1F3F7F);
        if (!hasInput()) {
            drawCentred(graphics, Component.translatable(KEY + "need_input"), 80, HINT);
        } else if (!outputFree()) {
            drawCentred(graphics, Component.translatable(KEY + "output_full"), 80, HINT);
        }
    }

    private void drawCentred(GuiGraphics graphics, Component text, int y, int colour) {
        String line = SideConfigScreenHelper.truncate(font, text.getString(), CONTENT_WIDTH - 14);
        graphics.drawString(font, line, (CONTENT_WIDTH - font.width(line)) / 2, y, colour, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        int entry = entryAt(mouseX, mouseY);
        if (entry >= 0) {
            graphics.renderTooltip(font, DrawingTableBlockEntity.result(entrySelection(entry)), mouseX, mouseY);
            return;
        }
        int tab = tabAt(mouseX, mouseY);
        if (tab >= 0) {
            graphics.renderTooltip(font, font.split(
                    Component.translatable(KEY + (tab == 1 ? "tab.glyphs.tip" : "tab.stencils.tip")), 200),
                    mouseX, mouseY);
        }
    }

    /** 44×18 Cut button: sprite background with the translated label on top. */
    private static final class CutButton extends AbstractButton {

        private final Runnable action;

        CutButton(int x, int y, Runnable action) {
            super(x, y, 44, 18, Component.translatable(KEY + "cut"));
            this.action = action;
            setTooltip(Tooltip.create(Component.translatable(KEY + "cut.tip")));
        }

        @Override
        public void onPress() {
            action.run();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ResourceLocation sprite = !active ? GuiSprites.DRAWING_CUT_DISABLED
                    : isHoveredOrFocused() ? GuiSprites.DRAWING_CUT_HIGHLIGHTED
                    : GuiSprites.DRAWING_CUT;
            GuiSprites.blit(graphics, sprite, getX(), getY(), width, height);
            graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + width / 2, getY() + (height - 8) / 2,
                    active ? 0xFFFFFF : 0xA0A0A0);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    /** 16×16 button that slides the pattern list in and out, like the side config one. */
    private static final class PanelToggleButton extends AbstractButton {

        private final Runnable action;

        PanelToggleButton(int x, int y, Runnable action) {
            super(x, y, 16, 16, Component.translatable(KEY + "toggle"));
            this.action = action;
            setTooltip(Tooltip.create(getMessage()));
        }

        @Override
        public void onPress() {
            action.run();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ResourceLocation sprite = isHoveredOrFocused() ? GuiSprites.DRAWING_TOGGLE_HIGHLIGHTED
                    : panelOpen ? GuiSprites.DRAWING_TOGGLE_OPEN
                    : GuiSprites.DRAWING_TOGGLE;
            GuiSprites.blit(graphics, sprite, getX(), getY(), width, height);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
