package dev.opencubes.client;

import dev.opencubes.client.sideconfig.SideConfigPanel;
import dev.opencubes.content.automation.AutoAnvilMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AutoAnvilScreen extends AbstractContainerScreen<AutoAnvilMenu> {

    private static final int CONTENT_WIDTH = 176;

    private SideConfigPanel panel;

    public AutoAnvilScreen(AutoAnvilMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, CONTENT_WIDTH + SideConfigPanel.exclusiveWidth(AutoAnvilMenu.class), 166);
    }

    @Override
    public int getImageWidth() {
        return CONTENT_WIDTH + SideConfigPanel.exclusiveWidth(AutoAnvilMenu.class);
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - getImageWidth()) / 2;
        panel = new SideConfigPanel(AutoAnvilMenu.class,
                id -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id))
                .onToggle(this::rebuildWidgets)
                .addItemRow(Component.translatable("container.opencubes.side_config.row.item_in"),
                        AutoAnvilMenu.ITEM_INPUT_BUTTONS, menu::isItemInputSide)
                .addItemRow(Component.translatable("container.opencubes.side_config.row.item_out"),
                        AutoAnvilMenu.ITEM_OUTPUT_BUTTONS, menu::isItemOutputSide)
                .addXpRow(Component.translatable("container.opencubes.side_config.row.xp_in"),
                        AutoAnvilMenu.XP_SIDE_BUTTONS, menu::isXpSide)
                .addOptionRow(Component.translatable("container.opencubes.side_config.auto_pull"),
                        Component.translatable("container.opencubes.side_config.auto_pull.tip"),
                        AutoAnvilMenu.BUTTON_AUTO_PULL, menu::isAutoPull)
                .addOptionRow(Component.translatable("container.opencubes.side_config.auto_push"),
                        Component.translatable("container.opencubes.side_config.auto_push.tip"),
                        AutoAnvilMenu.BUTTON_AUTO_PUSH, menu::isAutoPush)
                .addOptionRow(Component.translatable("container.opencubes.side_config.auto_xp"),
                        Component.translatable("container.opencubes.side_config.auto_xp.tip"),
                        AutoAnvilMenu.BUTTON_AUTO_XP, menu::isAutoXp);
        for (var widget : panel.layoutBeside(width, leftPos, CONTENT_WIDTH, topPos)) {
            addRenderableWidget(widget);
        }
        addRenderableWidget(panel.createInfoButton(leftPos + CONTENT_WIDTH - 44, topPos + 4, "auto_anvil"));
        addRenderableWidget(panel.createOpenButton(leftPos + CONTENT_WIDTH - 24, topPos + 4));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = leftPos;
        int y = topPos;
        SideConfigScreenHelper.blitContainer(graphics, MachineGuiTextures.AUTO_ANVIL, x, y, CONTENT_WIDTH, imageHeight);
        SideConfigScreenHelper.drawFluidGauge(graphics, x + 8, y + 17, 10, 52,
                menu.getFluidAmount(), menu.getFluidCapacity());

        graphics.text(font, "+", x + 58, y + 51, SideConfigScreenHelper.TEXT, false);
        drawProgressArrow(graphics, x + 100, y + 51, menu.getProgress(), menu.getMaxProgress());

        panel.render(graphics, font);
    }

    static void drawProgressArrow(GuiGraphicsExtractor graphics, int x, int y, int progress, int maxProgress) {
        graphics.fill(x, y + 2, x + 20, y + 6, SideConfigScreenHelper.SLOT_SHADOW);
        for (int i = 0; i < 5; i++) {
            graphics.fill(x + 20 + i, y + i, x + 21 + i, y + 9 - i, SideConfigScreenHelper.SLOT_SHADOW);
        }
        if (progress > 0 && maxProgress > 0) {
            int filled = Math.min(24, progress * 24 / maxProgress);
            graphics.fill(x, y + 2, x + Math.min(20, filled), y + 6, 0xFF54D45B);
            if (filled > 20) {
                int tip = filled - 20;
                for (int i = 0; i < tip && i < 5; i++) {
                    graphics.fill(x + 20 + i, y + i, x + 21 + i, y + 9 - i, 0xFF54D45B);
                }
            }
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop) {
        boolean outside = mouseX < guiLeft || mouseY < guiTop
                || mouseX >= guiLeft + getImageWidth() || mouseY >= guiTop + imageHeight;
        return outside && !panel.isMouseOver(mouseX, mouseY);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (isHovering(7, 16, 12, 54, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(font,
                    Component.translatable("container.opencubes.xp_bottler.fluid",
                            menu.getFluidAmount(), menu.getFluidCapacity()),
                    mouseX, mouseY);
            return;
        }
        super.extractTooltip(graphics, mouseX, mouseY);
    }
}
