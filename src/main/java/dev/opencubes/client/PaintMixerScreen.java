package dev.opencubes.client;

import dev.opencubes.client.sideconfig.MachineInfoButton;
import dev.opencubes.content.paint.PaintMixerBlockEntity;
import dev.opencubes.content.paint.PaintMixerMenu;
import dev.opencubes.network.SetPaintColorPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class PaintMixerScreen extends AbstractContainerScreen<PaintMixerMenu> {

    private static final int PANEL_DARK = SideConfigScreenHelper.SLOT_SHADOW;
    private static final int SLOT_BG = SideConfigScreenHelper.SLOT_BODY;
    private static final int TEXT = SideConfigScreenHelper.TEXT;

    private static final int[] SLOT_X = {8, 44, 62, 80, 98, 152};
    private static final int SLOT_Y = 28;

    private ChannelSlider red;
    private ChannelSlider green;
    private ChannelSlider blue;
    private int lastKnownColour = -1;

    public PaintMixerScreen(PaintMixerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 218;
        this.titleLabelY = 6;
        this.inventoryLabelY = 124;
    }

    @Override
    protected void init() {
        super.init();
        int colour = menu.getTargetColor();
        lastKnownColour = colour;

        red = addRenderableWidget(new ChannelSlider(leftPos + 8, topPos + 54, "R", (colour >> 16) & 0xFF));
        green = addRenderableWidget(new ChannelSlider(leftPos + 8, topPos + 68, "G", (colour >> 8) & 0xFF));
        blue = addRenderableWidget(new ChannelSlider(leftPos + 8, topPos + 82, "B", colour & 0xFF));

        addRenderableWidget(Button.builder(Component.translatable("opencubes.gui.paint_mixer.mix"), button ->
                        PacketDistributor.sendToServer(new SetPaintColorPayload(menu.pos(), pickedColour(), true)))
                .bounds(leftPos + 110, topPos + 96, 58, 16)
                .build());

        for (DyeColor dye : DyeColor.values()) {
            addRenderableWidget(new PresetSwatch(leftPos + 8 + dye.getId() * 10, topPos + 114, dye));
        }

        addRenderableWidget(MachineInfoButton.forMachine(leftPos + imageWidth - 22, topPos + 4, "paint_mixer"));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        int colour = menu.getTargetColor();
        if (colour != lastKnownColour) {
            lastKnownColour = colour;
            red.setChannel((colour >> 16) & 0xFF);
            green.setChannel((colour >> 8) & 0xFF);
            blue.setChannel(colour & 0xFF);
        }
    }

    private int pickedColour() {
        return (red.channel() << 16) | (green.channel() << 8) | blue.channel();
    }

    private void sendColour(int rgb) {
        lastKnownColour = rgb;
        PacketDistributor.sendToServer(new SetPaintColorPayload(menu.pos(), rgb, false));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        SideConfigScreenHelper.blitContainer(graphics, MachineGuiTextures.PAINT_MIXER,
                leftPos, topPos, imageWidth, imageHeight);

        int preview = pickedColour();
        graphics.fill(leftPos + 118, topPos + 26, leftPos + 146, topPos + 48, 0xFF000000 | preview);
        graphics.renderOutline(leftPos + 118, topPos + 26, 28, 22, PANEL_DARK);

        int progress = menu.getProgress();
        graphics.fill(leftPos + 8, topPos + 48, leftPos + 168, topPos + 52, SLOT_BG);
        if (progress > 0) {
            int filled = progress * 160 / PaintMixerBlockEntity.MIX_TIME;
            graphics.fill(leftPos + 8, topPos + 48, leftPos + 8 + filled, topPos + 52, 0xFF55FF55);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, TEXT, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT, false);

        // Captions sit under the title row so "Paint Mixer" never covers Milk / Ink / Can.
        graphics.drawString(font, Component.translatable("opencubes.gui.paint_mixer.slot.milk_short"),
                8, 18, TEXT, false);
        graphics.drawString(font, "C", 48, 18, TEXT, false);
        graphics.drawString(font, "M", 66, 18, TEXT, false);
        graphics.drawString(font, "Y", 84, 18, TEXT, false);
        graphics.drawString(font, "K", 102, 18, TEXT, false);
        graphics.drawString(font, Component.translatable("opencubes.gui.paint_mixer.slot.output_short"),
                148, 18, TEXT, false);

        graphics.drawString(font, String.format("#%06X", pickedColour()), 8, 100, TEXT, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderSlotHelp(graphics, mouseX, mouseY);
    }

    private void renderSlotHelp(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isHovering(SLOT_X[0], SLOT_Y, 16, 16, mouseX, mouseY) && menu.getSlot(0).getItem().isEmpty()) {
            graphics.renderTooltip(font,
                    Component.translatable("opencubes.gui.paint_mixer.slot.milk"), mouseX, mouseY);
        } else if (isHovering(SLOT_X[1], SLOT_Y, 16, 16, mouseX, mouseY) && menu.getSlot(1).getItem().isEmpty()) {
            graphics.renderTooltip(font,
                    Component.translatable("opencubes.gui.paint_mixer.slot.cyan"), mouseX, mouseY);
        } else if (isHovering(SLOT_X[2], SLOT_Y, 16, 16, mouseX, mouseY) && menu.getSlot(2).getItem().isEmpty()) {
            graphics.renderTooltip(font,
                    Component.translatable("opencubes.gui.paint_mixer.slot.magenta"), mouseX, mouseY);
        } else if (isHovering(SLOT_X[3], SLOT_Y, 16, 16, mouseX, mouseY) && menu.getSlot(3).getItem().isEmpty()) {
            graphics.renderTooltip(font,
                    Component.translatable("opencubes.gui.paint_mixer.slot.yellow"), mouseX, mouseY);
        } else if (isHovering(SLOT_X[4], SLOT_Y, 16, 16, mouseX, mouseY) && menu.getSlot(4).getItem().isEmpty()) {
            graphics.renderTooltip(font,
                    Component.translatable("opencubes.gui.paint_mixer.slot.black"), mouseX, mouseY);
        } else if (isHovering(SLOT_X[5], SLOT_Y, 16, 16, mouseX, mouseY) && menu.getSlot(5).getItem().isEmpty()) {
            graphics.renderTooltip(font,
                    Component.translatable("opencubes.gui.paint_mixer.slot.output"), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        ChannelSlider hovered = sliderAt(mouseX, mouseY);
        if (hovered != null && scrollY != 0.0D) {
            int delta = scrollY > 0.0D ? 1 : -1;
            if (hasShiftDown()) {
                delta *= 8;
            }
            hovered.nudge(delta);
            sendColour(pickedColour());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Nullable
    private ChannelSlider sliderAt(double mouseX, double mouseY) {
        if (red != null && red.isMouseOver(mouseX, mouseY)) {
            return red;
        }
        if (green != null && green.isMouseOver(mouseX, mouseY)) {
            return green;
        }
        if (blue != null && blue.isMouseOver(mouseX, mouseY)) {
            return blue;
        }
        return null;
    }

    private final class ChannelSlider extends AbstractSliderButton {

        private final String channelName;

        private ChannelSlider(int x, int y, String channelName, int initial) {
            super(x, y, 160, 12, Component.empty(), initial / 255.0D);
            this.channelName = channelName;
            updateMessage();
        }

        private int channel() {
            return (int) Math.round(value * 255.0D);
        }

        private void setChannel(int amount) {
            value = Mth.clamp(amount, 0, 255) / 255.0D;
            updateMessage();
        }

        private void nudge(int delta) {
            setChannel(channel() + delta);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(channelName + ": " + channel()));
        }

        @Override
        protected void applyValue() {
            // Committed on release so dragging does not flood the server.
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            super.onRelease(mouseX, mouseY);
            sendColour(pickedColour());
        }
    }

    private final class PresetSwatch extends AbstractButton {

        private final DyeColor dye;

        private PresetSwatch(int x, int y, DyeColor dye) {
            super(x, y, 10, 10, Component.translatable("color.minecraft." + dye.getName()));
            this.dye = dye;
            setTooltip(Tooltip.create(getMessage()));
        }

        @Override
        public void onPress() {
            int rgb = dye.getTextureDiffuseColor() & 0xFFFFFF;
            red.setChannel((rgb >> 16) & 0xFF);
            green.setChannel((rgb >> 8) & 0xFF);
            blue.setChannel(rgb & 0xFF);
            sendColour(rgb);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, PANEL_DARK);
            graphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1,
                    0xFF000000 | dye.getTextureDiffuseColor());
            if (isHovered()) {
                graphics.renderOutline(getX(), getY(), width, height, 0xFFFFFFFF);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
