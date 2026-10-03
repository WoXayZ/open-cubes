package dev.opencubes.util.shapes;

/**
 * Pure voxel generator - no Minecraft types. Produces relative coordinates into a {@link Shapeable}.
 */
public interface ShapeGenerator {
    void generateShape(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Shapeable shapeable);

    default void generateShape(int xSize, int ySize, int zSize, Shapeable shapeable) {
        generateShape(-xSize, -ySize, -zSize, xSize, ySize, zSize, shapeable);
    }
}
