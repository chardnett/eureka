/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package WormChase;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author hardnett
 */
class Obstacle {
    private static final int BOX_LENGTH = 12;
    private WormChase wcTop;
    private ArrayList<Rectangle> boxes;

    public Obstacle(WormChase wct) {
        wcTop = wct;
        boxes = new ArrayList<Rectangle>();
    }

    synchronized public void add(int x, int y) {
        boxes.add(new Rectangle(x, y, BOX_LENGTH, BOX_LENGTH));
        wcTop.setBoxNumber(boxes.size()); // report the no. of boxes
    } // end of add()

    int getNumObstacles() {
        return boxes.size();
    }

    synchronized public void draw(Graphics dbg) {
        Rectangle box;
        dbg.setColor(Color.BLUE);
        for (int i = 0; i < boxes.size(); i++) {
            box = boxes.get(i);
            dbg.fillRect(box.x, box.y, box.width, box.height);
        }
    } // end of draw()

    synchronized public boolean hits(Point p, int size) {
        Rectangle r = new Rectangle(p.x, p.y,size, size);
        Rectangle box;
        for (int i = 0; i < boxes.size(); i++) {
            box = boxes.get(i);
            if (box.intersects(r))
                return true;
        }
        return false;
    }
    
}
