/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package WormChase;

import GamePanel.GamePanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;


/**
 *
 * @author hardnett
 */
public class WormPanel extends GamePanel {
    private long test = 0;
    private final WormChase wcTop;
    private final Obstacle obs;
    private final Worm fred;
    private final Font wpfont;
    private final FontMetrics metrics;
    private long score = 0;
    
    
    public WormPanel() {
        this(null, 0L);
    } // end of WormPanel()
    
    public WormPanel(WormChase wc, long p) {
        super(p);
        
        wcTop = wc;
        setBackground(Color.WHITE);
        setPreferredSize(new java.awt.Dimension(PWIDTH, PHEIGHT));
        
        // create game components
        obs = new Obstacle(wcTop);
        fred = new Worm(PWIDTH, PHEIGHT, obs);
        
        // NOTE: there is other code in the book, but that is already done in
        // in the base class.
        
        // setup the message font
        wpfont = new Font("SansSerif", Font.BOLD, 24);
        metrics = this.getFontMetrics(wpfont);
        
    } // end of WormPanel(WormChase, long)
            
      /**
     * This is the initialization method that should be overriden 
     * by the derived class. This method will only be called once for setting up
     * game objects.
     */
    @Override
    public void customizeInit() {
       
    }
    /**
     * This is the Render() method that should be overridden during inheritance and
     * customized for your game to handle frame rendering.
     */
    @Override
    public void customizeGameRender() {
        
        if (!super.gameOver) {
            dbg.setColor(Color.WHITE);
            dbg.fillRect(0, 0, super.getWidth(), super.getHeight());

            dbg.setColor(Color.BLUE);
            dbg.setFont(wpfont);

            // report average FPS and UPS at top left
            dbg.drawString("Average FPS/UPS: " + df.format(getAverageFPS()) + "/"
                    + df.format(getAverageUPS()), 20, 25);

            dbg.setColor(Color.BLACK);

            // draw game elements
            obs.draw(dbg);
            fred.draw(dbg);
        }
        
        
    }
    
    /**
     * This is the GameUpdate() method that should be overridden during inheritance
     * and customized for your game to handle frame updates.
     */
    @Override
    public void customizeGameUpdate() {
      System.out.println("Update game state");
      
      // set stats textboxes
      wcTop.setTimeSpent(super.getTimeSpentInGame());
      
      // move the worm
      fred.move();
    }
    
    /**
     * This is the testPresss() method that should be overridden during inheritance
     * and customized for your game to handle mouse events.
     */            
    @Override
     synchronized public void customizeTestPress(int x, int y) {
     
        if (fred.nearHead(x,y)) {
            super.gameOver = true;
            
            score = (40 - super.getTimeSpentInGame()) + 40 - obs.getNumObstacles();
            super.msg = super.msg + " Score: " + df.format(score);
            super.printStats();
        }
        else { // add an obstacle if possible
            if (!fred.touchedAt(x,y)) // was worm's body not touched?
                obs.add(x,y);
        }
    } // end of customizeTestPress(int, int) 
    
    @Override
    protected void preGameLoop() {
        wcTop.setBoxNumber(0); 
    }
    
    @Override
    protected void insideGameLoop() {
        
    }
    
    @Override
    protected void postGameLoop() {
//        super.printStats();
    }

    public Worm getFred() {
        return fred;
    }

    public Obstacle getObs() {
        return obs;
    }

    public WormChase getWcTop() {
        return wcTop;
    }

    
    
}
