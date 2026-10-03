package dev.opencubes.client.sideconfig;

import dev.opencubes.client.GuiSprites;
import dev.opencubes.client.SideConfigScreenHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

/**
 * Side and automation configuration drawn as its own window next to a machine GUI, so the
 * container body keeps nothing but slots. The open state is remembered per menu class for the
 * rest of the session, which is what makes the panel feel persistent between openings.
 */
public final class SideConfigPanel {

    public static final int ITEM_COLOUR = 0xFF3F8F3F;
    public static final int XP_COLOUR = 0xFF3A6FB0;

    /** Gap between the container window and the panel. */
    public static final int OFFSET = 4;

    private static final int PADDING = 7;
    private static final int BUTTON_SIZE = 16;
    private static final int BUTTON_STRIDE = 18;
    /** Match {@link GuiSprites#OPTION_WIDTH} so option rows are not horizontally squashed. */
    private static final int ROW_WIDTH = GuiSprites.OPTION_WIDTH;
    /** Public so screens can expand {@code imageWidth} before {@code super.init()}. */
    public static final int WIDTH = ROW_WIDTH + PADDING * 2;

    private static final Set<String> OPEN_PANELS = new HashSet<>();

    private final String persistenceKey;
    private final IntConsumer buttonSender;
    private final List<Row> rows = new ArrayList<>();
    private final List<AbstractWidget> widgets = new ArrayList<>();

    private Runnable onToggle = () -> {};
    private int panelX;
    private int panelY;
    private int panelHeight;

    public SideConfigPanel(Class<?> menuClass, IntConsumer buttonSender) {
        this.persistenceKey = menuClass.getName();
        this.buttonSender = buttonSender;
    }

    public static boolean isOpen(Class<?> menuClass) {
        return OPEN_PANELS.contains(menuClass.getName());
    }

    /** Extra width to fold into {@code imageWidth} so JEI / EMI move aside. */
    public static int exclusiveWidth(Class<?> menuClass) {
        return isOpen(menuClass) ? WIDTH + OFFSET : 0;
    }

    public SideConfigPanel onToggle(Runnable onToggle) {
        this.onToggle = onToggle == null ? () -> {} : onToggle;
        return this;
    }

    public SideConfigPanel addItemRow(Component label, int buttonBase, Predicate<Direction> enabled) {
        rows.add(new SideRow(label, buttonBase, enabled, ITEM_COLOUR));
        return this;
    }

    public SideConfigPanel addXpRow(Component label, int buttonBase, Predicate<Direction> enabled) {
        rows.add(new SideRow(label, buttonBase, enabled, XP_COLOUR));
        return this;
    }

    public SideConfigPanel addOptionRow(Component label, Component tooltip, int buttonId,
                                        BooleanSupplier enabled) {
        rows.add(new OptionRow(label, tooltip, buttonId, enabled));
        return this;
    }

    /** Two rows of 16 dye swatches; {@code buttonBase + dyeId} is sent on click. */
    public SideConfigPanel addColourSwatches(Component label, int buttonBase, IntSupplier selectedRgb) {
        rows.add(new ColourRow(label, buttonBase, selectedRgb));
        return this;
    }

    public int width() {
        return WIDTH;
    }

    public int height() {
        return panelHeight;
    }

    public boolean isOpen() {
        return OPEN_PANELS.contains(persistenceKey);
    }

    public void toggle() {
        if (!OPEN_PANELS.remove(persistenceKey)) {
            OPEN_PANELS.add(persistenceKey);
        }
        updateVisibility();
        onToggle.run();
    }

    /** Horizontal space reserved for the panel when open (gap included). */
    public int exclusiveWidth() {
        return isOpen() ? WIDTH + OFFSET : 0;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return isOpen()
                && mouseX >= panelX && mouseX < panelX + WIDTH
                && mouseY >= panelY && mouseY < panelY + panelHeight;
    }

    /**
     * Places the panel just to the right of the content area. When the screen expanded
     * {@code imageWidth} by {@link #exclusiveWidth()}, that rectangle sits inside the GUI
     * bounds JEI uses, so the overlay moves aside instead of covering the panel.
     */
    public List<AbstractWidget> layoutBeside(int screenWidth, int left, int containerWidth, int top) {
        int x = left + containerWidth + OFFSET;
        if (!isOpen() && x + WIDTH > screenWidth) {
            x = Math.max(0, left - WIDTH - OFFSET);
        }
        return layout(x, top);
    }

    /** Places every row and returns the widgets for the screen to register. */
    public List<AbstractWidget> layout(int x, int y) {
        panelX = x;
        panelY = y;
        widgets.clear();
        int cursor = y + PADDING + 9 + 4;
        for (Row row : rows) {
            row.top = cursor;
            row.build(this, widgets);
            cursor += row.height();
        }
        panelHeight = cursor - y + PADDING - 2;
        updateVisibility();
        return widgets;
    }

    /** Widgets always exist so clicks keep working; only their visibility follows the panel. */
    public void updateVisibility() {
        boolean open = isOpen();
        for (AbstractWidget widget : widgets) {
            widget.visible = open;
            widget.active = open;
        }
    }

    /** Call from {@code renderBg} so the row buttons draw on top of the frame. */
    public void render(GuiGraphics graphics, Font font) {
        if (!isOpen()) {
            return;
        }
        graphics.blitSprite(GuiSprites.SIDE_PANEL_SPRITE, panelX, panelY, WIDTH, panelHeight);
        graphics.drawString(font, Component.translatable("container.opencubes.side_config.title"),
                panelX + PADDING, panelY + PADDING, SideConfigScreenHelper.TEXT, false);
        for (Row row : rows) {
            row.renderLabel(this, graphics, font);
        }
    }

