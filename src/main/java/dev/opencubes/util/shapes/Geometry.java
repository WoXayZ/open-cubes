package dev.opencubes.util.shapes;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

/**
 * Port of OpenModsLib {@code GeometryUtils} ellipsoid / ellipse / line / plane helpers.
 * Algorithms preserved; Minecraft Direction is used only as a unit axis vector.
 */
public final class Geometry {

    private Geometry() {}

    public enum Axis {
        X(1, 0, 0, Direction.EAST),
        Y(0, 1, 0, Direction.UP),
        Z(0, 0, 1, Direction.SOUTH);

        public final Direction positive;

        Axis(int dx, int dy, int dz, Direction positive) {
            this.positive = positive;
        }
    }

    public enum Octant {
        TOP_SOUTH_WEST(Direction.WEST, Direction.UP, Direction.SOUTH),
        TOP_NORTH_EAST(Direction.EAST, Direction.UP, Direction.NORTH),
        TOP_NORTH_WEST(Direction.WEST, Direction.UP, Direction.NORTH),
        TOP_SOUTH_EAST(Direction.EAST, Direction.UP, Direction.SOUTH),
        BOTTOM_SOUTH_WEST(Direction.WEST, Direction.DOWN, Direction.SOUTH),
        BOTTOM_NORTH_EAST(Direction.EAST, Direction.DOWN, Direction.NORTH),
        BOTTOM_NORTH_WEST(Direction.WEST, Direction.DOWN, Direction.NORTH),
        BOTTOM_SOUTH_EAST(Direction.EAST, Direction.DOWN, Direction.SOUTH);

        public static final Set<Octant> ALL = EnumSet.allOf(Octant.class);
        public static final Set<Octant> SOUTH = select(Direction.SOUTH);

        public final int x;
        public final int y;
        public final int z;
        private final EnumSet<Direction> dirs;

        Octant(Direction dirX, Direction dirY, Direction dirZ) {
            this.x = dirX.getStepX() + dirY.getStepX() + dirZ.getStepX();
            this.y = dirX.getStepY() + dirY.getStepY() + dirZ.getStepY();
            this.z = dirX.getStepZ() + dirY.getStepZ() + dirZ.getStepZ();
            this.dirs = EnumSet.of(dirX, dirY, dirZ);
        }

        private static Set<Octant> select(Direction dir) {
            EnumSet<Octant> result = EnumSet.noneOf(Octant.class);
            for (Octant o : values()) {
                if (o.dirs.contains(dir)) {
                    result.add(o);
                }
            }
            return result;
        }
    }

    public enum Quadrant {
        TOP_SOUTH_WEST(-1, 1),
        TOP_NORTH_EAST(1, -1),
        TOP_NORTH_WEST(-1, -1),
        TOP_SOUTH_EAST(1, 1);

        public static final Set<Quadrant> ALL = EnumSet.allOf(Quadrant.class);

        public final int x;
        public final int z;

        Quadrant(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }

    public static void makeLine(int startX, int startY, int startZ, Axis axis, int length, Shapeable shapeable) {
        makeLine(startX, startY, startZ, axis.positive, length, shapeable);
    }

    public static void makeLine(int startX, int startY, int startZ, Direction direction, int length,
                                Shapeable shapeable) {
        if (length < 0) {
            return;
        }
        Vec3i v = direction.getNormal();
        for (int offset = 0; offset <= length; offset++) {
            shapeable.setBlock(
                    startX + offset * v.getX(),
                    startY + offset * v.getY(),
                    startZ + offset * v.getZ());
        }
    }

    public static void makePlane(int startX, int startY, int startZ, int width, int height,
                                 Axis right, Axis up, Shapeable shapeable) {
        makePlane(startX, startY, startZ, width, height, right.positive, up.positive, shapeable);
    }

    public static void makePlane(int startX, int startY, int startZ, int width, int height,
                                 Direction right, Direction up, Shapeable shapeable) {
        if (width < 0 || height < 0) {
            return;
        }
        Vec3i v = up.getNormal();
        for (int h = 0; h <= height; h++) {
            makeLine(
                    startX + h * v.getX(),
                    startY + h * v.getY(),
                    startZ + h * v.getZ(),
                    right, width, shapeable);
        }
    }

