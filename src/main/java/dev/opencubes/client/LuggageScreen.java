package dev.opencubes.client;



import dev.opencubes.content.luggage.LuggageMenu;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import net.minecraft.network.chat.Component;

import net.minecraft.world.entity.player.Inventory;



public class LuggageScreen extends AbstractContainerScreen<LuggageMenu> {



    public LuggageScreen(LuggageMenu menu, Inventory inv, Component title) {

        super(menu, inv, title);

        int rows = menu.luggageSlots() / 9;

        this.imageHeight = 114 + rows * 18;

        this.inventoryLabelY = this.imageHeight - 94;

    }



    @Override

    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {

        int x = leftPos;

        int y = topPos;

        int rows = menu.luggageSlots() / 9;

        graphics.blit(MachineGuiTextures.LUGGAGE, x, y, 0, 0, imageWidth, rows * 18 + 17);

        graphics.blit(MachineGuiTextures.LUGGAGE, x, y + rows * 18 + 17, 0, 126, imageWidth, 96);

    }



    @Override

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

        renderBackground(graphics, mouseX, mouseY, partialTick);

        super.render(graphics, mouseX, mouseY, partialTick);

        renderTooltip(graphics, mouseX, mouseY);

    }

}


