package dev.opencubes.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/** Dispenser-style nine-slot machines backed by an editable PNG in {@code textures/gui/}. */
public class DispenserLikeScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    private final Identifier texture;

    public DispenserLikeScreen(T menu, Inventory inventory, Component title, Identifier texture) {
        super(menu, inventory, title);
        this.texture = texture;
    }

    protected DispenserLikeScreen(T menu, Inventory inventory, Component title) {
        this(menu, inventory, title, MachineGuiTextures.DISPENSER);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        SideConfigScreenHelper.blitContainer(graphics, texture, leftPos, topPos, imageWidth, imageHeight);
    }
}
