package dev.opencubes.client;

import dev.opencubes.content.luggage.LuggageMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class LuggageScreen extends AbstractContainerScreen<LuggageMenu> {

    public LuggageScreen(LuggageMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 114 + menu.luggageSlots() / 9 * 18);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = leftPos;
        int y = topPos;
        int rows = menu.luggageSlots() / 9;
        graphics.blit(RenderPipelines.GUI_TEXTURED, MachineGuiTextures.LUGGAGE, x, y, 0.0F, 0.0F,
                imageWidth, rows * 18 + 17, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, MachineGuiTextures.LUGGAGE, x, y + rows * 18 + 17, 0.0F, 126.0F,
                imageWidth, 96, 256, 256);
    }
}
