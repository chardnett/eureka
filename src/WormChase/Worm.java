/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package WormChase;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;

/**
 *
 * @author hardnett
 */
class Worm {
    
    // the worm structure
    private static final int MAXPOINTS = 40;
    private static final int DOTSIZE = 12;
    private static final int RADIUS = 6; // half the DOTSIZE
    private Point cells[];
    private int nPoints;
    private int tailPosn, headPosn;
    
    // compass direction/bearing constants
    private static final int NUM_DIRS = 8;
    private static final int N  = 0;  // starting north, then going clockwise
    private static final int NE = 1;
    private static final int E  = 2;    
    private static final int SE = 3;
    private static final int S  = 4;
    private static final int SW = 5;
    private static final int W  = 6;
    private static final int NW = 7;    
    
    private int currCompass; // the current compass dir/heading
    
    Point2D.Double incrs[]; // increments for changing worm direction
    
    private static final int NUM_PROBS = 9;    
    private int[] probsForOffset;

    private Obstacle allObstacles;


    public Worm(int PWIDTH, int PHEIGHT, Obstacle obs) {
        // initialize the worm structure
        cells    = new Point[MAXPOINTS];
        nPoints  = 0;
        headPosn = -1;
        tailPosn = -1;
        
        // initialize the diretional increments
        incrs     = new Point2D.Double[NUM_DIRS];
        incrs[N]  = new Point2D.Double(0.0, -1.0);
        incrs[NE] = new Point2D.Double(0.7, -0.7);
        incrs[E]  = new Point2D.Double(0.1,  0.0);
        incrs[SE] = new Point2D.Double(0.7,  0.7);
        incrs[S]  = new Point2D.Double(0.0,  1.0);
        incrs[SW] = new Point2D.Double(-0.7, 0.7);
        incrs[W]  = new Point2D.Double(-1.0, 0.0);
        incrs[NW] = new Point2D.Double(-0.7, 0.7);
        
        // set the random probabilities for an offset
        probsForOffset = new int[NUM_PROBS];
        probsForOffset[0] = 0;
        probsForOffset[1] = 0;        
        probsForOffset[2] = 0;
        probsForOffset[3] = 1;
        probsForOffset[4] = 1;
        probsForOffset[5] = 2;
        probsForOffset[6] = -1;
        probsForOffset[7] = -1;
        probsForOffset[8] = -2;        
        
        // set obstacles
        allObstacles = obs;
    }

    /**
     * is (x,y) near any part of the worm's body?
     * @param x x-coordinate of the position in question
     * @param y y-coordinate of the position in question
     * @return true if it is touching and false if not
     */
    public boolean touchedAt(int x, int y) {
        int i = tailPosn;
        while (i != headPosn) {
          if ( (Math.abs( cells[i].x + RADIUS - x) <= DOTSIZE) &&
               (Math.abs( cells[i].y + RADIUS - y) <= DOTSIZE) )
              return true;
          i = (i+1) % MAXPOINTS;
        }
        return false;
    } // end of touchedAt()


    /**
     * is (x,y) near the worm's head? Collision detection
     * @param x  x-coordinate for the position
     * @param y  y-coordinate for the position
     * @return true/false if within a DOTSIZE of the head
     */
    public boolean nearHead(int x, int y) {
        if (nPoints > 0) {
            if ( (Math.abs( cells[headPosn].x + RADIUS - x) <= DOTSIZE) &&
                  (Math.abs( cells[headPosn].y + RADIUS - y) <= DOTSIZE) )
                return true;
        }
        return false;
    }

    /**
     * Drawing the black worm with a red head
     * @param dbg the canvas to draw on 
     */
    public void draw(Graphics dbg) {
        if (nPoints > 0) {
            dbg.setColor(Color.BLACK);
            int i = tailPosn;
            while (i != headPosn) {
                dbg.fillOval(cells[i].x, cells[i].y, DOTSIZE, DOTSIZE);
                i = (i+1) % MAXPOINTS;
            }
            dbg.setColor(Color.RED);
            dbg.fillOval(cells[headPosn].x, cells[headPosn].y, DOTSIZE, DOTSIZE);
        }
    } // end of draw()
    
    /**
     * Uses the current head position and bearing to determine the new head
     * position
     * @param prevPosn the current head position
     * @param bearing  the direction of the worm
     * @return the point of the new position for the worms head
     */
    private Point nextPoint(int prevPosn, int bearing) {
        // get the increment for th compass bearing
        Point2D.Double incr = incrs[bearing];
        
        // compute the new position using the increments
        int newX = cells[prevPosn].x + (int)(DOTSIZE * incr.x);
        int newY = cells[prevPosn].y + (int)(DOTSIZE * incr.y);
        
        // modify newX/newY if < 0, or > pWidth/pHeight: wrap around mode
        if (newX+DOTSIZE < 0)   // is circle off left edge of canvas?
            newX = newX + WormPanel.PWIDTH;
        else if (newX > WormPanel.PWIDTH)  // is circle off right edge of canvas?
            newX = newX - WormPanel.PWIDTH;
        
        if (newY+DOTSIZE < 0)  // is circle off top of canvas?
            newY = newY + WormPanel.PHEIGHT;
        else if (newY > WormPanel.PHEIGHT) // is circle off the bottom?
            newY = newY - WormPanel.PHEIGHT;
        
        return new Point(newX, newY);
    } // end of nextPoint()

    /**
     * vary the compass bearing kinda randomly
     * @return new bearing
     */
    private int varyBearing() {
        int newOffset = probsForOffset[(int)(Math.random()*NUM_PROBS)];
        return calcBearing(newOffset);
    }

    /**
     * Use the offset to calculate a new compass bearing based on the 
     * current compass direction
     * @param newOffset the current offset
     * @return new compass bearting
     */
    private int calcBearing(int offset) {
        int turn = currCompass + offset;
        // turn must be between 0 - 7
        if (turn >= NUM_DIRS)
            turn = turn - NUM_DIRS;
        else if (turn < 0)
            turn = NUM_DIRS + turn;
        return turn;
    } // end of calcBearing()
    
    
    private void newHead(int prevPosn) {
        int fixedOffs[] = {2, 3, -4}; // offsets to avoid an obstacle
        
        // get the worm going
        int newBearing = varyBearing();
        Point newPt = nextPoint(prevPosn, newBearing);
        
        
//        // Obstacles?
        if (allObstacles.hits(newPt, DOTSIZE)) {
            for (int i = 0; i < fixedOffs.length; i++) {
                newBearing = calcBearing(fixedOffs[i]);
                newPt = nextPoint(prevPosn, newBearing);
                if (!allObstacles.hits(newPt, DOTSIZE))
                    break;
            }
        }
        
        
        // new position and direction
        cells[headPosn] = newPt;
        currCompass = newBearing;
        
    } // end of newhead()
    
    public void move() {
        int prevPosn = headPosn; // save old head posn
        headPosn = (headPosn+1) % MAXPOINTS;
        
        if (nPoints == 0) { // array is empty
            tailPosn = headPosn;
            currCompass = (int)(Math.random()*NUM_DIRS); // random direction
            cells[headPosn] = new Point(WormPanel.PWIDTH/2, WormPanel.PHEIGHT/2);
            nPoints++;
        }
        else if (nPoints == MAXPOINTS) { // array is full
            tailPosn = (tailPosn + 1) % MAXPOINTS; // forget the last tail
            newHead(prevPosn);
        }
        else {
            newHead(prevPosn);
            nPoints++;
        }
    } // end of move()
}
