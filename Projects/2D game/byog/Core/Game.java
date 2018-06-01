package byog.Core;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Font;
import java.awt.Color;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;


public class Game implements Serializable {
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    static Game g = new Game();
    TERenderer ter = new TERenderer();
    TETile[][] world = new TETile[WIDTH][HEIGHT];
    Room[] rooms = new Room[WIDTH * HEIGHT];
    int sizerooms = 0;
    Random r;
    int life = 5;
    Position door;
    Position player;
    TETile original = Tileset.FLOOR;
    boolean win = false;

    public static boolean checkrectangle(Position rp, int rw, int rh, TETile[][] world1) {
        for (int y = rp.py; y <= rp.py + rh; y++) {
            for (int x = rp.px; x <= rp.px + rw; x++) {
                if (!world1[x][y].equals(Tileset.NOTHING)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void serializer() {
        String filename = "saveworld.txt";
        //Serialization
        try {
            //Saving of object in a file
            FileOutputStream file = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(file);

            out.writeObject(this);
            out.close();
            System.out.println("Game has been saved");
        } catch (IOException ex) {
            System.out.println("IOException is caught");
        }
    }

    public void loader() {
        try {
            FileInputStream f = new FileInputStream("saveworld.txt");
            ObjectInputStream is = new ObjectInputStream(f);
            Game game = (Game) is.readObject();
            this.world = game.world;
            this.rooms = game.rooms;
            this.sizerooms = game.sizerooms;
            this.r = game.r;
            this.player = game.player;
            is.close();
            System.out.println(TETile.toString(this.world));
            ter.initialize(WIDTH, HEIGHT);
            ter.renderFrame(this.world);
        } catch (IOException ex) {
            System.out.println(ex);
            System.out.println("IOException is caught");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void createWorld(TETile[][] world1, Random ra) {
        //We want at least 5 rooms/hallways
        int n = 5 + ra.nextInt(5);

        //Keeps track of the last & first rooms created.
        Room first = new Room();
        Room last = new Room();
        Hallway h = new Hallway(world1);
        int i = 0;

        while (n > 0) {
            //Generate a random position and random measurement for the ith room
            int randlarge = 4 + ra.nextInt(5);
            int randlonge = 4 + ra.nextInt(5);
            int randpx = ra.nextInt(WIDTH - randlarge);
            int randpy = ra.nextInt(HEIGHT - randlonge);
            Position randpos = new Position(randpx, randpy);

            if (checkrectangle(randpos, randlarge, randlonge, world1)) {
                //Create a new room and draw it
                Room a = new Room(world1, randpos, randlarge, randlonge);
                rooms[sizerooms] = a;
                sizerooms += 1;
                a.draw(world1);
                n = n - 1;
                Room current = a;
                if (i == 0) {

                    first = a;
                }
                i = i + 1;
                //Creates a hallway connecting the last and current room and draw it.
                if (i > 1) {
                    h.connect(last, current);
                }
                last = current;
            }
        }
        h.connect(last, first);
    }

    /**
     * Method used for playing a fresh game. The game should start from the main menu.
     */
    public void playWithKeyboard() {
        drawMain();
        boolean a = false;
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char nextKey = StdDraw.nextKeyTyped();
                if (nextKey == 'n' || nextKey == 'N') {
                    long seed = helperenterSEED();
                    Random ra = new Random(seed);
                    this.r = ra;
                    TETile[][] finalWorldFrame = displayWorld(ra);
                    this.world = finalWorldFrame;
                    door(finalWorldFrame, ra, 0);
                    waters(finalWorldFrame, ra, 4);
                    flower(finalWorldFrame, ra);
                    Position start = randPos(world, ra);
                    this.player = start;
                    playermovement(world, start);
                } else if (nextKey == ':') {
                    a = true;
                } else if (nextKey == 's' || nextKey == 'S') {
                    serializer();
                } else if (nextKey == 'l' || nextKey == 'L') {
                    loader();
                    ter.initialize(WIDTH, HEIGHT);
                    ter.renderFrame(this.world);
                    Position start = randPos(world, r);
                    playermovement(world, start);
                }
            }
        }
    }


    public void playermovement(TETile[][] world1, Position p) {
        int positionx = p.px;
        int positiony = p.py;
        world1[positionx][positiony] = Tileset.PLAYER;
        StdDraw.clear(StdDraw.BLACK);
        ter.renderFrame(world1);
        boolean a = false;
        while (true) {
            drawHUD(world1);
            StdDraw.clear(StdDraw.BLACK);
            ter.renderFrame(world1);
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (a) {
                    if (c == 'q' || c == 'Q') {
                        serializer();
                        System.exit(0);
                    }
                }
                if (this.life <= 0) {
                    StdDraw.clear(StdDraw.DARK_GRAY);
                    Font font = new Font("Arial", Font.BOLD, 30);
                    StdDraw.setFont(font);
                    StdDraw.setPenColor(StdDraw.BOOK_RED);
                    StdDraw.text(WIDTH/2, HEIGHT/2, "You don't have life left");
                    StdDraw.text(WIDTH/2, HEIGHT/3, "GAME OVER");
                    StdDraw.show();
                    break;
                }

                if (c == 'w' || c == 'W') {
                    moveahead(world1, this.player);
                    if (this.win) {
                        return;
                    }
                    StdDraw.clear(StdDraw.BLACK);
                    ter.renderFrame(world1);
                }
                if (c == 'a' || c == 'A') {
                    moveleft(world1, this.player);
                    if (this.win) {
                        return;
                    }
                    StdDraw.clear(StdDraw.BLACK);
                    ter.renderFrame(world1);
                }
                if (c == 's' || c == 'S') {
                    movedown(world1, this.player);
                    if (this.win) {
                        return;
                    }
                    StdDraw.clear(StdDraw.BLACK);
                    ter.renderFrame(world1);
                }
                if (c == 'd' || c == 'D') {
                    moveright(world1, this.player);
                    if (this.win) {
                        return;
                    }
                    StdDraw.clear(StdDraw.BLACK);
                    ter.renderFrame(world1);
                } else if (c == ':') {
                    a = true;
                }
            }
        }
    }


    public long helperenterSEED() {
        StdDraw.clear(StdDraw.BLACK);
        Font font = new Font("Arial", Font.BOLD, 20);
        StdDraw.setFont(font);
        StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
        StdDraw.text(0.5, 0.5, "Type the SEED and then press s:");
        String input = new String("");
        boolean checker = true;
        double x = 0.2;
        while (checker) {
            if (StdDraw.hasNextKeyTyped()) {
                char nextKey = StdDraw.nextKeyTyped();
                if (nextKey == '0') {
                    input += '0';
                    x += 0.03;
                    StdDraw.text(x, 0.4, "0");
                }
                if (nextKey == '1') {
                    input += '1';
                    x += 0.03;
                    StdDraw.text(x, 0.4, "1");

                }
                if (nextKey == 50) {
                    input += '2';
                    x += 0.03;
                    StdDraw.text(x, 0.4, "2");

                }
                if (nextKey == 51) {
                    input += '3';
                    x += 0.03;
                    StdDraw.text(x, 0.4, "3");

                }
                if (nextKey == 52) {
                    input += '4';
                    x += 0.03;
                    StdDraw.text(x, 0.4, "4");

                }
                if (nextKey == 53) {
                    input += '5';
                    x += 0.03;
                    StdDraw.text(x, 0.4, "5");

                }
                if (nextKey == 54) {
                    input += '6';
                    x += 0.03;
                    StdDraw.text(x, 0.4, "6");

                }
                if (nextKey == 55) {
                    input += '7';
                    x += 0.03;
                    StdDraw.text(x, 0.4, "7");

                }
                if (nextKey == 56) {
                    input += '8';
                    x += 0.03;
                    StdDraw.text(x, 0.4, "8");

                }
                if (nextKey == 57) {
                    input += '9';
                    x += 0.03;
                    StdDraw.text(x, 0.4, "9");

                }
                if (nextKey == 's') {
                    checker = false;
                }
            }
        }
        long seed = Long.parseLong(input);
        return seed;
    }

    /**
     * Method used for autograding and testing the game code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The game should
     * behave exactly as if the user typed these characters into the game after playing
     * playWithKeyboard. If the string ends in ":q", the same world should be returned as if the
     * string did not end with q. For example "n123sss" and "n123sss:q" should return the same
     * world. However, the behavior is slightly different. After playing with "n123sss:q", the game
     * should save, and thus if we then called playWithInputString with the string "l", we'd expect
     * to get the exact same world back again, since this corresponds to loading the saved game.
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */


    public TETile[][] playWithInputString(String input) {
        //and return a 2D tile representation of the world that would have been
        //drawn if the same inputs had been given to playWithKeyboard().
        boolean a = false;
        String save = input;

        if (input.endsWith(":q")) {
            input = input.substring(0, input.length() - 2);
            a = true;
        }
        char begin = input.charAt(0);
        String number = new String();
        String ending = new String();
        if ((begin == 'n' || begin == 'N') && input.length() > 0) {
            for (int i = 1; i < input.length(); i++) {
                if (input.charAt(i) >= '0' && input.charAt(i) <= '9') {
                    number += input.charAt(i);
                } else {
                    ending += input.charAt(i);
                }
            }
            if (ending.length() > 0 && ending.charAt(0) == 's') {
                ending = ending.substring(1, ending.length());
            } else if (ending.charAt(0) != 's') {
                System.out.print("End your input with s to start the game");
            }
            long seed = Long.parseLong(number);
            final Random random = new Random(seed);
            this.r = random;
            TETile[][] finalWorldFrame = displayWorld(random);
            createWorld(finalWorldFrame, random);
            this.world = finalWorldFrame;
            door(finalWorldFrame, random, 0);
            waters(finalWorldFrame, random, 1);
            flower(finalWorldFrame, random);

            Position start = randPos(finalWorldFrame, random);
            this.player = start;
            player(finalWorldFrame, ending, start);
            if (a) {
                serializer();
            }
            return this.world;
        }
        if (begin == 'l' || begin == 'L') {
            loader();
            if (input.length() > 1) {
                player(this.world, input.substring(1), player);
                if (input.endsWith(":q")) {
                    playWithInputString(":q");
                }
            }
        } else if (begin == 'q' || begin == 'Q') {
            return null;
        } else {
            System.out.print("invalid input");
        }
        return this.world;
    }

    //Return a new world initialized with NOTHING tiles
    public TETile[][] initoNothing() {

        //all tiles to NOTHING
        TETile[][] world1 = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                world1[x][y] = Tileset.NOTHING;
            }
        }
        return world1;
    }


    public void drawMain() {
        StdDraw.clear(StdDraw.BLACK);
        Font font = new Font("Arial", Font.BOLD, 40);
        StdDraw.setFont(font);
        StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
        StdDraw.text(0.5, 0.4, "Quit (Q)");
        StdDraw.text(0.5, 0.5, "Load previous game (L)");
        StdDraw.text(0.5, 0.6, "New game (N)");
        Font font2 = new Font("Arial", Font.PLAIN, 40);
        StdDraw.setPenColor(StdDraw.GRAY);
        StdDraw.setFont(font2);
    }

    public void drawHUD(TETile[][] world1) {
        Font hud1 = new Font("Arial", Font.PLAIN, 5);
        Font hud2 = new Font("Arial", Font.ITALIC, 5);
        StdDraw.setPenColor(Color.YELLOW);
        StdDraw.textLeft(0, 0.1, "Life : " + Integer.toString(this.life));
        StdDraw.textRight(40, 1, "Mouse : " + mouseover(world1));
        StdDraw.show();
    }


    public String mouseover(TETile[][] world1) {
        double d = StdDraw.mouseX();
        Long l = Math.round(d);
        int xmouse = Integer.valueOf(l.intValue());

        double d2 = StdDraw.mouseY();
        Long l2 = Math.round(d2);
        int ymouse = Integer.valueOf(l2.intValue()) - 3;
        if (xmouse <= WIDTH && xmouse >=0 && ymouse <= HEIGHT  && ymouse >= 0) {
            if (world1[xmouse][ymouse].equals(Tileset.WALL)) {
                return "Wall hurts";
            } else if (world1[xmouse][ymouse].equals(Tileset.WATER)) {
                return "Water to stay healthy";
            } else if (world1[xmouse][ymouse].equals(Tileset.FLOOR)) {
                return "Floor";
            } else if (world1[xmouse][ymouse].equals(Tileset.FLOWER)) {
                return "Flower is the key";
            } else if (world1[xmouse][ymouse].equals(Tileset.LOCKED_DOOR)) {
                return "Door to unlock";
            } else if (world1[xmouse][ymouse].equals(Tileset.UNLOCKED_DOOR)) {
                return "Unlocked door is your way to victory";
            }
            return "";
        }
        return "";
    }

    public TETile[][] displayWorld(Random ra) {
        StdDraw.clear(StdDraw.BLACK);
        Font font = new Font("Arial", Font.BOLD, 20);
        StdDraw.setFont(font);
        StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);

        TETile[][] world1 = initoNothing();
        ter.initialize(WIDTH, HEIGHT, 0, 3);
        createWorld(world1, ra);
        drawHUD(world1);
        ter.renderFrame(world1);
        return world1;
    }

