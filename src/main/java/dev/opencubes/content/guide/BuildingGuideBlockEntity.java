package dev.opencubes.content.guide;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.util.shapes.Shapeable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class BuildingGuideBlockEntity extends BlockEntity {

    private static final Comparator<BlockPos> SHAPE_ORDER = (o1, o2) -> {
        int byY = Integer.compare(o1.getY(), o2.getY());
        if (byY != 0) {
            return byY;
        }
        double angle1 = Math.atan2(o1.getZ(), o1.getX());
        double angle2 = Math.atan2(o2.getZ(), o2.getX());
        int byAngle = Double.compare(angle1, angle2);
        if (byAngle != 0) {
            return byAngle;
        }
        double len1 = (double) o1.getX() * o1.getX() + (double) o1.getZ() * o1.getZ();
        double len2 = (double) o2.getX() * o2.getX() + (double) o2.getZ() * o2.getZ();
        int byDist = Double.compare(len2, len1);
        if (byDist != 0) {
            return byDist;
        }
        int byX = Integer.compare(o1.getX(), o2.getX());
        if (byX != 0) {
            return byX;
        }
        return Integer.compare(o1.getZ(), o2.getZ());
    };

    private final Map<GuideHalfAxis, Integer> halfExtents = new EnumMap<>(GuideHalfAxis.class);
    private GuideShape shapeMode = GuideShape.SPHERE;
    private DyeColor markerDye = DyeColor.WHITE;
    private boolean redstonePowered;
    private Direction facing = Direction.NORTH;

    private List<BlockPos> shape = List.of();
    private List<BlockPos> previousShape = List.of();
    private float timeSinceChange = 1.0F;
    @Nullable
    private AABB renderBox;

    public BuildingGuideBlockEntity(BlockPos pos, BlockState state) {
        this(OCBlockEntities.BUILDING_GUIDE.get(), pos, state);
    }

    protected BuildingGuideBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        for (GuideHalfAxis axis : GuideHalfAxis.values()) {
            halfExtents.put(axis, 8);
        }
        if (state.hasProperty(BuildingGuideBlock.FACING)) {
            facing = state.getValue(BuildingGuideBlock.FACING);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BuildingGuideBlockEntity guide) {
        if (guide.timeSinceChange < 1.0F) {
            guide.timeSinceChange = Math.min(1.0F, guide.timeSinceChange + 0.1F);
        }
    }

    public boolean shouldRenderMarkers() {
        int sensitivity = OCCommonConfig.GUIDE_REDSTONE_SENSITIVITY.get();
        if (sensitivity == 0) {
            return true;
        }
        return (sensitivity < 0) ^ redstonePowered;
    }

    public boolean isActive() {
        return shouldRenderMarkers();
    }

    public List<BlockPos> shapeCoords() {
        if (shape.isEmpty()) {
            recreateShape();
        }
        return shape;
    }

    public List<BlockPos> previousShapeCoords() {
        return previousShape;
    }

    public float timeSinceChange() {
        return timeSinceChange;
    }

    public int markerColor() {
        return markerDye.getTextureDiffuseColor() & 0xFFFFFF;
    }

    public DyeColor markerDye() {
        return markerDye;
    }

    public GuideShape shapeMode() {
        return shapeMode;
    }

    public int halfExtent(GuideHalfAxis axis) {
        return halfExtents.get(axis);
    }

    public Direction guideFacing() {
        return facing;
    }

    public void setShapeMode(GuideShape mode) {
        if (shapeMode == mode) {
            return;
        }
        shapeMode = mode;
        recreateShape();
        setChanged();
        sync();
    }

    public void setMarkerColor(int rgb) {
        setMarkerDye(closestDye(rgb));
    }

    public void setMarkerDye(DyeColor dye) {
        if (markerDye == dye) {
            return;
        }
        markerDye = dye;
        setChanged();
        sync();
    }

    private static DyeColor closestDye(int rgb) {
        int clean = rgb & 0xFFFFFF;
        DyeColor best = DyeColor.WHITE;
        int bestDist = Integer.MAX_VALUE;
        for (DyeColor dye : DyeColor.values()) {
            int c = dye.getTextureDiffuseColor() & 0xFFFFFF;
            int dr = ((c >> 16) & 0xFF) - ((clean >> 16) & 0xFF);
            int dg = ((c >> 8) & 0xFF) - ((clean >> 8) & 0xFF);
            int db = (c & 0xFF) - (clean & 0xFF);
            int dist = dr * dr + dg * dg + db * db;
            if (dist < bestDist) {
                bestDist = dist;
                best = dye;
            }
        }
        return best;
    }

    /** Moves one half-extent by {@code delta}, clamped to the 0-64 range the save format allows. */
    public boolean adjustHalfExtent(GuideHalfAxis axis, int delta) {
        int next = Mth.clamp(halfExtents.get(axis) + delta, 0, 64);
        if (next == halfExtents.get(axis)) {
            return false;
        }
        halfExtents.put(axis, next);
        recreateShape();
        setChanged();
        sync();
        return true;
    }

    public void mirrorHalfExtent(GuideHalfAxis from) {
        halfExtents.put(from.negate(), halfExtents.get(from));
        recreateShape();
        setChanged();
        sync();
    }

    public void rotate(boolean clockwise) {
        if (level == null || !(level.getBlockState(worldPosition).getBlock() instanceof BuildingGuideBlock)) {
            return;
        }
        Direction next = clockwise ? facing.getClockWise() : facing.getCounterClockWise();
        level.setBlock(worldPosition,
                level.getBlockState(worldPosition).setValue(BuildingGuideBlock.FACING, next), 3);
        onFacingChanged(next);
    }

    public int markerCount() {
        return shapeCoords().size();
    }

    public AABB renderBoundingBox() {
        if (renderBox == null) {
            renderBox = computeRenderBox();
        }
        return renderBox;
    }

    public void updateRedstone() {
        if (level == null || OCCommonConfig.GUIDE_REDSTONE_SENSITIVITY.get() == 0) {
            return;
        }
        boolean powered = level.hasNeighborSignal(worldPosition);
        if (powered != redstonePowered) {
            redstonePowered = powered;
            setChanged();
            sync();
        }
    }

    public void onFacingChanged(Direction newFacing) {
        if (facing != newFacing) {
            facing = newFacing;
            recreateShape();
            setChanged();
            sync();
        }
    }

    public boolean tryDye(ItemStack stack) {
        DyeColor dye = DyeColor.getColor(stack);
        if (dye == null) {
            return false;
        }
        setMarkerDye(dye);
        return true;
    }

    protected boolean canAddCoord(int x, int y, int z) {
        return x != 0 || y != 0 || z != 0;
    }

    protected void recreateShape() {
        previousShape = shape;
        timeSinceChange = 0.0F;
        shape = generateShape();
        renderBox = null;
    }

    private List<BlockPos> generateShape() {
        Set<BlockPos> unique = new HashSet<>();
        Shapeable collector = (x, y, z) -> {
            if (canAddCoord(x, y, z)) {
                unique.add(new BlockPos(x, y, z));
            }
        };
        shapeMode.generator().generateShape(
                -halfExtents.get(GuideHalfAxis.NEG_X),
                -halfExtents.get(GuideHalfAxis.NEG_Y),
                -halfExtents.get(GuideHalfAxis.NEG_Z),
                halfExtents.get(GuideHalfAxis.POS_X),
                halfExtents.get(GuideHalfAxis.POS_Y),
                halfExtents.get(GuideHalfAxis.POS_Z),
                collector);

        List<BlockPos> sorted = new ArrayList<>(unique);
        sorted.sort(SHAPE_ORDER);

        List<BlockPos> rotated = new ArrayList<>(sorted.size());
        for (BlockPos c : sorted) {
            rotated.add(rotate(c, facing));
        }

        int max = OCCommonConfig.GUIDE_MAX_MARKERS.get();
        if (rotated.size() > max) {
            return List.copyOf(rotated.subList(0, max));
        }
        return List.copyOf(rotated);
    }

    private static BlockPos rotate(BlockPos c, Direction facing) {
        return switch (facing) {
            case SOUTH -> new BlockPos(-c.getX(), c.getY(), -c.getZ());
            case WEST -> new BlockPos(c.getZ(), c.getY(), -c.getX());
            case EAST -> new BlockPos(-c.getZ(), c.getY(), c.getX());
            default -> c; // NORTH
        };
    }

    private AABB computeRenderBox() {
        if (shape.isEmpty()) {
            return new AABB(worldPosition);
        }
        int minX = 0;
        int minY = 0;
        int minZ = 0;
        int maxX = 0;
        int maxY = 0;
        int maxZ = 0;
        for (BlockPos c : shape) {
            minX = Math.min(minX, c.getX());
            minY = Math.min(minY, c.getY());
            minZ = Math.min(minZ, c.getZ());
            maxX = Math.max(maxX, c.getX());
            maxY = Math.max(maxY, c.getY());
            maxZ = Math.max(maxZ, c.getZ());
        }
        return new AABB(
                worldPosition.getX() + minX,
                worldPosition.getY() + minY,
                worldPosition.getZ() + minZ,
                worldPosition.getX() + maxX + 1,
                worldPosition.getY() + maxY + 1,
                worldPosition.getZ() + maxZ + 1);
    }

    protected void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        for (GuideHalfAxis axis : GuideHalfAxis.values()) {
            tag.putInt(axis.commandName(), halfExtents.get(axis));
        }
        tag.putString("Shape", shapeMode.getSerializedName());
        tag.putString("MarkerDye", markerDye.getSerializedName());
        tag.putInt("Color", markerColor()); // legacy
        tag.putBoolean("Powered", redstonePowered);
        tag.putString("Facing", facing.getSerializedName());
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        for (GuideHalfAxis axis : GuideHalfAxis.values()) {
            if (tag.keySet().contains(axis.commandName())) {
                halfExtents.put(axis, Mth.clamp(tag.getIntOr(axis.commandName(), 0), 0, 64));
            }
        }
        if (tag.keySet().contains("Shape")) {
            shapeMode = GuideShape.byId(tag.getStringOr("Shape", ""));
        }
        if (tag.keySet().contains("MarkerDye")) {
            DyeColor loaded = DyeColor.byName(tag.getStringOr("MarkerDye", ""), null);
            if (loaded != null) {
                markerDye = loaded;
            }
        } else if (tag.keySet().contains("Color")) {
            markerDye = closestDye(tag.getIntOr("Color", 0));
        }
        redstonePowered = tag.getBooleanOr("Powered", false);
        if (tag.keySet().contains("Facing")) {
            Direction loaded = Direction.byName(tag.getStringOr("Facing", ""));
            if (loaded != null && loaded.getAxis().isHorizontal()) {
                facing = loaded;
            }
        }
        recreateShape();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
