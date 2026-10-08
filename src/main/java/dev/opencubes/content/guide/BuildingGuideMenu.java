package dev.opencubes.content.guide;

import dev.opencubes.registry.OCMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Menu behind the Building Guide screen. Shape/size/rotation and colour use button ids.
 * The Enhanced Guide shares this menu (no item slot); placement happens in-world.
 */
public class BuildingGuideMenu extends AbstractContainerMenu {

    // Button ids travel as a single byte, so everything has to stay inside 0-127.
    public static final int BUTTON_DECREMENT = 0;
    public static final int BUTTON_INCREMENT = 6;
    public static final int BUTTON_MIRROR = 12;
    public static final int BUTTON_SHAPE_PREV = 18;
    public static final int BUTTON_SHAPE_NEXT = 19;
    public static final int BUTTON_ROTATE_CCW = 20;
    public static final int BUTTON_ROTATE_CW = 21;
    public static final int BUTTON_SHAPE_SET = 30;
    public static final int BUTTON_COLOUR_SET = 50;
    public static final int SHIFT_OFFSET = 100;

    private final BlockPos pos;
    @Nullable
    private final BuildingGuideBlockEntity guide;
    private final Player player;
    private final boolean enhanced;

    public BuildingGuideMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readBlockPos());
    }

    public BuildingGuideMenu(int id, Inventory inv, BlockPos pos) {
        super(OCMenus.BUILDING_GUIDE.get(), id);
        this.pos = pos;
        this.player = inv.player;
        this.guide = inv.player.level().getBlockEntity(pos) instanceof BuildingGuideBlockEntity be ? be : null;
        this.enhanced = guide instanceof EnhancedBuildingGuideBlockEntity;
    }

    public BlockPos pos() {
        return pos;
    }

    @Nullable
    public BuildingGuideBlockEntity guide() {
        return guide;
    }

    public boolean isEnhanced() {
        return enhanced;
    }

    public static int decrementId(GuideHalfAxis axis) {
        return BUTTON_DECREMENT + axis.ordinal();
    }

    public static int incrementId(GuideHalfAxis axis) {
        return BUTTON_INCREMENT + axis.ordinal();
    }

    public static int mirrorId(GuideHalfAxis axis) {
        return BUTTON_MIRROR + axis.ordinal();
    }

    public static int shapeId(GuideShape shape) {
        return BUTTON_SHAPE_SET + shape.ordinal();
    }

    public static int colourId(DyeColor colour) {
        return BUTTON_COLOUR_SET + colour.getId();
    }

    @Override
    public boolean clickMenuButton(Player clicker, int id) {
        if (guide == null || guide.getLevel() == null || guide.getLevel().isClientSide()) {
            return false;
        }
        boolean shift = id >= SHIFT_OFFSET;
        int base = shift ? id - SHIFT_OFFSET : id;
        if (base >= BUTTON_COLOUR_SET && base < BUTTON_COLOUR_SET + DyeColor.values().length) {
            guide.setMarkerDye(DyeColor.byId(base - BUTTON_COLOUR_SET));
            return true;
        }
        if (base >= BUTTON_SHAPE_SET && base < BUTTON_SHAPE_SET + GuideShape.VALUES.length) {
            guide.setShapeMode(GuideShape.VALUES[base - BUTTON_SHAPE_SET]);
            return true;
        }
        return switch (base) {
            case BUTTON_SHAPE_PREV -> {
                guide.setShapeMode(guide.shapeMode().previous());
                yield true;
            }
            case BUTTON_SHAPE_NEXT -> {
                guide.setShapeMode(guide.shapeMode().next());
                yield true;
            }
            case BUTTON_ROTATE_CCW -> {
                guide.rotate(false);
                yield true;
            }
            case BUTTON_ROTATE_CW -> {
                guide.rotate(true);
                yield true;
            }
            default -> axisButton(base, shift);
        };
    }

    private boolean axisButton(int id, boolean shift) {
        if (guide == null) {
            return false;
        }
        GuideHalfAxis[] axes = GuideHalfAxis.values();
        if (id >= BUTTON_MIRROR && id < BUTTON_MIRROR + axes.length) {
            guide.mirrorHalfExtent(axes[id - BUTTON_MIRROR]);
            return true;
        }
        int step = shift ? 10 : 1;
        if (id >= BUTTON_INCREMENT && id < BUTTON_INCREMENT + axes.length) {
            return guide.adjustHalfExtent(axes[id - BUTTON_INCREMENT], step);
        }
        if (id >= BUTTON_DECREMENT && id < BUTTON_DECREMENT + axes.length) {
            return guide.adjustHalfExtent(axes[id - BUTTON_DECREMENT], -step);
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player clicker, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player clicker) {
        return guide != null
                && !guide.isRemoved()
                && clicker.level().getBlockEntity(pos) == guide
                && clicker.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    public Player player() {
        return player;
    }
}
