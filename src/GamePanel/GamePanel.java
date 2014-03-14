/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package GamePanel;

import java.awt.Color;
import java.awt.Graphics;
import java.text.DecimalFormat;

/**
 * GamePanel is based on the code from Chapter 2. It includes the main game
 * loop, timing control for FPS/UPS, and keyboard and mouse events.
 * @author hardnett
 */
public class GamePanel extends javax.swing.JPanel implements Runnable{

    // variables for the Game Panel
    public static final int PWIDTH = 800;
    public static final int PHEIGHT = 600;
    
    protected Thread animator;    // process animation
    protected volatile boolean running = false;  // stop animation
    
    protected volatile boolean gameOver = false; // game termination

    protected volatile boolean isPaused = false;
    
    protected java.awt.Graphics dbg;
    protected java.awt.image.BufferedImage dbImage = null;
    
    protected String msg = "THE GAME IS OVER";
    
    // this is the desired FPS
    protected static final long FPS = 32L;
            
    // the length of time for one iteration to meet the desired FPS
    protected long period;
    
    
    // number of frames with a delay of 0 ms before the animation thread
    // yields to other running threads.
    private static final int NO_DELAYS_PER_YIELD = 16;
    
    // # of frames that can be skipped in any  one animation loop
    // i.e. the games state is updated but not rendered
    private static int MAX_FRAMES_SKIPS = 5;
    
    // records stats every 1 seccond 
    private static long MAX_STATS_INTERVAL = 1000L; 
    
    // number of FPS and UPS values stored for stats
    private static int NUM_FPS = 10;
    
    // variables for stats collection
    private long frameCount;
    private long gameStartTime;
    private long timeSpentInGame;
    private long totalElapsedTime;  
    private long prevStatsTime;
    private long framesSkipped;
    
    private final double[] fpsStore;
    private final double[] upsStore;
    
    private long statsInterval = 0L;
    private long totalFramesSkipped = 0L;
    private long statsCount = 0L;
    
