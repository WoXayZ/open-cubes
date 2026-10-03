package dev.opencubes.client;

import dev.opencubes.client.sideconfig.MachineInfoButton;
import dev.opencubes.content.automation.BlockPlacerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BlockPlacerScreen extends DispenserLikeScreen<BlockPlacerMenu> {

    public BlockPlacerScreen(BlockPlacerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, MachineGuiTextures.BLOCK_PLACER);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(MachineInfoButton.forMachine(leftPos + imageWidth - 22, topPos + 4, "block_placer"));
    }
}
