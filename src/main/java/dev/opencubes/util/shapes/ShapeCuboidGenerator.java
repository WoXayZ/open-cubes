package dev.opencubes.util.shapes;

public final class ShapeCuboidGenerator implements ShapeGenerator {

    public enum Elements {
        CORNERS(true, false, false),
        EDGES(true, true, false),
        WALLS(true, true, true);

        final boolean corners;
        final boolean edges;
        final boolean walls;

        Elements(boolean corners, boolean edges, boolean walls) {
            this.corners = corners;
            this.edges = edges;
            this.walls = walls;
        }
    }

    private final boolean corners;
    private final boolean edges;
    private final boolean walls;

    public ShapeCuboidGenerator(Elements elements) {
        this.corners = elements.corners;
        this.edges = elements.edges;
        this.walls = elements.walls;
    }

    public ShapeCuboidGenerator() {
        this(Elements.WALLS);
    }

    @Override
    public void generateShape(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                              Shapeable shapeable) {
        int dx = maxX - minX - 2;
        int dy = maxY - minY - 2;
        int dz = maxZ - minZ - 2;

        if (corners) {
            shapeable.setBlock(maxX, maxY, maxZ);
            shapeable.setBlock(maxX, maxY, minZ);
            shapeable.setBlock(maxX, minY, maxZ);
            shapeable.setBlock(maxX, minY, minZ);
            shapeable.setBlock(minX, maxY, maxZ);
            shapeable.setBlock(minX, maxY, minZ);
            shapeable.setBlock(minX, minY, maxZ);
            shapeable.setBlock(minX, minY, minZ);
        }

        if (edges) {
            Geometry.makeLine(minX, minY + 1, minZ, Geometry.Axis.Y, dy, shapeable);
            Geometry.makeLine(minX, minY + 1, maxZ, Geometry.Axis.Y, dy, shapeable);
            Geometry.makeLine(maxX, minY + 1, maxZ, Geometry.Axis.Y, dy, shapeable);
            Geometry.makeLine(maxX, minY + 1, minZ, Geometry.Axis.Y, dy, shapeable);

            Geometry.makeLine(minX + 1, minY, minZ, Geometry.Axis.X, dx, shapeable);
            Geometry.makeLine(minX + 1, minY, maxZ, Geometry.Axis.X, dx, shapeable);
            Geometry.makeLine(minX + 1, maxY, maxZ, Geometry.Axis.X, dx, shapeable);
            Geometry.makeLine(minX + 1, maxY, minZ, Geometry.Axis.X, dx, shapeable);

            Geometry.makeLine(minX, minY, minZ + 1, Geometry.Axis.Z, dz, shapeable);
            Geometry.makeLine(minX, maxY, minZ + 1, Geometry.Axis.Z, dz, shapeable);
            Geometry.makeLine(maxX, maxY, minZ + 1, Geometry.Axis.Z, dz, shapeable);
            Geometry.makeLine(maxX, minY, minZ + 1, Geometry.Axis.Z, dz, shapeable);
        }

        if (walls) {
            Geometry.makePlane(minX + 1, minY + 1, minZ, dx, dy, Geometry.Axis.X, Geometry.Axis.Y, shapeable);
            Geometry.makePlane(minX + 1, minY + 1, maxZ, dx, dy, Geometry.Axis.X, Geometry.Axis.Y, shapeable);

            Geometry.makePlane(minX + 1, minY, minZ + 1, dx, dz, Geometry.Axis.X, Geometry.Axis.Z, shapeable);
            Geometry.makePlane(minX + 1, maxY, minZ + 1, dx, dz, Geometry.Axis.X, Geometry.Axis.Z, shapeable);

            Geometry.makePlane(minX, minY + 1, minZ + 1, dy, dz, Geometry.Axis.Y, Geometry.Axis.Z, shapeable);
            Geometry.makePlane(maxX, minY + 1, minZ + 1, dy, dz, Geometry.Axis.Y, Geometry.Axis.Z, shapeable);
        }
    }
}
