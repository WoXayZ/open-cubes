package dev.opencubes.util.shapes;

import java.util.Set;

public final class ShapeSphereGenerator implements ShapeGenerator {

    private final Set<Geometry.Octant> octants;

    public ShapeSphereGenerator() {
        this(Geometry.Octant.ALL);
    }

    public ShapeSphereGenerator(Set<Geometry.Octant> octants) {
        this.octants = octants;
    }

    @Override
    public void generateShape(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                              Shapeable shapeable) {
        Geometry.makeEllipsoid(minX, minY, minZ, maxX, maxY, maxZ, shapeable, octants);
    }
}
