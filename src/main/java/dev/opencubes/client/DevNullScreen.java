package dev.opencubes.client;

import dev.opencubes.content.devnull.DevNullMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DevNullScreen extends AbstractContainerScreen<DevNullMenu> {

    public DevNullScreen(DevNullMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        SideConfigScreenHelper.blitContainer(graphics, MachineGuiTextures.DEV_NULL,
                leftPos, topPos, imageWidth, imageHeight);
    }
}