    public void moveahead(TETile[][] world1, Position p) {
        int positionx = p.px;
        int positiony = p.py;

        TETile togo = world1[positionx][positiony + 1];

        if (togo.equals(Tileset.FLOOR) || togo.equals(Tileset.NOTHING)) {
            world1[positionx][positiony] = this.original;
            positiony += 1;
            world1[positionx][positiony] = Tileset.PLAYER;
            player.py += 1;
            StdDraw.clear(StdDraw.BLACK);
            ter.renderFrame(world1);
        } else if (world1[positionx][positiony + 1].equals(Tileset.WALL)) {
            lifelost1();
        } else if (world1[positionx][positiony + 1].equals(Tileset.WATER)) {
            world1[positionx][positiony] = Tileset.FLOOR;
            world1[positionx][positiony + 1] = Tileset.PLAYER;
            drinkwater(world1[positionx][positiony + 1]);
            this.player = new Position(player.px, player.py + 1);
        } else if (world1[positionx][positiony + 1].equals(Tileset.FLOWER)) {
            world1[positionx][positiony] = Tileset.FLOOR;
            world1[positionx][positiony + 1] = Tileset.PLAYER;
            this.player = new Position(player.px, player.py + 1);
            unlock(world1);
        } else if (togo.equals(Tileset.UNLOCKED_DOOR)) {
            winning();
        }
    }

