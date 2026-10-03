package dev.opencubes.client;

import dev.opencubes.client.sideconfig.SideConfigPanel;
import dev.opencubes.content.automation.VacuumHopperMenu;
import net.minecraft.client.gui.GuiGraphics;
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
        super(menu, inventory, title);
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
        imageWidth = CONTENT_WIDTH + SideConfigPanel.exclusiveWidth(VacuumHopperMenu.class);
    }

    @Override
    protected void init() {
        imageWidth = CONTENT_WIDTH + SideConfigPanel.exclusiveWidth(VacuumHopperMenu.class);
        super.init();
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
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        SideConfigScreenHelper.blitContainer(graphics, MachineGuiTextures.VACUUM_HOPPER, x, y, CONTENT_WIDTH, imageHeight);
        SideConfigScreenHelper.drawFluidGauge(graphics, x + 9, y + 19, 10, 36,
                menu.getFluidAmount(), menu.getFluidCapacity());
        panel.render(graphics, font);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        boolean running = !menu.isVacuumDisabled();
        Component state = Component.translatable(running
                ? "container.opencubes.side_config.on"
                : "container.opencubes.side_config.off");
        graphics.drawString(font,
                Component.translatable("container.opencubes.vacuum_hopper.suction", state),
                8, 58, running ? 0x206020 : 0xAA2020, false);

        int percent = menu.getFluidAmount() * 100 / menu.getFluidCapacity();
        Component buffer = Component.translatable("container.opencubes.vacuum_hopper.buffer", percent);
        graphics.drawString(font, buffer, CONTENT_WIDTH - 8 - font.width(buffer), 58,
                SideConfigScreenHelper.TEXT, false);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int button) {
        return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, button)
                && !panel.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(8, 18, 12, 38, mouseX, mouseY)) {
            graphics.renderTooltip(font,
                    Component.translatable("container.opencubes.xp_bottler.fluid",
                            menu.getFluidAmount(), menu.getFluidCapacity()),
                    mouseX, mouseY);
        }
        if (isHovering(8, 56, 110, 12, mouseX, mouseY)) {
            graphics.renderTooltip(font,
                    Component.translatable("container.opencubes.vacuum_hopper.toggle_hint"),
                    mouseX, mouseY);
        }
    }
}
