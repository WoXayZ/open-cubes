package dev.opencubes.content.paint;

import java.util.BitSet;
import java.util.Locale;

/**
 * 16×16 paint masks, rows top to bottom, {@code X} = hole the paint goes through. Same names and
 * shapes as OpenBlocks. The item, mask and cover textures under {@code textures/item/stencil} and
 * {@code textures/block/stencil_mask} are cut from these strings and must be regenerated when one
 * changes.
 */
public enum StencilPattern {
    CREEPER_FACE(
            "                ",
            "                ",
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  ",
            "      XXXX      ",
            "      XXXX      ",
            "    XXXXXXXX    ",
            "    XXXXXXXX    ",
            "    XXXXXXXX    ",
            "    XXXXXXXX    ",
            "    XX    XX    ",
            "    XX    XX    ",
            "                ",
            "                "),
    BORDER(
            "XX              ",
            "XX              ",
            "XX              ",
            "XX              ",
            "XX              ",
            "XX              ",
            "XX              ",
            "XX              ",
            "XX              ",
            "XX              ",
            "XX              ",
            "XX              ",
            "XX              ",
            "XX              ",
            "XX              ",
            "XX              "),
    STRIPES(
            "X X X X X X X X ",
            "X X X X X X X X ",
            "X X X X X X X X ",
            "X X X X X X X X ",
            "X X X X X X X X ",
            "X X X X X X X X ",
            "X X X X X X X X ",
            "X X X X X X X X ",
            "X X X X X X X X ",
            "X X X X X X X X ",
            "X X X X X X X X ",
            "X X X X X X X X ",
            "X X X X X X X X ",
            "X X X X X X X X ",
            "X X X X X X X X ",
            "X X X X X X X X "),
    CORNER(
            "                ",
            "                ",
            "  XXXXXX        ",
            "  XXXXXX        ",
            "  XX            ",
            "  XX            ",
            "  XX            ",
            "  XX            ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                "),
    CORNER2(
            "XXXXXXXX        ",
            "XXXXXXX         ",
            "XXXXXX          ",
            "XXXXX           ",
            "XXXX            ",
            "XXX             ",
            "XX              ",
            "X               ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                "),
    CORNER3(
            "                ",
            " XXXXXXX        ",
            " XXXXXXX        ",
            " XX             ",
            " XX XXXX        ",
            " XX XXXX        ",
            " XX XX          ",
            " XX XX          ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                "),
    HOLE(
            "                ",
            "                ",
            "                ",
            "                ",
            "    XXXXXXXX    ",
            "    XXXXXXXX    ",
            "    XXXXXXXX    ",
            "    XXXXXXXX    ",
            "    XXXXXXXX    ",
            "    XXXXXXXX    ",
            "    XXXXXXXX    ",
            "    XXXXXXXX    ",
            "                ",
            "                ",
            "                ",
            "                "),
    SPIRAL(
            "                ",
            "XXXXXXXXXXXXXXX ",
            "              X ",
            " XXXXXXXXXXXX X ",
            " X          X X ",
            " X XXXXXXXX X X ",
            " X X      X X X ",
            " X X XXXX X X X ",
            " X X X  X X X X ",
            " X X X    X X X ",
            " X X XXXXXX X X ",
            " X X        X X ",
            " X XXXXXXXXXX X ",
            " X            X ",
            " XXXXXXXXXXXXXX ",
            "                "),
    THICKSTRIPES(
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  ",
            "  XXXX    XXXX  "),
    SPLAT(
            " XX     X    XX ",
            "XXX    XXX   XXX",
            "XX     XX      X",
            "    X      XX   ",
            "   XXX    XXX   ",
            "   XXX    X    X",
            "    XX      XXXX",
            "              XX",
            "        X       ",
            "       XX  XX   ",
            " XX   XXX     X ",
            " XXX  XXX     XX",
            " XXX   X        ",
            "           XX   ",
            "    XX     XXX  ",
            "    XXX     XX  "),
    STORAGE(
            "                ",
            "                ",
            "                ",
            "   XXXXXXXXXX   ",
            "   X        X   ",
            "   X        X   ",
            "   X   XX   X   ",
            "   XXXXXXXXXX   ",
            "   X   XX   X   ",
            "   X        X   ",
            "   X        X   ",
            "   X        X   ",
            "   XXXXXXXXXX   ",
            "                ",
            "                ",
            "                "),
    HEART(
            "                ",
            "                ",
            "   XXX    XXX   ",
            "  X   X  X   X  ",
            " X     XX     X ",
            " X            X ",
            " X            X ",
            " X            X ",
            "  X          X  ",
            "   X        X   ",
            "    X      X    ",
            "     X    X     ",
            "      X  X      ",
            "       XX       ",
            "                ",
            "                "),
    HEART2(
            "                ",
            "                ",
            "                ",
            "   XXX    XXX   ",
            "  XXXXX  XXXXX  ",
            "  XXXXXXXXXXXX  ",
            "  XXXXXXXXXXXX  ",
            "  XXXXXXXXXXXX  ",
            "   XXXXXXXXXX   ",
            "    XXXXXXXX    ",
            "     XXXXXX     ",
            "      XXXX      ",
            "       XX       ",
            "                ",
            "                ",
            "                "),
    MUSIC(
            "                ",
            "                ",
            "       XXXXXX   ",
            "  XXXXXXXXXXX   ",
            "  XXXXXX    X   ",
            "  X         X   ",
            "  X         X   ",
            "  X         X   ",
            "  X         X   ",
            "  X         XX  ",
            "  XX        XXX ",
            "  XXX       XXX ",
            "  XXX        X  ",
            "   X            ",
            "                ",
            "                "),
    BALLOON(
            "                ",
            "      XXXX      ",
            "     XXXXXX     ",
            "    XXXXXXXX    ",
            "   XXXXXXXXXX   ",
            "   XXXXXXXXXX   ",
            "   XXXXXXXXXX   ",
            "   XXXXXXXXXX   ",
            "   XXXXXXXXXX   ",
            "    XXXXXXXX    ",
            "     XXXXXX     ",
            "      XXXX      ",
            "       X        ",
            "      XXX       ",
            "         X      ",
            "          XXX   ");

    public static final int SIZE = 16;

    private final BitSet holes = new BitSet(SIZE * SIZE);

    StencilPattern(String... rows) {
        if (rows.length != SIZE) {
            throw new IllegalArgumentException(name() + " needs " + SIZE + " rows");
        }
        for (int y = 0; y < SIZE; y++) {
            if (rows[y].length() != SIZE) {
                throw new IllegalArgumentException(name() + " row " + y + " needs " + SIZE + " columns");
            }
            for (int x = 0; x < SIZE; x++) {
                if (rows[y].charAt(x) != ' ') {
                    holes.set(y * SIZE + x);
                }
            }
        }
    }

    /**
     * {@code x} left to right and {@code y} top to bottom, as the face texture is read, with the
     * pattern turned clockwise by {@code rotation} quarter turns.
     */
    public boolean isHole(int x, int y, int rotation) {
        int last = SIZE - 1;
        return switch (rotation & 3) {
            case 1 -> holes.get((last - x) * SIZE + y);
            case 2 -> holes.get((last - y) * SIZE + last - x);
            case 3 -> holes.get(x * SIZE + last - y);
            default -> holes.get(y * SIZE + x);
        };
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static StencilPattern byId(String id) {
        try {
            return StencilPattern.valueOf(id.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return CREEPER_FACE;
        }
    }
}
