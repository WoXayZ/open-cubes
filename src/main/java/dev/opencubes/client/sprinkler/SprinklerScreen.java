package dev.opencubes.client.sprinkler;

import dev.opencubes.client.MachineGuiTextures;
import dev.opencubes.client.SideConfigScreenHelper;
import dev.opencubes.client.sideconfig.MachineInfoButton;
import dev.opencubes.content.sprinkler.SprinklerMenu;
import net.minecraft.client.gui.GuiGraphics;
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
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
        super.init();
        addRenderableWidget(MachineInfoButton.forMachine(leftPos + imageWidth - 22, topPos + 4, "sprinkler"));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        SideConfigScreenHelper.blitContainer(graphics, MachineGuiTextures.SPRINKLER, x, y, imageWidth, imageHeight);
        SideConfigScreenHelper.drawWaterFluidGauge(graphics, x + BAR_X, y + BAR_Y, BAR_WIDTH, BAR_HEIGHT,
                menu.fluidAmount(), menu.fluidCapacity());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(BAR_X - 1, BAR_Y - 1, BAR_WIDTH + 2, BAR_HEIGHT + 2, mouseX, mouseY)) {
            graphics.renderTooltip(font,
                    Component.translatable("container.opencubes.sprinkler.water",
                            menu.fluidAmount(), menu.fluidCapacity()),
                    mouseX, mouseY);
        }
    }
}
