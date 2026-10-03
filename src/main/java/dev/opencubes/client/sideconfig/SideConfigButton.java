package dev.opencubes.client.sideconfig;

import dev.opencubes.client.GuiSprites;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Small square button that slides the side config panel. Sprite:
 * {@code gui/sprites/widget/config*.png}.
 */
public class SideConfigButton extends AbstractButton {

    public static final int SIZE = 16;

    private final SideConfigPanel panel;

    public SideConfigButton(int x, int y, SideConfigPanel panel) {
        super(x, y, SIZE, SIZE, Component.translatable("container.opencubes.side_config.toggle"));
        this.panel = panel;
        setTooltip(Tooltip.create(getMessage()));
    }

    @Override
    public void onPress() {
        panel.toggle();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var texture = panel.isOpen() ? GuiSprites.CONFIG_OPEN : GuiSprites.CONFIG;
        if (isHoveredOrFocused()) {
            texture = GuiSprites.CONFIG_HIGHLIGHTED;
        }
        GuiSprites.blit(graphics, texture, getX(), getY(), width, height);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
