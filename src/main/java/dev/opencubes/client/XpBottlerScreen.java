package dev.opencubes.client;

import dev.opencubes.client.sideconfig.SideConfigPanel;
import dev.opencubes.content.xp.XpBottlerBlockEntity;
import dev.opencubes.content.xp.XpBottlerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class XpBottlerScreen extends AbstractContainerScreen<XpBottlerMenu> {

    private static final int CONTENT_WIDTH = 176;

    private SideConfigPanel panel;

    public XpBottlerScreen(XpBottlerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, CONTENT_WIDTH + SideConfigPanel.exclusiveWidth(XpBottlerMenu.class), 166);
    }

    @Override
    public int getImageWidth() {
        return CONTENT_WIDTH + SideConfigPanel.exclusiveWidth(XpBottlerMenu.class);
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - getImageWidth()) / 2;
        panel = new SideConfigPanel(XpBottlerMenu.class,
                id -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id))
                .onToggle(this::rebuildWidgets)
                .addItemRow(Component.translatable("container.opencubes.side_config.row.item_in"),
                        XpBottlerMenu.ITEM_INPUT_BUTTONS, menu::isItemInputSide)
                .addItemRow(Component.translatable("container.opencubes.side_config.row.item_out"),
                        XpBottlerMenu.ITEM_OUTPUT_BUTTONS, menu::isItemOutputSide)
                .addXpRow(Component.translatable("container.opencubes.side_config.row.xp_in"),
                        XpBottlerMenu.XP_SIDE_BUTTONS, menu::isXpSide)
                .addOptionRow(Component.translatable("container.opencubes.side_config.auto_pull"),
                        Component.translatable("container.opencubes.side_config.auto_pull.tip"),
                        XpBottlerMenu.BUTTON_AUTO_PULL, menu::isAutoPull)
                .addOptionRow(Component.translatable("container.opencubes.side_config.auto_push"),
                        Component.translatable("container.opencubes.side_config.auto_push.tip"),
                        XpBottlerMenu.BUTTON_AUTO_PUSH, menu::isAutoPush)
                .addOptionRow(Component.translatable("container.opencubes.side_config.auto_xp"),
                        Component.translatable("container.opencubes.side_config.auto_xp.tip"),
                        XpBottlerMenu.BUTTON_AUTO_XP, menu::isAutoXp);
        for (var widget : panel.layoutBeside(width, leftPos, CONTENT_WIDTH, topPos)) {
            addRenderableWidget(widget);
        }
        addRenderableWidget(panel.createInfoButton(leftPos + CONTENT_WIDTH - 44, topPos + 4, "xp_bottler"));
        addRenderableWidget(panel.createOpenButton(leftPos + CONTENT_WIDTH - 24, topPos + 4));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = leftPos;
        int y = topPos;
        SideConfigScreenHelper.blitContainer(graphics, MachineGuiTextures.XP_BOTTLER, x, y, CONTENT_WIDTH, imageHeight);
        SideConfigScreenHelper.drawFluidGauge(graphics, x + 8, y + 17, 10, 52,
                menu.getFluidAmount(), menu.getFluidCapacity());

        graphics.fill(x + 79, y + 38, x + 103, y + 46, SideConfigScreenHelper.SLOT_BODY);
        graphics.outline(x + 78, y + 37, 26, 10, SideConfigScreenHelper.SLOT_SHADOW);
        int progress = menu.getProgress();
        if (progress > 0) {
            int filled = Math.min(24, progress * 24 / XpBottlerBlockEntity.PROGRESS_TICKS);
            graphics.fill(x + 79, y + 38, x + 79 + filled, y + 46, 0xFF54D45B);
        }

        panel.render(graphics, font);
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
