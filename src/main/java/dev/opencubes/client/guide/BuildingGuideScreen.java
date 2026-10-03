package dev.opencubes.client.guide;

import dev.opencubes.client.MachineGuiTextures;
import dev.opencubes.client.SideConfigScreenHelper;
import dev.opencubes.client.sideconfig.MachineInfoButton;
import dev.opencubes.content.guide.BuildingGuideBlockEntity;
import dev.opencubes.content.guide.BuildingGuideMenu;
import dev.opencubes.content.guide.GuideHalfAxis;
import dev.opencubes.content.guide.GuideShape;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;

/**
 * Building Guide controls: shape, per-axis half extents, rotation and marker colour swatches.
 * Enhanced Guide uses the same layout (placement is done in-world from the held block only).
 */
public class BuildingGuideScreen extends AbstractContainerScreen<BuildingGuideMenu> {

    private static final int TEXT = SideConfigScreenHelper.TEXT;
    private static final int CONTENT_WIDTH = 232;

    private static final GuideHalfAxis[] LEFT_AXES = {GuideHalfAxis.NEG_X, GuideHalfAxis.NEG_Y, GuideHalfAxis.NEG_Z};
    private static final GuideHalfAxis[] RIGHT_AXES = {GuideHalfAxis.POS_X, GuideHalfAxis.POS_Y, GuideHalfAxis.POS_Z};

    public BuildingGuideScreen(BuildingGuideMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = CONTENT_WIDTH;
        this.imageHeight = 178;
        // No player inventory slots on this screen.
        this.inventoryLabelY = 10000;
    }

    @Override
    protected void init() {
        imageWidth = CONTENT_WIDTH;
        super.init();
        BuildingGuideBlockEntity guide = menu.guide();
        GuideShape current = guide != null ? guide.shapeMode() : GuideShape.SPHERE;

        addRenderableWidget(CycleButton.<GuideShape>builder(shape -> Component.translatable(shape.translationKey()))
                .withValues(GuideShape.VALUES)
                .withInitialValue(current)
                .displayOnlyValue()
                .create(leftPos + 8, topPos + 20, 140, 18,
                        Component.translatable("container.opencubes.building_guide.shape"),
                        (button, shape) -> press(BuildingGuideMenu.shapeId(shape))));

        addRenderableWidget(Button.builder(Component.literal("<"),
                        button -> press(BuildingGuideMenu.BUTTON_ROTATE_CCW))
                .bounds(leftPos + 154, topPos + 20, 18, 18)
                .tooltip(Tooltip.create(
                        Component.translatable("container.opencubes.building_guide.rotate_ccw")))
                .build());
        addRenderableWidget(Button.builder(Component.literal(">"),
                        button -> press(BuildingGuideMenu.BUTTON_ROTATE_CW))
                .bounds(leftPos + 174, topPos + 20, 18, 18)
                .tooltip(Tooltip.create(
                        Component.translatable("container.opencubes.building_guide.rotate_cw")))
                .build());

        for (int row = 0; row < 3; row++) {
            addAxisRow(LEFT_AXES[row], leftPos + 8, topPos + 58 + row * 20);
            addAxisRow(RIGHT_AXES[row], leftPos + 120, topPos + 58 + row * 20);
        }

        // Same packing as Paint Mixer presets: 10px swatches, 10px stride.
        int swatchY = topPos + 156;
        for (DyeColor dye : DyeColor.values()) {
            addRenderableWidget(new MarkerSwatch(leftPos + 8 + dye.getId() * 10, swatchY, dye));
        }

        String infoKey = menu.isEnhanced() ? "enhanced_building_guide" : "building_guide";
        addRenderableWidget(MachineInfoButton.forMachine(leftPos + CONTENT_WIDTH - 22, topPos + 4, infoKey));
    }

