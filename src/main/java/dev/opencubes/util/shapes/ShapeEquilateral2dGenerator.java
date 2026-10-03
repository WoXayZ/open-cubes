package dev.opencubes.util.shapes;

import java.util.ArrayList;
import java.util.List;

public final class ShapeEquilateral2dGenerator implements ShapeGenerator {

    private record Trig(double sin, double cos) {}

    private record Point(int x, int y) {}

    private enum Symmetry {
        TWO_FOLD(Math.PI) {
            @Override
            Shapeable mirror(Shapeable shapeable) {
                return (x, y, z) -> {
                    if (z >= 0) {
                        shapeable.setBlock(x, y, +z);
                        shapeable.setBlock(x, y, -z);
                    }
                };
            }

            @Override
            Point mirrorLast(Point point) {
                return new Point(point.x, -point.y);
            }
        },
        FOUR_FOLD(Math.PI / 2) {
            @Override
            Shapeable mirror(Shapeable shapeable) {
                return (x, y, z) -> {
                    if (x >= 0 && z >= 0) {
                        shapeable.setBlock(+x, y, -z);
                        shapeable.setBlock(-x, y, -z);
                        shapeable.setBlock(-x, y, +z);
                        shapeable.setBlock(+x, y, +z);
                    }
                };
            }

            @Override
            Point mirrorLast(Point point) {
                return new Point(-point.x, point.y);
            }
        },
        EIGHT_FOLD(Math.PI / 4) {
            @Override
            Shapeable mirror(Shapeable shapeable) {
                return (x, y, z) -> {
                    if (x >= z) {
                        shapeable.setBlock(+x, y, -z);
                        shapeable.setBlock(-x, y, -z);
                        shapeable.setBlock(-x, y, +z);
                        shapeable.setBlock(+x, y, +z);

                        shapeable.setBlock(+z, y, -x);
                        shapeable.setBlock(-z, y, -x);
                        shapeable.setBlock(-z, y, +x);
                        shapeable.setBlock(+z, y, +x);
                    }
                };
            }

            @Override
            Point mirrorLast(Point point) {
                return new Point(point.y, point.x);
            }
        };

        final double angleLimit;

        Symmetry(double angleLimit) {
            this.angleLimit = angleLimit;
        }

        abstract Shapeable mirror(Shapeable shapeable);

        abstract Point mirrorLast(Point point);

        static Symmetry forSides(int sides) {
            if (sides % 4 == 0) {
                return EIGHT_FOLD;
            }
            if (sides % 2 == 0) {
                return FOUR_FOLD;
            }
            return TWO_FOLD;
        }
    }

    private final Symmetry symmetry;
    private final Trig[] angles;

    public ShapeEquilateral2dGenerator(int sides) {
        this.symmetry = Symmetry.forSides(sides);
        List<Trig> list = new ArrayList<>();
        for (int i = 0; i < sides; i++) {
            double d = 2 * Math.PI * i / sides;
            if (d > this.symmetry.angleLimit) {
                break;
            }
            list.add(new Trig(Math.sin(d), Math.cos(d)));
        }
        this.angles = list.toArray(Trig[]::new);
    }

    @Override
    public void generateShape(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                              Shapeable shapeable) {
        Shapeable column = (x, ignored, z) -> {
            for (int y = minY; y <= maxY; y++) {
                shapeable.setBlock(x, y, z);
            }
        };

        double middleX = (maxX + minX) / 2.0;
        double radiusX = (maxX - minX) / 2.0;
        double middleZ = (maxZ + minZ) / 2.0;
        double radiusZ = (maxZ - minZ) / 2.0;

        Point[] points = new Point[angles.length];
        for (int i = 0; i < angles.length; i++) {
            Trig t = angles[i];
            points[i] = new Point(
                    (int) Math.round(middleX + radiusX * t.cos),
                    (int) Math.round(middleZ + radiusZ * t.sin));
        }

        Shapeable mirrored = symmetry.mirror(column);
        Point prev = points[0];
        for (int i = 1; i < points.length; i++) {
            Point point = points[i];
            Geometry.line2D(0, prev.x, prev.y, point.x, point.y, mirrored);
            prev = point;
        }
        Point last = symmetry.mirrorLast(prev);
        Geometry.line2D(0, prev.x, prev.y, last.x, last.y, mirrored);
    }
}