    public SideConfigButton createOpenButton(int x, int y) {
        return new SideConfigButton(x, y, this);
    }

    public MachineInfoButton createInfoButton(int x, int y, String machineKey) {
        return MachineInfoButton.forMachine(x, y, machineKey);
    }

    void send(int buttonId) {
        buttonSender.accept(buttonId);
    }

    private abstract static class Row {
        int top;

        abstract int height();

        abstract void build(SideConfigPanel panel, List<AbstractWidget> out);

        abstract void renderLabel(SideConfigPanel panel, GuiGraphics graphics, Font font);
    }

    /** A caption plus the six face toggles below it. */
    private static final class SideRow extends Row {

        private final Component label;
        private final int buttonBase;
        private final Predicate<Direction> enabled;
        private final int colour;

        private SideRow(Component label, int buttonBase, Predicate<Direction> enabled, int colour) {
            this.label = label;
            this.buttonBase = buttonBase;
            this.enabled = enabled;
            this.colour = colour;
        }

        @Override
        int height() {
            return 11 + BUTTON_SIZE + 6;
        }

        @Override
        void build(SideConfigPanel panel, List<AbstractWidget> out) {
            for (int i = 0; i < 6; i++) {
                final int index = i;
                Direction side = SideConfigScreenHelper.SIDES[i];
                Component face = Component.translatable(
                        "container.opencubes.side_config.face." + SideConfigScreenHelper.SIDE_KEYS[i]);
                Component tooltip = Component.translatable(
                        "container.opencubes.side_config.face_tooltip", label, face);
                out.add(new SideToggleButton(
                        panel.panelX + PADDING + i * BUTTON_STRIDE, top + 11, BUTTON_SIZE,
                        Component.literal(SideConfigScreenHelper.SIDE_LABELS[i]), tooltip,
                        () -> enabled.test(side), colour,
                        () -> panel.send(buttonBase + index)));
            }
        }

        @Override
        void renderLabel(SideConfigPanel panel, GuiGraphics graphics, Font font) {
            graphics.drawString(font, label, panel.panelX + PADDING, top,
                    SideConfigScreenHelper.TEXT, false);
        }
    }

    /** A single wide on/off row; the button paints its own caption. */
    private static final class OptionRow extends Row {

        private final Component label;
        private final Component tooltip;
        private final int buttonId;
        private final BooleanSupplier enabled;

        private OptionRow(Component label, Component tooltip, int buttonId, BooleanSupplier enabled) {
            this.label = label;
            this.tooltip = tooltip;
            this.buttonId = buttonId;
            this.enabled = enabled;
        }

        @Override
        int height() {
            return BUTTON_SIZE + 4;
        }

        @Override
        void build(SideConfigPanel panel, List<AbstractWidget> out) {
            out.add(new OptionToggleButton(panel.panelX + PADDING, top, ROW_WIDTH, BUTTON_SIZE,
                    label, tooltip, enabled, () -> panel.send(buttonId)));
        }

        @Override
        void renderLabel(SideConfigPanel panel, GuiGraphics graphics, Font font) {
            // The button draws its own label and state.
        }
    }

    private static final class ColourRow extends Row {

        private static final int SWATCH = 12;
        private static final int STRIDE = 13;
        private static final int COLS = 8;

        private final Component label;
        private final int buttonBase;
        private final IntSupplier selectedRgb;

        private ColourRow(Component label, int buttonBase, IntSupplier selectedRgb) {
            this.label = label;
            this.buttonBase = buttonBase;
            this.selectedRgb = selectedRgb;
        }

        @Override
        int height() {
            return 11 + SWATCH * 2 + 4;
        }

        @Override
        void build(SideConfigPanel panel, List<AbstractWidget> out) {
            DyeColor[] colours = DyeColor.values();
            for (int i = 0; i < colours.length; i++) {
                DyeColor colour = colours[i];
                int col = i % COLS;
                int row = i / COLS;
                int x = panel.panelX + PADDING + col * STRIDE;
                int y = top + 11 + row * (SWATCH + 1);
                out.add(new ColourSwatchButton(x, y, SWATCH, colour, selectedRgb,
                        () -> panel.send(buttonBase + colour.getId())));
            }
        }

        @Override
        void renderLabel(SideConfigPanel panel, GuiGraphics graphics, Font font) {
            graphics.drawString(font, label, panel.panelX + PADDING, top,
                    SideConfigScreenHelper.TEXT, false);
        }
    }

    private static final class ColourSwatchButton extends AbstractButton {

        private final DyeColor colour;
        private final IntSupplier selectedRgb;
        private final Runnable onClick;

        private ColourSwatchButton(int x, int y, int size, DyeColor colour, IntSupplier selectedRgb,
                                   Runnable onClick) {
            super(x, y, size, size, Component.translatable("color.minecraft." + colour.getName()));
            this.colour = colour;
            this.selectedRgb = selectedRgb;
            this.onClick = onClick;
            setTooltip(Tooltip.create(getMessage()));
        }

        @Override
        public void onPress() {
            onClick.run();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int rgb = 0xFF000000 | colour.getTextureDiffuseColor();
            graphics.fill(getX(), getY(), getX() + width, getY() + height, SideConfigScreenHelper.SLOT_SHADOW);
            graphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, rgb);
            boolean selected = (selectedRgb.getAsInt() & 0xFFFFFF)
                    == (colour.getTextureDiffuseColor() & 0xFFFFFF);
            if (selected || isHovered()) {
                graphics.renderOutline(getX(), getY(), width, height,
                        selected ? 0xFFFFFFFF : 0xFF8B8B8B);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