    private void addAxisRow(GuideHalfAxis axis, int x, int y) {
        addRenderableWidget(Button.builder(Component.literal("-"),
                        button -> press(BuildingGuideMenu.decrementId(axis)))
                .bounds(x + 22, y, 16, 16)
                .build());
        addRenderableWidget(Button.builder(Component.literal("+"),
                        button -> press(BuildingGuideMenu.incrementId(axis)))
                .bounds(x + 62, y, 16, 16)
                .build());
        addRenderableWidget(Button.builder(Component.literal("="),
                        button -> press(BuildingGuideMenu.mirrorId(axis)))
                .bounds(x + 80, y, 16, 16)
                .tooltip(Tooltip.create(
                        Component.translatable("container.opencubes.building_guide.mirror")))
                .build());
    }

    private void press(int buttonId) {
        if (minecraft == null || minecraft.gameMode == null) {
            return;
        }
        int id = buttonId;
        if (hasShiftDown() && isAxisAdjust(buttonId)) {
            id += BuildingGuideMenu.SHIFT_OFFSET;
        }
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    private static boolean isAxisAdjust(int buttonId) {
        return (buttonId >= BuildingGuideMenu.BUTTON_DECREMENT
                && buttonId < BuildingGuideMenu.BUTTON_DECREMENT + GuideHalfAxis.values().length)
                || (buttonId >= BuildingGuideMenu.BUTTON_INCREMENT
                && buttonId < BuildingGuideMenu.BUTTON_INCREMENT + GuideHalfAxis.values().length);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        SideConfigScreenHelper.blitContainer(graphics, MachineGuiTextures.BUILDING_GUIDE,
                leftPos, topPos, CONTENT_WIDTH, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        BuildingGuideBlockEntity guide = menu.guide();
        graphics.drawString(font, title, 8, 7, TEXT, false);
        graphics.drawString(font, Component.translatable("container.opencubes.building_guide.size"),
                8, 46, TEXT, false);

        for (int row = 0; row < 3; row++) {
            drawAxisLabel(graphics, guide, LEFT_AXES[row], 8, 62 + row * 20);
            drawAxisLabel(graphics, guide, RIGHT_AXES[row], 120, 62 + row * 20);
        }

        if (guide != null) {
            graphics.drawString(font, Component.translatable("container.opencubes.building_guide.facing",
                            Component.translatable("container.opencubes.direction."
                                    + guide.guideFacing().getSerializedName())),
                    8, 124, TEXT, false);
            graphics.drawString(font, Component.translatable("container.opencubes.building_guide.blocks",
                            guide.markerCount()),
                    8, 136, TEXT, false);
        }
        graphics.drawString(font, Component.translatable("container.opencubes.building_guide.colour"),
                8, 146, TEXT, false);
    }

    private void drawAxisLabel(GuiGraphics graphics, BuildingGuideBlockEntity guide, GuideHalfAxis axis,
                               int x, int y) {
        graphics.drawString(font, axisName(axis), x, y, TEXT, false);
        String value = guide == null ? "-" : Integer.toString(guide.halfExtent(axis));
        int centre = x + 40 + (24 - font.width(value)) / 2;
        graphics.drawString(font, value, centre, y, TEXT, false);
    }

    private static Component axisName(GuideHalfAxis axis) {
        return Component.literal(switch (axis) {
            case NEG_X -> "X-";
            case POS_X -> "X+";
            case NEG_Y -> "Y-";
            case POS_Y -> "Y+";
            case NEG_Z -> "Z-";
            case POS_Z -> "Z+";
        });
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private final class MarkerSwatch extends AbstractButton {

        private final DyeColor dye;

        private MarkerSwatch(int x, int y, DyeColor dye) {
            super(x, y, 10, 10, Component.translatable("color.minecraft." + dye.getName()));
            this.dye = dye;
            setTooltip(Tooltip.create(getMessage()));
        }

        @Override
        public void onPress() {
            press(BuildingGuideMenu.colourId(dye));
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int rgb = 0xFF000000 | dye.getTextureDiffuseColor();
            graphics.fill(getX(), getY(), getX() + width, getY() + height, SideConfigScreenHelper.SLOT_SHADOW);
            graphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, rgb);
            BuildingGuideBlockEntity guide = menu.guide();
            boolean selected = guide != null && guide.markerDye() == dye;
            if (selected) {
                graphics.renderOutline(getX(), getY(), width, height, 0xFFFFFFFF);
            } else if (isHovered()) {
                graphics.renderOutline(getX(), getY(), width, height, 0xFF8B8B8B);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
