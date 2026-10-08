package dev.opencubes.client.heightmap;

import dev.opencubes.client.MachineGuiTextures;
import dev.opencubes.client.SideConfigScreenHelper;
import dev.opencubes.client.sideconfig.MachineInfoButton;
import dev.opencubes.content.heightmap.HeightMapProjectorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class HeightMapProjectorScreen extends AbstractContainerScreen<HeightMapProjectorMenu> {

    public HeightMapProjectorScreen(HeightMapProjectorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                        HeightMapProjectorMenu.BUTTON_ROTATE_LEFT);
            }
        }).bounds(leftPos + 26, topPos + 34, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                        HeightMapProjectorMenu.BUTTON_ROTATE_RIGHT);
            }
        }).bounds(leftPos + 130, topPos + 34, 20, 20).build());
        addRenderableWidget(MachineInfoButton.forMachine(leftPos + imageWidth - 22, topPos + 4,
                "height_map_projector"));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        SideConfigScreenHelper.blitContainer(graphics, MachineGuiTextures.HEIGHT_MAP_PROJECTOR,
                leftPos, topPos, imageWidth, imageHeight);
    }
}
