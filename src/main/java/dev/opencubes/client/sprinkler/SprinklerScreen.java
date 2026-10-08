package dev.opencubes.client.sprinkler;

import dev.opencubes.client.MachineGuiTextures;
import dev.opencubes.client.SideConfigScreenHelper;
import dev.opencubes.client.sideconfig.MachineInfoButton;
import dev.opencubes.content.sprinkler.SprinklerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Dispenser-style 3x3 + player inventory, with a water gauge on the right of the grid.
 * Slot frames are drawn to match {@link SprinklerMenu} coordinates exactly.
 */
public class SprinklerScreen extends AbstractContainerScreen<SprinklerMenu> {

    private static final int BAR_X = 133;
    private static final int BAR_Y = 17;
    private static final int BAR_WIDTH = 10;
    private static final int BAR_HEIGHT = 52;

    public SprinklerScreen(SprinklerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(MachineInfoButton.forMachine(leftPos + imageWidth - 22, topPos + 4, "sprinkler"));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = leftPos;
        int y = topPos;
        SideConfigScreenHelper.blitContainer(graphics, MachineGuiTextures.SPRINKLER, x, y, imageWidth, imageHeight);
        SideConfigScreenHelper.drawWaterFluidGauge(graphics, x + BAR_X, y + BAR_Y, BAR_WIDTH, BAR_HEIGHT,
                menu.fluidAmount(), menu.fluidCapacity());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (isHovering(BAR_X - 1, BAR_Y - 1, BAR_WIDTH + 2, BAR_HEIGHT + 2, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(font,
                    Component.translatable("container.opencubes.sprinkler.water",
                            menu.fluidAmount(), menu.fluidCapacity()),
                    mouseX, mouseY);
            return;
        }
        super.extractTooltip(graphics, mouseX, mouseY);
    }
}
