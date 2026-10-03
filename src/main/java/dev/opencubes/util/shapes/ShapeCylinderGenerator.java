package dev.opencubes.util.shapes;

import java.util.Set;

public final class ShapeCylinderGenerator implements ShapeGenerator {

    private final Set<Geometry.Quadrant> quadrants;

    public ShapeCylinderGenerator() {
        this(Geometry.Quadrant.ALL);
    }

    public ShapeCylinderGenerator(Set<Geometry.Quadrant> quadrants) {
        this.quadrants = quadrants;
    }

    @Override
    public void generateShape(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                              Shapeable shapeable) {
        Geometry.makeEllipse(minX, minZ, maxX, maxZ, 0, (x, ignore, z) -> {
            for (int y = minY; y <= maxY; y++) {
                shapeable.setBlock(x, y, z);
            }
        }, quadrants);
    }
}
