package dev.opencubes.content.imaginary;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public enum ImaginaryShape implements StringRepresentable {
    BLOCK(Shapes.block()),
    PANEL(Shapes.box(0.0D, 0.9D, 0.0D, 1.0D, 1.0D, 1.0D)),
    HALF_PANEL(Shapes.box(0.0D, 0.4D, 0.0D, 1.0D, 0.5D, 1.0D)),
    STAIRS(Shapes.or(
            Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D),
            Shapes.box(0.0D, 0.5D, 0.5D, 1.0D, 1.0D, 1.0D)));

    public static final ImaginaryShape[] VALUES = values();

    private final String name;
    private final VoxelShape shape;

    ImaginaryShape(VoxelShape shape) {
        this.name = name().toLowerCase();
        this.shape = shape;
    }

    public VoxelShape voxelShape() {
        return shape;
    }

    public VoxelShape rotated(Direction facing) {
        if (this != STAIRS) {
            return shape;
        }
        VoxelShape lower = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
        return switch (facing) {
            case NORTH -> Shapes.or(lower, Shapes.box(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 0.5D));
            case SOUTH -> shape;
            case EAST -> Shapes.or(lower, Shapes.box(0.5D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D));
            case WEST -> Shapes.or(lower, Shapes.box(0.0D, 0.5D, 0.0D, 0.5D, 1.0D, 1.0D));
            default -> shape;
        };
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
