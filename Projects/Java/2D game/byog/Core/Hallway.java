package byog.Core;

import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

import java.io.Serializable;
import java.util.Random;

public class Hallway implements Serializable {
    TETile[][] world;


    public Hallway(TETile[][] world) {
        this.world = world;
    }


    // Creates a vertical path between (b.px, a.py) to (b.px, b.py).

    public void createVertical(Position a, Position b) {
        if (a.py <= b.py) {
            for (int i = a.py + 2; i < b.py; i++) {
                if (world[b.px][i].equals(Tileset.NOTHING)) {
                    world[b.px][i] = Tileset.WALL;
                }
                world[b.px + 1][i] = Tileset.FLOOR;

                if (world[b.px + 2][i].equals(Tileset.NOTHING)) {
                    world[b.px + 2][i] = Tileset.WALL;
                }
            }
        } else {
            for (int i = a.py; i > b.py; i--) {
                if (world[b.px][i].equals(Tileset.NOTHING)) {
                    world[b.px][i] = Tileset.WALL;
                }
                world[b.px + 1][i] = Tileset.FLOOR;
                if (world[b.px + 2][i].equals(Tileset.NOTHING)) {
                    world[b.px + 2][i] = Tileset.WALL;
                }
            }
        }
    }


    // Create a horizontal path from (a.px,a.py) to (b.px, a.py)
    public void createHorizontal(Position a, Position b) {
        if (a.px <= b.px) {
            for (int i = a.px; i < b.px; i++) {
                if (world[i][a.py].equals(Tileset.NOTHING)) {
                    world[i][a.py] = Tileset.WALL;
                }

                world[i][a.py + 1] = Tileset.FLOOR;

                if (world[i][a.py + 2].equals(Tileset.NOTHING)) {
                    world[i][a.py + 2] = Tileset.WALL;
                }
            }
        } else {

            for (int j = a.px; j > b.px; j--) {
                if (world[j][a.py].equals(Tileset.NOTHING)) {
                    world[j][a.py] = Tileset.WALL;
                }
                world[j][a.py + 1] = Tileset.FLOOR;
                if (world[j][a.py + 2].equals(Tileset.NOTHING)) {
                    world[j][a.py + 2] = Tileset.WALL;
                }
            }
        }
    }

    // Creates a corner at (b.px, a.py)

    public void corner(TETile[][] world1, Position a, Position b) {
        if (a.px <= b.px) {
            if (a.py <= b.py) {
                if (world1[b.px][a.py].equals(Tileset.NOTHING)) {
                    world1[b.px][a.py] = Tileset.WALL;
                }
                if (world1[b.px + 1][a.py].equals(Tileset.NOTHING)) {
                    world1[b.px + 1][a.py] = Tileset.WALL;
                }
                if (world1[b.px + 2][a.py].equals(Tileset.NOTHING)) {
                    world1[b.px + 2][a.py] = Tileset.WALL;
                }
                if (world1[b.px + 2][a.py + 1].equals(Tileset.NOTHING)) {
                    world1[b.px + 2][a.py + 1] = Tileset.WALL;
                }
                if (world1[b.px + 2][a.py + 2].equals(Tileset.NOTHING)) {
                    world1[b.px + 2][a.py + 2] = Tileset.WALL;
                }

            } else {

                if (world1[b.px][a.py + 2].equals(Tileset.NOTHING)) {
                    world1[b.px][a.py + 2] = Tileset.WALL;
                }
                if (world1[b.px + 1][a.py + 2].equals(Tileset.NOTHING)) {
                    world1[b.px + 1][a.py + 2] = Tileset.WALL;
                }
                if (world1[b.px + 2][a.py + 2].equals(Tileset.NOTHING)) {
                    world1[b.px + 2][a.py + 2] = Tileset.WALL;
                }
                if (world1[b.px + 2][a.py + 1].equals(Tileset.NOTHING)) {
                    world1[b.px + 2][a.py + 1] = Tileset.WALL;
                }
                if (world1[b.px + 2][a.py].equals(Tileset.NOTHING)) {
                    world1[b.px + 2][a.py] = Tileset.WALL;

                }
            }
        } else {

            if (a.py <= b.py) {
                if (world1[b.px + 1][a.py].equals(Tileset.NOTHING)) {
                    world1[b.px + 1][a.py] = Tileset.WALL;
                }
                if (world1[b.px][a.py].equals(Tileset.NOTHING)) {
                    world1[b.px][a.py] = Tileset.WALL;
                }
                if (world1[b.px][a.py + 1].equals(Tileset.NOTHING)) {
                    world1[b.px][a.py + 1] = Tileset.WALL;
                }
                if (world1[b.px][a.py + 2].equals(Tileset.NOTHING)) {
                    world1[b.px][a.py + 2] = Tileset.WALL;
                }
                if (world1[b.px + 1][a.py].equals(Tileset.NOTHING)) {
                    world1[b.px + 1][a.py] = Tileset.WALL;
                }
            } else {
                if (world1[b.px][a.py + 2].equals(Tileset.NOTHING)) {
                    world1[b.px][a.py + 2] = Tileset.WALL;
                }
                if (world1[b.px][a.py + 1].equals(Tileset.NOTHING)) {
                    world1[b.px][a.py + 1] = Tileset.WALL;
                }
                if (world1[b.px + 1][a.py + 2].equals(Tileset.NOTHING)) {
                    world1[b.px + 1][a.py + 2] = Tileset.WALL;
                }
                if (world1[b.px + 2][a.py + 2].equals(Tileset.NOTHING)) {
                    world1[b.px + 2][a.py + 2] = Tileset.WALL;
                }
                if (world1[b.px][a.py].equals(Tileset.NOTHING)) {
                    world1[b.px][a.py] = Tileset.WALL;
                }
            }
        }
    }


    // Connects Room a and Room b
    public void connect(Room a, Room b) {
        Random r = new Random(123);
        int rax = a.p.px + r.nextInt(a.width - 2) + 1;
        int ray = a.p.py + r.nextInt(a.height - 2) + 1;
        int rbx = b.p.px + r.nextInt(b.width - 2) + 1;
        int rby = b.p.py + r.nextInt(b.height - 2) + 1;
        Position ap = new Position(rax, ray);
        Position bp = new Position(rbx, rby);
        createHorizontal(ap, bp);
        corner(world, ap, bp);
        createVertical(ap, bp);

    }

}


