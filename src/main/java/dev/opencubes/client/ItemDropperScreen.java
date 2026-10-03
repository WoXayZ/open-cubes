package dev.opencubes.client;

import dev.opencubes.client.sideconfig.MachineInfoButton;
import dev.opencubes.client.sideconfig.OptionToggleButton;
import dev.opencubes.content.automation.ItemDropperMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Dispenser layout with throw speed on the left of the grid and redstone scaling on the right.
 */
public class ItemDropperScreen extends DispenserLikeScreen<ItemDropperMenu> {

    public ItemDropperScreen(ItemDropperMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, MachineGuiTextures.ITEM_DROPPER);
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos;
        int y = topPos;

        Button slower = Button.builder(Component.literal("-"),
                        b -> press(ItemDropperMenu.BUTTON_SPEED_DOWN))
                .bounds(x + 8, y + 34, 16, 16).build();
        slower.setTooltip(Tooltip.create(Component.translatable("container.opencubes.item_dropper.speed_tip")));
        addRenderableWidget(slower);

        Button faster = Button.builder(Component.literal("+"),
                        b -> press(ItemDropperMenu.BUTTON_SPEED_UP))
                .bounds(x + 40, y + 34, 16, 16).build();
        faster.setTooltip(Tooltip.create(Component.translatable("container.opencubes.item_dropper.speed_tip")));
        addRenderableWidget(faster);

        addRenderableWidget(new OptionToggleButton(x + 122, y + 34, 46, 16,
                Component.translatable("container.opencubes.item_dropper.redstone_short"),
                Component.translatable("container.opencubes.item_dropper.redstone_tip"),
                menu::usesRedstoneStrength,
                () -> press(ItemDropperMenu.BUTTON_TOGGLE_REDSTONE)));

        addRenderableWidget(MachineInfoButton.forMachine(x + imageWidth - 22, y + 4, "item_dropper"));
    }

    private void press(int buttonId) {
        if (minecraft == null || minecraft.gameMode == null) {
            return;
        }
        int id = buttonId;
        if (hasShiftDown() && (buttonId == ItemDropperMenu.BUTTON_SPEED_DOWN
                || buttonId == ItemDropperMenu.BUTTON_SPEED_UP)) {
            id += ItemDropperMenu.SHIFT_OFFSET;
        }
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, Component.translatable("container.opencubes.item_dropper.speed_label"),
                10, 22, SideConfigScreenHelper.TEXT, false);
        String speed = String.format("%.2f", menu.getItemSpeed());
        graphics.drawString(font, speed, 32 - font.width(speed) / 2, 52,
                SideConfigScreenHelper.TEXT, false);
        graphics.drawString(font, Component.translatable("container.opencubes.item_dropper.redstone_label"),
                124, 22, SideConfigScreenHelper.TEXT, false);
    }
}
