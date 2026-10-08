package dev.opencubes.client.sideconfig;

import dev.opencubes.client.GuiSprites;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Small "i" button next to the side-config toggle. Sprite:
 * {@code gui/sprites/widget/info*.png}.
 */
public class MachineInfoButton extends AbstractButton {

    public static final int SIZE = 16;

    public MachineInfoButton(int x, int y, Component tooltip) {
        super(x, y, SIZE, SIZE, Component.translatable("container.opencubes.side_config.info"));
        setTooltip(Tooltip.create(tooltip));
    }

    public static MachineInfoButton forMachine(int x, int y, String machineKey) {
        return new MachineInfoButton(x, y, Component.translatable("container.opencubes." + machineKey + ".info"));
    }

    @Override
    public void onPress(InputWithModifiers input) {
        // Tooltip-only control.
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        GuiSprites.blit(graphics,
                isHoveredOrFocused() ? GuiSprites.INFO_HIGHLIGHTED : GuiSprites.INFO,
                getX(), getY(), width, height);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
