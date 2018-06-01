package byog.SaveDemo;

import edu.princeton.cs.introcs.StdDraw;

import java.awt.Font;
import java.awt.Color;
import java.io.Serializable;

public class Square implements Serializable {
    private static final long serialVersionUID = 45498234798734234L;
    private double x;
    private double y;
    private double size;
    private Color c;


    public Square(double xp, double yp, double sizep, Color cp) {
        x = xp;
        y = yp;
        size = sizep;
        c = cp;
    }

    public void draw() {
        StdDraw.setPenColor(c);
        StdDraw.filledSquare(x, y, size);
    }
}
