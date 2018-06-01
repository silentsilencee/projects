package byog.Core;

import java.io.Serializable;
import java.util.Random;

//Add the postition class with has 2 instances px and py and no methods.
public class Position implements Serializable {
    int px;
    int py;
    Random r = new Random(44);

    public Position(int px, int py) {
        this.px = px;
        this.py = py;
    }

    public Position() {
        this.px = 0;
        this.py = 0;
    }

    public boolean equals(Position b) {
        if (px == b.px && py == b.py) {
            return true;
        }
        return false;
    }
}




