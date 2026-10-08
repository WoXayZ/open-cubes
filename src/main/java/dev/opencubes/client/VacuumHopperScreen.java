package dev.opencubes.client;

import dev.opencubes.client.sideconfig.SideConfigPanel;
import dev.opencubes.content.automation.VacuumHopperMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Ten buffered slots, the liquid XP gauge and a readout of the suction state, which is otherwise
 * only visible from the sneak-click that toggles it.
 */
public class VacuumHopperScreen extends AbstractContainerScreen<VacuumHopperMenu> {

    private static final int CONTENT_WIDTH = 176;

    private SideConfigPanel panel;

    public VacuumHopperScreen(VacuumHopperMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, CONTENT_WIDTH + SideConfigPanel.exclusiveWidth(VacuumHopperMenu.class), 166);
    }

    @Override
    public int getImageWidth() {
        return CONTENT_WIDTH + SideConfigPanel.exclusiveWidth(VacuumHopperMenu.class);
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - getImageWidth()) / 2;
        panel = new SideConfigPanel(VacuumHopperMenu.class,
                id -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id))
                .onToggle(this::rebuildWidgets)
                .addItemRow(Component.translatable("container.opencubes.side_config.row.item_out"),
                        VacuumHopperMenu.ITEM_SIDE_BUTTONS, menu::isItemSide)
                .addXpRow(Component.translatable("container.opencubes.side_config.row.xp_out"),
                        VacuumHopperMenu.XP_SIDE_BUTTONS, menu::isXpSide);
        for (var widget : panel.layoutBeside(width, leftPos, CONTENT_WIDTH, topPos)) {
            addRenderableWidget(widget);
        }
        addRenderableWidget(panel.createInfoButton(leftPos + CONTENT_WIDTH - 44, topPos + 4, "vacuum_hopper"));
        addRenderableWidget(panel.createOpenButton(leftPos + CONTENT_WIDTH - 24, topPos + 4));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = leftPos;
        int y = topPos;
        SideConfigScreenHelper.blitContainer(graphics, MachineGuiTextures.VACUUM_HOPPER, x, y, CONTENT_WIDTH, imageHeight);
        SideConfigScreenHelper.drawFluidGauge(graphics, x + 9, y + 19, 10, 36,
                menu.getFluidAmount(), menu.getFluidCapacity());
        panel.render(graphics, font);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        boolean running = !menu.isVacuumDisabled();
        Component state = Component.translatable(running
                ? "container.opencubes.side_config.on"
                : "container.opencubes.side_config.off");
        graphics.text(font,
                Component.translatable("container.opencubes.vacuum_hopper.suction", state),
                8, 58, running ? 0xFF206020 : 0xFFAA2020, false);

        int percent = menu.getFluidAmount() * 100 / menu.getFluidCapacity();
        Component buffer = Component.translatable("container.opencubes.vacuum_hopper.buffer", percent);
        graphics.text(font, buffer, CONTENT_WIDTH - 8 - font.width(buffer), 58,
                SideConfigScreenHelper.TEXT, false);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop) {
        boolean outside = mouseX < guiLeft || mouseY < guiTop
                || mouseX >= guiLeft + getImageWidth() || mouseY >= guiTop + imageHeight;
        return outside && !panel.isMouseOver(mouseX, mouseY);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (isHovering(8, 18, 12, 38, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(font,
                    Component.translatable("container.opencubes.xp_bottler.fluid",
                            menu.getFluidAmount(), menu.getFluidCapacity()),
                    mouseX, mouseY);
            return;
        }
        if (isHovering(8, 56, 110, 12, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(font,
                    Component.translatable("container.opencubes.vacuum_hopper.toggle_hint"),
                    mouseX, mouseY);
            return;
        }
        super.extractTooltip(graphics, mouseX, mouseY);
    }
}
