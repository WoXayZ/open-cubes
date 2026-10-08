package dev.opencubes.client;

import dev.opencubes.client.sideconfig.SideConfigPanel;
import dev.opencubes.content.automation.AutoEnchantmentTableMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Auto-enchanting UI: XP gauge left, L1/Cap/Shelves + Next/-/+ above the
 * tool / lapis / arrow / output row, side-config on the right.
 */
public class AutoEnchantmentTableScreen extends AbstractContainerScreen<AutoEnchantmentTableMenu> {

    private static final int CONTENT_WIDTH = 176;

    private SideConfigPanel panel;

    public AutoEnchantmentTableScreen(AutoEnchantmentTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, CONTENT_WIDTH + SideConfigPanel.exclusiveWidth(AutoEnchantmentTableMenu.class), 166);
    }

    @Override
    public int getImageWidth() {
        return CONTENT_WIDTH + SideConfigPanel.exclusiveWidth(AutoEnchantmentTableMenu.class);
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - getImageWidth()) / 2;
        int x = leftPos;
        int y = topPos;

        // Controls sit above the slot row and left of the top-right i / gear buttons.
        Button cycle = Button.builder(Component.translatable("container.opencubes.auto_enchanting_table.next"),
                        b -> press(AutoEnchantmentTableMenu.BUTTON_CYCLE_LEVEL))
                .bounds(x + 88, y + 16, 40, 16).build();
        cycle.setTooltip(Tooltip.create(
                Component.translatable("container.opencubes.auto_enchanting_table.next.tip")));
        addRenderableWidget(cycle);

        Button down = Button.builder(Component.literal("-"),
                        b -> press(AutoEnchantmentTableMenu.BUTTON_POWER_DOWN))
                .bounds(x + 88, y + 34, 16, 16).build();
        down.setTooltip(Tooltip.create(
                Component.translatable("container.opencubes.auto_enchanting_table.cap.tip")));
        addRenderableWidget(down);

        Button up = Button.builder(Component.literal("+"),
                        b -> press(AutoEnchantmentTableMenu.BUTTON_POWER_UP))
                .bounds(x + 106, y + 34, 16, 16).build();
        up.setTooltip(Tooltip.create(
                Component.translatable("container.opencubes.auto_enchanting_table.cap.tip")));
        addRenderableWidget(up);

        panel = new SideConfigPanel(AutoEnchantmentTableMenu.class,
                id -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id))
                .onToggle(this::rebuildWidgets)
                .addItemRow(Component.translatable("container.opencubes.side_config.row.item_in"),
                        AutoEnchantmentTableMenu.ITEM_INPUT_BUTTONS, menu::isItemInputSide)
                .addItemRow(Component.translatable("container.opencubes.side_config.row.item_out"),
                        AutoEnchantmentTableMenu.ITEM_OUTPUT_BUTTONS, menu::isItemOutputSide)
                .addXpRow(Component.translatable("container.opencubes.side_config.row.xp_in"),
                        AutoEnchantmentTableMenu.XP_SIDE_BUTTONS, menu::isXpSide)
                .addOptionRow(Component.translatable("container.opencubes.side_config.auto_pull"),
                        Component.translatable("container.opencubes.side_config.auto_pull.tip"),
                        AutoEnchantmentTableMenu.BUTTON_AUTO_PULL, menu::isAutoPull)
                .addOptionRow(Component.translatable("container.opencubes.side_config.auto_push"),
                        Component.translatable("container.opencubes.side_config.auto_push.tip"),
                        AutoEnchantmentTableMenu.BUTTON_AUTO_PUSH, menu::isAutoPush)
                .addOptionRow(Component.translatable("container.opencubes.side_config.auto_xp"),
                        Component.translatable("container.opencubes.side_config.auto_xp.tip"),
                        AutoEnchantmentTableMenu.BUTTON_AUTO_XP, menu::isAutoXp);
        for (var widget : panel.layoutBeside(width, leftPos, CONTENT_WIDTH, topPos)) {
            addRenderableWidget(widget);
        }
        addRenderableWidget(panel.createInfoButton(leftPos + CONTENT_WIDTH - 44, topPos + 4,
                "auto_enchanting_table"));
        addRenderableWidget(panel.createOpenButton(leftPos + CONTENT_WIDTH - 24, topPos + 4));
    }

    private void press(int buttonId) {
        if (minecraft == null || minecraft.gameMode == null) {
            return;
        }
        int id = buttonId;
        if (minecraft.hasShiftDown() && (buttonId == AutoEnchantmentTableMenu.BUTTON_POWER_DOWN
                || buttonId == AutoEnchantmentTableMenu.BUTTON_POWER_UP)) {
            id += AutoEnchantmentTableMenu.SHIFT_OFFSET;
        }
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = leftPos;
        int y = topPos;
        SideConfigScreenHelper.blitContainer(graphics, MachineGuiTextures.AUTO_ENCHANTMENT_TABLE,
                x, y, CONTENT_WIDTH, imageHeight);
        SideConfigScreenHelper.drawFluidGauge(graphics, x + 7, y + 15, 10, 48,
                menu.getFluidAmount(), menu.getFluidCapacity());

        AutoAnvilScreen.drawProgressArrow(graphics, x + 96, y + 57,
                menu.getProgress(), menu.getMaxProgress());

        panel.render(graphics, font);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, SideConfigScreenHelper.TEXT, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                SideConfigScreenHelper.TEXT, false);
        graphics.text(font,
                Component.translatable("container.opencubes.auto_enchanting_table.slot",
                        menu.getSelectedLevel()),
                24, 18, SideConfigScreenHelper.TEXT, false);
        graphics.text(font,
                Component.translatable("container.opencubes.auto_enchanting_table.cap",
                        menu.getPowerLimit()),
                24, 34, SideConfigScreenHelper.TEXT, false);
        graphics.text(font,
                Component.translatable("container.opencubes.auto_enchanting_table.shelves",
                        menu.getAvailablePower()),
                24, 42, SideConfigScreenHelper.TEXT, false);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop) {
        boolean outside = mouseX < guiLeft || mouseY < guiTop
                || mouseX >= guiLeft + getImageWidth() || mouseY >= guiTop + imageHeight;
        return outside && !panel.isMouseOver(mouseX, mouseY);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (isHovering(7, 15, 12, 50, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(font,
                    Component.translatable("container.opencubes.xp_bottler.fluid",
                            menu.getFluidAmount(), menu.getFluidCapacity()),
                    mouseX, mouseY);
            return;
        }
        super.extractTooltip(graphics, mouseX, mouseY);
    }
}