    protected DecimalFormat df = new DecimalFormat("0.##"); // 2 dec places
    protected DecimalFormat timedf = new DecimalFormat("0.####"); // 4 dec places
    private double averageFPS;
    private double averageUPS;
    
 
    /**
     * Creates new form GamePanel
     * 
     * @param p The period
     */
    public GamePanel(long p) {
        initComponents();
        
        setFocusable(true);
        requestFocus();     // JPanel now receives the key events
        readyForTermination();
        
        if (p > 0)
            period = p;
        else
            period = 1000L/FPS;
            
        
        
        // listen for mouse events
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                testPress(e.getX(), e.getY()); }

        });
        
        // initialize stats
        fpsStore = new double[NUM_FPS];
        upsStore = new double[NUM_FPS];
        
        for (int i = 0; i < NUM_FPS; i++) {
            fpsStore[i] = 0.0;
            upsStore[i] = 0.0;
        }    
        
         try {
            customizeInit();
                 
         }
         catch (UnsupportedOperationException e){}
        
    }  // end of GamePanel(long)


    
    public GamePanel() {
        this(1000/FPS);
    }
    
    
    
    /**
     * pauses the game
     */
    public void pauseGame() {
        isPaused=true;
    }
    
    /**
     * resumes the game
     */
    public void resumeGame() {
        isPaused = false;
    }

    /**
     * set the boolean gameover that controls the display of the game over message
     * @param gameOver true shows the message / false does not show message
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
    
    
    /**
     * Sets the game over message string
     * @param msg the string containing the game over message
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * obtains the value of the game over flag
     * @return true or false 
     */
    public boolean isGameOver() {
        return gameOver;
    }
    
    public void customizeInit() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * This is the Render() method that should be overridden during inheritance and
     * customized for your game to handle frame rendering.
     */
    public void customizeGameRender() {
            throw new UnsupportedOperationException("customizeGameRender(): Not yet implemented");
    }
    
    /**
     * This is the GameUpdate() method that should be overridden during inheritance
     * and customized for your game to handle frame updates.
     */
    public void customizeGameUpdate() {
            throw new UnsupportedOperationException("customizeGameUpdate(): Not yet implemented");
    }
    
    /**
     * This is the testPresss() method that should be overridden during inheritance
     * and customized for your game to handle mouse events.
     */            
    public void customizeTestPress(int x, int y) {
        throw new UnsupportedOperationException("customizeTestPress(): Not yet implemented");
    }
    
    
    /*
     * this function is called before the gameloop starts in run()
     */
    protected void preGameLoop() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /*
     * this function is called after the gameloop ends in run()
     */
    protected void postGameLoop() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /*
     * this function is called within the gameloop in run()
     */
    protected void insideGameLoop() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    /**
     * is (x,y) important to the game?
     * @param x the x-coordinate of the mouse
     * @param y the y-coordinate of the mouse
     */
     private void testPress(int x, int y) {
         if (!isPaused && !gameOver) {
             // do something with x and y
             try {
                 customizeTestPress(x, y);
                 
             }
             catch (UnsupportedOperationException e){}
         }
     } // end of testPresss()
    
    
    /**
     * wait for the JPanel to be added to the JFrame/JApplet before starting.
     */
    @Override
    public void addNotify() {
        System.out.println("add Notify executed");
        super.addNotify(); // creates the peer
        startGame();        
    } // end of addNotify()
    
    /**
     * initialize and stat the thread
     */
    private void startGame() {
        if (animator == null || !running) {
            animator = new Thread(this);
            animator.start();
        }
    }  // end of startGame()
    
    
    
    /**
     * called by the user to stop execution
     */
    public void stopGame() {
        running = false;
    } // end of stopGame()
    
    
    /**
     * Repeatedly update, render, sleep so loop takes close to period of 
     * nsecs. Sleep inaccuracies are handled. The timing calculation uses the 
     * Java 3D timer.
     */
    @Override
    public void run() {
            // stats variables
        long skips;
        long beforeTime, afterTime, 
            timeDiff, sleepTime;
        long overSleepTime = 0L;
        int noDelays = 0;
        long excess = 0L;
           
        
        
        gameStartTime = com.sun.j3d.utils.timer.J3DTimer.getValue();
        prevStatsTime = gameStartTime;
        beforeTime = gameStartTime;
       
        running = true;
        
        try {
            preGameLoop();
        }
        catch(UnsupportedOperationException e){}
        
        
        while(running) {
            gameUpdate(); // game state is updated
            gameRender(); // render to a buffer
            paintScreen(); // draw buffer to screen
            
            afterTime = com.sun.j3d.utils.timer.J3DTimer.getValue();
            timeDiff = afterTime - beforeTime;
            sleepTime = (period - timeDiff) - overSleepTime;
            
            if (sleepTime > 0){
                try {
                    Thread.sleep(sleepTime/1000000L); // nano -> ms
                }
                catch(InterruptedException ex) {
                    overSleepTime = (com.sun.j3d.utils.timer.J3DTimer.getValue() -
                            afterTime) - sleepTime;
            
                }
            }
            else {
                excess -= sleepTime;  // store excess time value
                overSleepTime = 0L;
                
                if (++noDelays >= NO_DELAYS_PER_YIELD) {
                    Thread.yield();  // give anothe rthread a chance to run
                    noDelays = 0;
                }
            }
            
            beforeTime = com.sun.j3d.utils.timer.J3DTimer.getValue();
            
            /*
             * if frame animation is taking too long, update the game state
             * without rendering it, to get the updates/sec nearer to the 
             * required FPS.
             */
            skips = 0L;
            while ((excess > period) && (skips < MAX_FRAMES_SKIPS) ) {
                excess -= period;
                gameUpdate(); // update state but don't render
                skips++;
            }
            
            framesSkipped += skips;
            storeStats();
            
            try {
                insideGameLoop();
            }
            catch (UnsupportedOperationException e){}
        }
        
        try {
            postGameLoop();
        }
        catch (UnsupportedOperationException e) {}
        
        System.exit(0); // so the enclosing JFrame/Japplet exits
    } // end of run()
    
    
    /**
     * updates the state of the game.
     */
    private void gameUpdate() {
        if (!isPaused && !gameOver) {
            
            try {
            // calls the customized game update method.
            customizeGameUpdate();
            }
            catch(UnsupportedOperationException e) {
                System.out.println("Update game state");
            }
        }
    } // end of gameUpdate
    
    
    /**
     * draw the current frame to an image buffer
     */    
    private void gameRender() {
        

        
        if (dbImage == null) {
            dbImage = (java.awt.image.BufferedImage) createImage(PWIDTH, PHEIGHT);
            
            if (dbImage == null) {
                System.out.println("Error: dbImage is null");
                System.exit(1);
//                return;
            }
        } else {
            dbg = dbImage.getGraphics();
        }
        
        // draw game elements
        if (gameOver) {
            gameOverMessage(dbg);
        }
        
        try {
            // calls the customization method for rendering
           if (dbg != null) {
               customizeGameRender();
           }
        }
        catch (UnsupportedOperationException e) {
            System.out.println("Render game elements");            
        }
    } // end of gameRender()
    
    
    /**
     * shows the game over message on the screen
     * @param g Graphics object to draw game over message onto
     */
    protected void gameOverMessage(java.awt.Graphics g) {

        if (g != null) {
            // need font size
            java.awt.Font theFont = g.getFont();

            // code to calculate the x and y
            int sizePerChar = theFont.getSize();

            int x = (PWIDTH - (msg.length() / 2 * sizePerChar)) / 2;
            int y = PHEIGHT / 2;
            g.setColor(java.awt.Color.BLACK);
            g.drawString(msg, x, y);
        }
        
    } // end of gameOverMessage
    
    @Override
    public void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        
        if (dbImage != null)
            g.drawImage(dbImage, 0, 0, null);
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setPreferredSize(new java.awt.Dimension(PWIDTH, PHEIGHT));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    /**
     * watches key presses that signal termination of the game
     */
    private void readyForTermination() {
        addKeyListener(new java.awt.event.KeyAdapter() {
            // list for esc, q, end, ctrl-c
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int keyCode = e.getKeyCode();
                if ((keyCode == java.awt.event.KeyEvent.VK_ESCAPE) ||
                    (keyCode == java.awt.event.KeyEvent.VK_Q) ||
                    (keyCode == java.awt.event.KeyEvent.VK_END) ||
                    ((keyCode == java.awt.event.KeyEvent.VK_C) && e.isControlDown())) {
                    running = false;
                }
            }
        });
    } // end of readyForTermination()

    
    /**
     * actively render the buffer image to the screen
     */
    private void paintScreen() {
        java.awt.Graphics g;
        
        try {
            g = this.getGraphics(); // get the panel's graphic context
            if ((g != null) && (dbImage != null))
                g.drawImage(dbImage, 0, 0, null);
            
            java.awt.Toolkit.getDefaultToolkit().sync(); // sync the display
            g.dispose();
        }
        catch(Exception e) {
            System.out.println("Graphics context error: " + e);
        }
    }
    
    
    /**
     * gathers and stores the stats into the FPS and UPS arrays. It also
     * computes the average and total FPS and UPS.
     */
    private void storeStats() {
        frameCount++;
        statsInterval += period;

        if (statsInterval >= MAX_STATS_INTERVAL) {
            long timeNow = com.sun.j3d.utils.timer.J3DTimer.getValue();
            timeSpentInGame = (int) ((timeNow - gameStartTime)/1000000000L);
        
        
            long realElapsedTime = timeNow - prevStatsTime;
            // time since last stats collection
            totalElapsedTime += realElapsedTime;

            double timingError = ((double) 
                    (realElapsedTime - statsInterval) / statsInterval)*100.0;
            totalFramesSkipped += framesSkipped;

            double actualFPS = 0.0; // calculate the latest FPS and UPS
            double actualUPS = 0.0;

            if (totalElapsedTime > 0) {
                actualFPS = (((double) frameCount / totalElapsedTime) * 1000000000L);
                actualUPS = (((double) (frameCount + totalFramesSkipped)
                        / totalElapsedTime) * 1000000000L);
            }
            
            // store the lastest FPS and UPS
            fpsStore[(int)statsCount%NUM_FPS] = actualFPS;
            upsStore[(int)statsCount%NUM_FPS] = actualUPS;
            statsCount++;
            
            double totalFPS = 0.0;
            double totalUPS = 0.0;
            
            for (int i=0; i < NUM_FPS; i++) {
                totalFPS += fpsStore[i];
                totalUPS += upsStore[i];
            }
            
            if (statsCount < NUM_FPS && statsCount > 0) {
                averageFPS = totalFPS/statsCount;
                averageUPS = totalUPS/statsCount;                                
            } else if (statsCount > 0) {
                averageFPS = totalFPS/NUM_FPS;
                averageUPS = totalUPS/NUM_FPS;                
            } else
                throw new UnsupportedOperationException("Something is wrong with"
                        + " statsCount. It may be 0");
            framesSkipped = 0;
            prevStatsTime = timeNow;
            statsInterval = 0L;
        }
    } // end of storeStats()

    
    
    public void printStats() {
       System.out.println("Frame Count/Loss: " + frameCount + " / " + 
               totalFramesSkipped);
       System.out.println("Average FPS: " + df.format(averageFPS));
       System.out.println("Average UPS: " + df.format(averageUPS));
       System.out.println("Time Spent: " + timeSpentInGame + " secs");
    } // end of printStats()

    public long getFrameCount() {
        return frameCount;
    }

    public long getFramesSkipped() {
        return framesSkipped;
    }

    public long getGameStartTime() {
        return gameStartTime;
    }

    public long getTimeSpentInGame() {
        return timeSpentInGame;
    }

    public long getTotalElapsedTime() {
        return totalElapsedTime;
    }

    public long getTotalFramesSkipped() {
        return totalFramesSkipped;
    }

    public double getAverageFPS() {
        return averageFPS;
    }

    public double getAverageUPS() {
        return averageUPS;
    }

 

    
    
   
}  // end of paintScreen()