    public static void makeEllipsoid(int radiusX, int radiusY, int radiusZ, Shapeable shapeable,
                                     Set<Octant> octants) {
        List<Octant> octantsList = List.copyOf(octants);

        double invRadiusX = 1.0 / (radiusX + 0.5);
        double invRadiusY = 1.0 / (radiusY + 0.5);
        double invRadiusZ = 1.0 / (radiusZ + 0.5);

        double nextXn = 0;
        forX:
        for (int x = 0; x <= radiusX; ++x) {
            double xn = nextXn;
            nextXn += invRadiusX;
            double nextYn = 0;
            forY:
            for (int y = 0; y <= radiusY; ++y) {
                double yn = nextYn;
                nextYn += invRadiusY;
                double nextZn = 0;
                forZ:
                for (int z = 0; z <= radiusZ; ++z) {
                    double zn = nextZn;
                    nextZn += invRadiusZ;

                    double distanceSq = lengthSq(xn, yn, zn);
                    if (distanceSq > 1) {
                        if (z == 0) {
                            if (y == 0) {
                                break forX;
                            }
                            break forY;
                        }
                        break forZ;
                    }

                    if (lengthSq(nextXn, yn, zn) <= 1
                            && lengthSq(xn, nextYn, zn) <= 1
                            && lengthSq(xn, yn, nextZn) <= 1) {
                        continue;
                    }

                    for (Octant octant : octantsList) {
                        shapeable.setBlock(x * octant.x, y * octant.y, z * octant.z);
                    }
                }
            }
        }
    }

    public static void makeEllipsoid(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                                     Shapeable shapeable, Set<Octant> octants) {
        int centerX = (minX + maxX) / 2;
        int centerY = (minY + maxY) / 2;
        int centerZ = (minZ + maxZ) / 2;

        Shapeable centered = (x, y, z) -> shapeable.setBlock(centerX + x, centerY + y, centerZ + z);

        int radiusX;
        int radiusY;
        int radiusZ;

        int diffX = maxX - minX;
        if ((diffX & 1) == 0) {
            radiusX = diffX / 2;
        } else {
            radiusX = diffX / 2 + 1;
            centered = skipMiddleX(centered);
        }

        int diffY = maxY - minY;
        if ((diffY & 1) == 0) {
            radiusY = diffY / 2;
        } else {
            radiusY = diffY / 2 + 1;
            centered = skipMiddleY(centered);
        }

        int diffZ = maxZ - minZ;
        if ((diffZ & 1) == 0) {
            radiusZ = diffZ / 2;
        } else {
            radiusZ = diffZ / 2 + 1;
            centered = skipMiddleZ(centered);
        }

        makeEllipsoid(radiusX, radiusY, radiusZ, centered, octants);
    }

    public static void makeEllipse(int radiusX, int radiusZ, int y, Shapeable shapeable,
                                   Set<Quadrant> quadrants) {
        double invRadiusX = 1.0 / (radiusX + 0.5);
        double invRadiusZ = 1.0 / (radiusZ + 0.5);
        List<Quadrant> quadrantsList = List.copyOf(quadrants);

        double nextXn = 0;
        forX:
        for (int x = 0; x <= radiusX; ++x) {
            double xn = nextXn;
            nextXn += invRadiusX;
            double nextZn = 0;
            forZ:
            for (int z = 0; z <= radiusZ; ++z) {
                double zn = nextZn;
                nextZn += invRadiusZ;

                double distanceSq = lengthSq(xn, zn);
                if (distanceSq > 1) {
                    if (z == 0) {
                        break forX;
                    }
                    break forZ;
                }

                if (lengthSq(nextXn, zn) <= 1 && lengthSq(xn, nextZn) <= 1) {
                    continue;
                }

                for (Quadrant quadrant : quadrantsList) {
                    shapeable.setBlock(x * quadrant.x, y, z * quadrant.z);
                }
            }
        }
    }