    public void moveleft(TETile[][] world1, Position p) {
        int positionx = p.px;
        int positiony = p.py;


        TETile togo = world1[positionx - 1][positiony];
        if (togo.equals(Tileset.FLOOR) || togo.equals(Tileset.NOTHING)) {
            world1[positionx][positiony] = this.original;
            positionx -= 1;
            world1[positionx][positiony] = Tileset.PLAYER;
            StdDraw.clear(StdDraw.BLACK);
            ter.renderFrame(world1);
            player.px -= 1;
        } else if (world1[positionx - 1][positiony].equals(Tileset.WALL)) {
            lifelost1();
        } else if (world1[positionx - 1][positiony].equals(Tileset.WATER)) {
            world1[positionx][positiony] = Tileset.FLOOR;
            world1[positionx - 1][positiony] = Tileset.PLAYER;
            this.player = new Position(player.px - 1, player.py);
            drinkwater(world1[positionx - 1][positiony]);
        } else if (world1[positionx - 1][positiony].equals(Tileset.FLOWER)) {
            world1[positionx][positiony] = Tileset.FLOOR;
            world1[positionx - 1][positiony] = Tileset.PLAYER;
            this.player = new Position(player.px - 1, player.py);
            unlock(world1);
        } else if (togo.equals(Tileset.UNLOCKED_DOOR)) {
            winning();
        }
    }

