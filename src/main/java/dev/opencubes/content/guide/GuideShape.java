package dev.opencubes.content.guide;

import dev.opencubes.util.shapes.Geometry;
import dev.opencubes.util.shapes.ShapeAxesGenerator;
import dev.opencubes.util.shapes.ShapeCuboidGenerator;
import dev.opencubes.util.shapes.ShapeCylinderGenerator;
import dev.opencubes.util.shapes.ShapeEquilateral2dGenerator;
import dev.opencubes.util.shapes.ShapeGenerator;
import dev.opencubes.util.shapes.ShapePlanesGenerator;
import dev.opencubes.util.shapes.ShapeSphereGenerator;
import net.minecraft.util.StringRepresentable;

public enum GuideShape implements StringRepresentable {
    SPHERE(new ShapeSphereGenerator(Geometry.Octant.ALL), "sphere"),
    CYLINDER(new ShapeCylinderGenerator(), "cylinder"),
    CUBOID(new ShapeCuboidGenerator(ShapeCuboidGenerator.Elements.EDGES), "cuboid"),
    FULL_CUBOID(new ShapeCuboidGenerator(ShapeCuboidGenerator.Elements.WALLS), "full_cuboid"),
    DOME(new ShapeSphereGenerator(Geometry.Octant.SOUTH), "dome"),
    TRIANGLE(new ShapeEquilateral2dGenerator(3), "triangle"),
    PENTAGON(new ShapeEquilateral2dGenerator(5), "pentagon"),
    HEXAGON(new ShapeEquilateral2dGenerator(6), "hexagon"),
    OCTAGON(new ShapeEquilateral2dGenerator(8), "octagon"),
    AXES(new ShapeAxesGenerator(), "axes"),
    PLANES(new ShapePlanesGenerator(), "planes");

    public static final GuideShape[] VALUES = values();

    private final ShapeGenerator generator;
    private final String id;

    GuideShape(ShapeGenerator generator, String id) {
        this.generator = generator;
        this.id = id;
    }

    public ShapeGenerator generator() {
        return generator;
    }

    public String translationKey() {
        return "opencubes.misc.shape." + id;
    }

    public GuideShape next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public GuideShape previous() {
        return VALUES[(ordinal() + VALUES.length - 1) % VALUES.length];
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public static GuideShape byId(String name) {
        for (GuideShape shape : VALUES) {
            if (shape.id.equals(name)) {
                return shape;
            }
        }
        return SPHERE;
    }
}
