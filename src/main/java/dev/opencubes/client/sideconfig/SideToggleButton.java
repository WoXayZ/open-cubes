package dev.opencubes.client.sideconfig;

import dev.opencubes.client.GuiSprites;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Square toggle for one block face. Uses editable sprites under {@code gui/sprites/side_config/}.
 */
public class SideToggleButton extends AbstractButton {

    private final BooleanSupplier state;
    private final int onColour;
    private final Runnable action;

    public SideToggleButton(int x, int y, int size, Component label, Component tooltip,
                            BooleanSupplier state, int onColour, Runnable action) {
        super(x, y, size, size, label);
        this.state = state;
        this.onColour = onColour;
        this.action = action;
        setTooltip(Tooltip.create(tooltip));
    }

    @Override
    public void onPress(InputWithModifiers input) {
        action.run();
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        boolean on = state.getAsBoolean();
        int x = getX();
        int y = getY();
        if (on) {
            int colour = isHoveredOrFocused()
                    ? lighten(onColour, 36)
                    : onColour;
            GuiSprites.blitTinted(graphics, GuiSprites.FACE, x, y, width, height, colour);
        } else {
            GuiSprites.blit(graphics,
                    isHoveredOrFocused() ? GuiSprites.BUTTON_HIGHLIGHTED : GuiSprites.FACE_OFF,
                    x, y, width, height);
        }
        graphics.centeredText(Minecraft.getInstance().font, getMessage(),
                x + width / 2, y + (height - 8) / 2, on ? 0xFFFFFFFF : 0xFFB0B0B0);
    }

    private static int lighten(int colour, int amount) {
        int r = Math.min(255, ((colour >> 16) & 0xFF) + amount);
        int g = Math.min(255, ((colour >> 8) & 0xFF) + amount);
        int b = Math.min(255, (colour & 0xFF) + amount);
        return (colour & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