    public static void makeEllipse(int minX, int minZ, int maxX, int maxZ, int y, Shapeable shapeable,
                                   Set<Quadrant> quadrants) {
        int centerX = (minX + maxX) / 2;
        int centerZ = (minZ + maxZ) / 2;
        Shapeable centered = (x, y1, z) -> shapeable.setBlock(centerX + x, y1, centerZ + z);

        int radiusX;
        int radiusZ;

        int diffX = maxX - minX;
        if ((diffX & 1) == 0) {
            radiusX = diffX / 2;
        } else {
            radiusX = diffX / 2 + 1;
            centered = skipMiddleX(centered);
        }

        int diffZ = maxZ - minZ;
        if ((diffZ & 1) == 0) {
            radiusZ = diffZ / 2;
        } else {
            radiusZ = diffZ / 2 + 1;
            centered = skipMiddleZ(centered);
        }

        makeEllipse(radiusX, radiusZ, y, centered, quadrants);
    }

    public static void line2D(int y, int x0, int z0, int x1, int z1, Shapeable shapeable) {
        int dx = Math.abs(x1 - x0);
        int sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(z1 - z0);
        int sy = z0 < z1 ? 1 : -1;
        int err = dx + dy;

        while (true) {
            shapeable.setBlock(x0, y, z0);
            if (x0 == x1 && z0 == z1) {
                break;
            }
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                z0 += sy;
            }
        }
    }

    public static void line3D(int startX, int startY, int startZ, int endX, int endY, int endZ,
                              Shapeable shapeable) {
        int dx = endX - startX;
        int dy = endY - startY;
        int dz = endZ - startZ;

        int ax = Math.abs(dx) << 1;
        int ay = Math.abs(dy) << 1;
        int az = Math.abs(dz) << 1;

        int signx = Integer.signum(dx);
        int signy = Integer.signum(dy);
        int signz = Integer.signum(dz);

        int x = startX;
        int y = startY;
        int z = startZ;

        if (ax >= Math.max(ay, az)) {
            int deltay = ay - (ax >> 1);
            int deltaz = az - (ax >> 1);
            while (true) {
                shapeable.setBlock(x, y, z);
                if (x == endX) {
                    return;
                }
                if (deltay >= 0) {
                    y += signy;
                    deltay -= ax;
                }
                if (deltaz >= 0) {
                    z += signz;
                    deltaz -= ax;
                }
                x += signx;
                deltay += ay;
                deltaz += az;
            }
        } else if (ay >= Math.max(ax, az)) {
            int deltax = ax - (ay >> 1);
            int deltaz = az - (ay >> 1);
            while (true) {
                shapeable.setBlock(x, y, z);
                if (y == endY) {
                    return;
                }
                if (deltax >= 0) {
                    x += signx;
                    deltax -= ay;
                }
                if (deltaz >= 0) {
                    z += signz;
                    deltaz -= ay;
                }
                y += signy;
                deltax += ax;
                deltaz += az;
            }
        } else {
            int deltax = ax - (az >> 1);
            int deltay = ay - (az >> 1);
            while (true) {
                shapeable.setBlock(x, y, z);
                if (z == endZ) {
                    return;
                }
                if (deltax >= 0) {
                    x += signx;
                    deltax -= az;
                }
                if (deltay >= 0) {
                    y += signy;
                    deltay -= az;
                }
                z += signz;
                deltax += ax;
                deltay += ay;
            }
        }
    }

    private static Shapeable skipMiddleX(Shapeable shapeable) {
        return (x, y, z) -> {
            if (x != 0) {
                if (x < 0) {
                    shapeable.setBlock(x + 1, y, z);
                } else {
                    shapeable.setBlock(x, y, z);
                }
            }
        };
    }

    private static Shapeable skipMiddleY(Shapeable shapeable) {
        return (x, y, z) -> {
            if (y != 0) {
                if (y < 0) {
                    shapeable.setBlock(x, y + 1, z);
                } else {
                    shapeable.setBlock(x, y, z);
                }
            }
        };
    }

    private static Shapeable skipMiddleZ(Shapeable shapeable) {
        return (x, y, z) -> {
            if (z != 0) {
                if (z < 0) {
                    shapeable.setBlock(x, y, z + 1);
                } else {
                    shapeable.setBlock(x, y, z);
                }
            }
        };
    }

    private static double lengthSq(double x, double y, double z) {
        return x * x + y * y + z * z;
    }

    private static double lengthSq(double x, double z) {
        return x * x + z * z;
    }
}