    public void moveright(TETile[][] world1, Position p) {
        int positionx = p.px;
        int positiony = p.py;

        TETile togo = world1[positionx + 1][positiony];
        if (togo.equals(Tileset.FLOOR) || togo.equals(Tileset.NOTHING)) {
            world1[positionx][positiony] = this.original;
            positionx += 1;
            world1[positionx][positiony] = Tileset.PLAYER;
            player.px += 1;
            StdDraw.clear(StdDraw.BLACK);
            ter.renderFrame(world1);
        } else if (world1[positionx + 1][positiony].equals(Tileset.WALL)) {
            lifelost1();
        } else if (world1[positionx + 1][positiony].equals(Tileset.WATER)) {
            world1[positionx][positiony] = Tileset.FLOOR;
            world1[positionx + 1][positiony] = Tileset.PLAYER;
            drinkwater(world1[positionx + 1][positiony]);
            this.player = new Position(player.px + 1, player.py);
        } else if (world1[positionx + 1][positiony].equals(Tileset.FLOWER)) {
            world1[positionx][positiony] = Tileset.FLOOR;
            world1[positionx + 1][positiony] = Tileset.PLAYER;
            unlock(world1);
            this.player = new Position(player.px + 1, player.py);
        } else if (togo.equals(Tileset.UNLOCKED_DOOR)) {
            winning();
        }
    }

