package byog.lab5;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {

    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    public static void addHexagon(TETile[][] world, Position p, int s, TETile t) {
        int i = p.px;
        int j = p.py;
        for (int y = j; y < j + s * 2; y++) {
            int nbx = helpery(y, j + s * 2, s);
            for (int x = i; x < j + s * 2; x++) {
                if (helperx(s, nbx, x)) {
                    world[x][y] = t;
                }
            }
        }
    }

    //returns the number symbols in a particular row
    private static int helpery(int y, int max, int s) {
        if (y == 0 || y == max - 1) {
            return s;
        } else if (y < s) {
            return 2 + helpery(y - 1, max, s);

        } else {
            return 2 + helpery(y + 1, max, s);
        }
    }

    //returns true if a symbol should be put at this spot
    private static boolean helperx(int s, int nbx, int x) {
        if (nbx == s) {
            return true;
        }
        if (nbx % 2 == 0) {
            if (x < s - (nbx / 2) || x > s + (nbx / 2)) {
                return false;
            }
            return true;
        } else {
            if (x < s - (1 + nbx / 2) || x > s + 1 + (nbx / 2)) {
                return false;
            }
            return true;
        }
    }

    public static void main(String[] args) {
        // initialize the tile rendering engine with a window of size WIDTH x HEIGHT
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        // initialize tiles
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        // fills in a block 14 tiles wide by 4 tiles tall
        for (int x = 20; x < 35; x += 1) {
            for (int y = 5; y < 10; y += 1) {
                world[x][y] = Tileset.WATER;
            }
        }
        // draws the world to the screen

        Position p = new Position(5, 5);

        HexWorld.addHexagon(world, p, 3, Tileset.FLOWER);

        ter.renderFrame(world);

    }

    @Test
    public void testhelpery() {
        assertEquals(3, helpery(0, 5, 3));
        assertEquals(5, helpery(1, 5, 3));


/**
 @Test public void testHexRowOffset() {
 assertEquals(0, hexRowOffset(3, 5));
 assertEquals(-1, hexRowOffset(3, 4));
 assertEquals(-2, hexRowOffset(3, 3));
 assertEquals(-2, hexRowOffset(3, 2));
 assertEquals(-1, hexRowOffset(3, 1));
 assertEquals(0, hexRowOffset(3, 0));
 assertEquals(0, hexRowOffset(2, 0));
 assertEquals(-1, hexRowOffset(2, 1));
 assertEquals(-1, hexRowOffset(2, 2));
 assertEquals(0, hexRowOffset(2, 3)); */
    }

    public static class Position {
        int px;
        int py;

        public Position(int px, int py) {
            this.px = px;
            this.py = py;
        }

        public Position() {
            this.px = 0;
            this.py = 0;
        }
    }
}
