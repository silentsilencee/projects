package byog.Core;

import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;


public class Room implements Serializable {
    //Create a table containing all rooms and the position of the next room (i)
    static ArrayList<Room> rooms = new ArrayList<>();
    //Position of the bottom left corner of the room
    Position p;
    boolean connected = false;
    int width;
    int height;
    TETile[][] world;
    Random r = new Random(44);


    public Room(TETile[][] world, Position p, int mywidth, int myheight) {
        this.world = world;
        this.p = p;
        height = myheight;
        width = mywidth;
        Room.rooms.add(this);
    }

    public Room() {
        width = 0;
        height = 0;
        world = null;
        p = new Position(0, 0);
    }

    public void draw(TETile[][] word) {
        Room room = new Room(word, new Position(p.px, p.py), width, height);
        for (int y = p.py; y <= p.py + height; y++) {
            for (int x = p.px; x <= p.px + width; x++) {
                if (y == p.py || y == p.py + height) {
                    word[x][y] = Tileset.WALL;
                } else if (x == p.px || x == p.px + width) {
                    word[x][y] = Tileset.WALL;
                } else {
                    word[x][y] = Tileset.FLOOR;
                }
            }
        }
    }

    public Position randoPos() {
        int rpx = this.p.px + r.nextInt(this.width - 1);
        int rpy = this.p.py + r.nextInt(this.height - 1);
        return new Position(rpx, rpy);
    }

    //Is the position included in the room?
    public boolean contains(Position pos) {
        if (pos.px >= this.p.px && pos.px <= this.p.px + this.width) {
            if (pos.py >= this.p.py && pos.py <= this.p.py + this.height) {
                return true;
            }
        }
        return false;
    }


}
