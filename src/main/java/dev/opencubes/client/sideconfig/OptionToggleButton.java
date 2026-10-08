package dev.opencubes.client.sideconfig;

import dev.opencubes.client.GuiSprites;
import dev.opencubes.client.SideConfigScreenHelper;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Wide on/off row drawn from {@code gui/sprites/side_config/option*.png}.
 */
public class OptionToggleButton extends AbstractButton {

    private final BooleanSupplier state;
    private final Runnable action;

    public OptionToggleButton(int x, int y, int width, int height, Component label,
                              @Nullable Component tooltip, BooleanSupplier state, Runnable action) {
        super(x, y, width, height, label);
        this.state = state;
        this.action = action;
        if (tooltip != null) {
            setTooltip(Tooltip.create(tooltip));
        }
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
        var texture = on ? GuiSprites.OPTION_ON : GuiSprites.OPTION;
        // Draw at native sprite size so the green/grey bevel stays crisp; centre within the hitbox.
        int drawW = Math.min(width, GuiSprites.OPTION_WIDTH);
        int drawH = Math.min(height, GuiSprites.OPTION_HEIGHT);
        int drawX = x + (width - drawW) / 2;
        int drawY = y + (height - drawH) / 2;
        if (isHoveredOrFocused()) {
            GuiSprites.blitTintedStretched(graphics, texture, drawX, drawY, drawW, drawH,
                    GuiSprites.OPTION_WIDTH, GuiSprites.OPTION_HEIGHT, 0xFFE8E8E8);
        } else {
            GuiSprites.blitStretched(graphics, texture, drawX, drawY, drawW, drawH,
                    GuiSprites.OPTION_WIDTH, GuiSprites.OPTION_HEIGHT);
        }

        Font font = Minecraft.getInstance().font;
        Component stateText = Component.translatable(on
                ? "container.opencubes.side_config.on"
                : "container.opencubes.side_config.off");
        int stateWidth = font.width(stateText);
        int textY = drawY + (drawH - font.lineHeight) / 2 + 1;
        String label = SideConfigScreenHelper.truncate(font, getMessage().getString(),
                drawW - stateWidth - 12);
        graphics.text(font, label, drawX + 4, textY, 0xFFFFFFFF);
        graphics.text(font, stateText, drawX + drawW - 4 - stateWidth, textY,
                on ? 0xFF7CE87C : 0xFFD8D8D8);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