    public void movedown(TETile[][] world1, Position p) {
        int positionx = this.player.px;
        int positiony = this.player.py;

        TETile togo = world1[positionx][positiony - 1];
        if (togo.equals(Tileset.FLOOR)|| togo.equals(Tileset.NOTHING)) {
            world1[positionx][positiony] = this.original;
            positiony -= 1;
            world1[positionx][positiony] = Tileset.PLAYER;
            player.py -= 1;
            StdDraw.clear(StdDraw.BLACK);
            ter.renderFrame(world1);
        } else if ((world1[positionx][positiony - 1].equals(Tileset.WALL))) {
            lifelost1();
        } else if ((world1[positionx][positiony - 1].equals(Tileset.WATER))) {
            world1[positionx][positiony] = Tileset.FLOOR;
            world1[positionx][positiony - 1] = Tileset.PLAYER;
            this.player = new Position(player.px, player.py - 1);
            drinkwater(world1[positionx][positiony - 1]);
        } else if ((world1[positionx][positiony - 1].equals(Tileset.FLOWER))) {
            world1[positionx][positiony] = Tileset.FLOOR;
            world1[positionx][positiony - 1] = Tileset.PLAYER;
            this.player = new Position(player.px, player.py - 1);
            unlock(world1);
        } else if (togo.equals(Tileset.UNLOCKED_DOOR)) {
            winning();
        }
    }

    public void player(TETile[][] world1, String movements, Position p) {
        int positionx = p.px;
        int positiony = p.py;
        world1[positionx][positiony] = Tileset.PLAYER;
        StdDraw.clear(StdDraw.BLACK);
        ter.renderFrame(world1);
        for (int i = 0; i < movements.length(); i++) {
            if (this.life <= 0) {
                StdDraw.clear(StdDraw.DARK_GRAY);
                StdDraw.setPenColor(StdDraw.BOOK_RED);
                StdDraw.text(20, 20, "You don't have life left");
                StdDraw.text(30, 30, "GAME OVER");
                StdDraw.show();
            }
            char c = movements.charAt(i);
            if (c == 'w' || c == 'W') {
                moveahead(world1, this.player);
                StdDraw.clear(StdDraw.BLACK);
                ter.renderFrame(world1);
            }
            if (c == 'a' || c == 'A') {
                StdDraw.clear(StdDraw.BLACK);
                ter.renderFrame(world1);
                moveleft(world1, this.player);
            }
            if (c == 's' || c == 'S') {
                movedown(world1, this.player);
                StdDraw.clear(StdDraw.BLACK);
                ter.renderFrame(world1);
            }
            if (c == 'd' || c == 'D') {
                moveright(world1, this.player);
                StdDraw.clear(StdDraw.BLACK);
                ter.renderFrame(world1);
            }
        }
        StdDraw.clear(StdDraw.BLACK);
        ter.renderFrame(world1);
    }

    public void winning() {
        this.win = true;
        StdDraw.clear(StdDraw.YELLOW);
        Font f = new Font("Arial", Font.BOLD,30);
        StdDraw.setFont(f);
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.text(WIDTH/2, HEIGHT/2, "You won the game");
        StdDraw.show();
        this.world = null;

    }

    public void lifelost1() {
        this.life -= 1;
        if (this.life <= 0) {
            StdDraw.clear(StdDraw.DARK_GRAY);
            StdDraw.setPenColor(StdDraw.BOOK_RED);
            StdDraw.text(0.5, 0.5, "You don't have life left");
            StdDraw.text(0.5, 0.4, "GAME OVER");
            StdDraw.show();
            this.world = null;
        }
    }

    public void drinkwater(TETile water) {
        water = Tileset.FLOOR;
        this.life += 1;
    }

    public void waters(TETile[][] world1, Random ra, int n) {
        for (int i = 0; i < n; i++) {
            Position p = randPos(world1, ra);
            world1[p.px][p.py] = Tileset.WATER;
        }

    }

    public void flower(TETile[][] world1, Random ra) {
        Position p = randPos(world1, ra);
        world1[p.px][p.py] = Tileset.FLOWER;

    }

    public void door(TETile[][] world1, Random ra, int i) {
        //This is the start of the game
        if (i == 0) {
            Position p = randPos(world1, ra);
            world1[p.px][p.py] = Tileset.LOCKED_DOOR;
            this.door = p;
        }
        if (i == 1) {
            world1[this.door.px][this.door.py] = Tileset.FLOOR;
            Position p = randPos(world1, ra);
            world1[p.px][p.py] = Tileset.LOCKED_DOOR;
            this.door = p;
        }
        StdDraw.show();
    }

    private void unlock(TETile[][] world1) {
        world1[this.door.px][this.door.py] = Tileset.UNLOCKED_DOOR;
        StdDraw.show();
    }

    public Position randPos(TETile[][] world1, Random ra) {
        int positionx = ra.nextInt(WIDTH);
        int positiony = ra.nextInt(HEIGHT);
        while (!world1[positionx][positiony].equals(Tileset.FLOOR)) {
            positionx = ra.nextInt(WIDTH);
            positiony = ra.nextInt(HEIGHT);
        }
        Position p = new Position(positionx, positiony);
        return p;
    }

}



