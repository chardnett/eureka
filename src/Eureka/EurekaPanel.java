/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Eureka;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DecimalFormat;

/**
 * EurekaPanel is based on the code from Chapter 2. It includes the main game
 * loop, timing control for FPS/UPS, and keyboard and mouse events.
 * @author hardnett
 */
public abstract class EurekaPanel extends javax.swing.JPanel implements Runnable{

    /**
     * The width of the game panel where the screen is drawn
     */
    public static final int PWIDTH = 800;
    private static int panelWidth = 800;

    
     /**
     * The height of the game panel where the screen is drawn
     */
    public static final int PHEIGHT = 600;
    private static int panelHeight = 600;
    
    /**
     * The thread that is dedicated to run the game loop.
     * @see #run() 
     */
    protected Thread animator;  
    
    /**
     * The flag used by the game loop to determine if it should continue to execute. 
     * When this is true the game loop executes, and when its false the game loop 
     * terminates.
     */
    protected volatile boolean running = false;  

    /**
     * The flag to determine if the game over message should be displayed.
     */
    protected volatile boolean gameOver = false; 
    
    /**
     * The flag to determine if the game is currently paused. When the game is paused 
     * the methods in the game loop are not called.
     * @see #run() 
     */
    protected volatile boolean isPaused = false;
    
    /**
     * The canvas for drawing a frame of the game. You will use this object in 
     * your version of the customizeGameRender() method of your derived class. 
     * @see #customizeGameRender() 
     */
    protected java.awt.Graphics2D theCanvas;
    
    /**
     * The background double buffered image. The screen is drawn here by the game
 engine before it is painted onto the theCanvas.
     */
    protected java.awt.image.BufferedImage theCanvasBufferedImage = null;
    
    /**
     * This is the string that holds the game over message.
     */
    protected String gameOverMessage = "THE GAME IS OVER";
    
    /**
     * This is the desired frames per second (FPS)
     */
    protected static final long FPS = 32L;
            
    /**
     * The length of time for one iteration to meet the desired FPS
     */
    protected long period;
    
    /**
     * The number of frames with a delay of 0 ms before the animation thread
     * yields to other running threads.
     */
    private static final int NO_DELAYS_PER_YIELD = 16;
    
    /**
     * The # of frames that can be skipped in any  one animation loop
     * the games state is updated but not rendered.
     */
 
    private static int MAX_FRAMES_SKIPS = 5;

    /**
     * The frequency for storing performance statistics is set to 1 second
     */
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
     * This constructor creates new form GamePanel using the period passed in as
     * a parameter. It also initializes the JPanel for the game, sets up the
     * MouseListner, initializes the FPS and UPS stats, and it calls the 
     * customizeInit() method.
     * @see #customizeInit() 
     * @see #storeStats() 
     * @param p The period
     */
    public EurekaPanel(long p) {
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
                mousePress(e.getX(), e.getY()); }

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
        
    }  // end of EurekaPanel(long)


    /***
     * The default constructor that sets period to the default period of the game
     * loop to 1000/FPS ms. 
     */
    public EurekaPanel() {
        this(1000/FPS);
    }
    
    
    
    /**
     * This method places the game loop in a paused state. In the paused state 
     * there are no updates to the game play or screen.
     */
    public void pauseGame() {
        isPaused=true;
    }
    
    /**
     * This method resumes the game from its paused state.
     */
    public void resumeGame() {
        isPaused = false;
    }

    /**
     * This method allows you to set the value of the gameOver flag in the game
     * engine that controls whether or not a game over message is displayed.
     * @param gameOver true shows the message / false does not show message
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
    
    
    /**
     * This method is used to set the message to display as the game over message.
     * @param gameOverMessage the string containing the game over message
     */
    public void setGameOverMessage(String gameOverMessage) {
        this.gameOverMessage = gameOverMessage;
    }

    /**
     * This method returns the value of the gameOver flag of the engine. The gameOver
     * flag is set by the setGameOver(boolean) method. When the gameOver is set the
     * game will display the message, if the flag is not set then no message is
     * displayed.
     * @see #setGameOver(boolean) 
     * @return true or false 
     */
    public boolean isGameOver() {
        return gameOver;
    }
    
    /**
     * The purpose of this method is to allow customization of the constructor
     * for the Gamepanel class. This is where you will put code that you would 
 like to execute as your EurekaPanel derived class object is being constructed. 
     * @see #GamePanel(long) 
     * @see GamePanel.GamePanel
     */
    public abstract void customizeInit();
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
    
    /**
     * The purpose of this method is to allow customization of the frame rendering
     * phase of the game loop. This method is overridable and is called within
     * the game loop (see the run()) method.
     * @see #run() 
     */
    public abstract void customizeGameRender();
    //{
//            throw new UnsupportedOperationException("customizeGameRender(): Not yet implemented");
//    }
    
    /**
     * The purpose of this method is to allow customization of the frame update 
     * phase of the game loop. This method is overridable and is called within 
     * the game loop (see the run()) method. 
     * @see #run() 
     */
    public abstract void customizeGameUpdate(); 
    //{
//            throw new UnsupportedOperationException("customizeGameUpdate(): Not yet implemented");
//    }
    
    /**
     * The purpose of this method is to allow customization of your games' 
     * handling of mouse events. This method is overridable and is registered 
     * with the MouseListener.
     * @param x is the value of the x-coordinate of the mouse click
     * @param y is the value of the y-coordinate of the mouse click
     */            
    public abstract void customizeMousePress(int x, int y);
    //{
//        throw new UnsupportedOperationException("customizeTestPress(): Not yet implemented");
//    }
    
    
    /**
     * The purpose of this method is to provide a way of performing operations
     * immediately before the first iteration of the game loop. This method 
     * is overridable and is called within the game loop (see the run()) method.
     * @see #run()
     */
    protected void preGameLoop()
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * The purpose of this method is to provide a way of performing operations
     * immediately after the game loop has terminated. This method 
     * is overridable and is called within the gameloop (see the run()) method.
     * @see #run()
     */
    protected void postGameLoop() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * The purpose of this method is to provide a way of performing additional 
     * housekeeping at the end of each iteration of the game loop. This method 
     * is overridable and is called within the gameloop (see the run()) method. 
     * @see #run() 
     */
    protected void insideGameLoop() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    /**
     * is (x,y) important to the game?
     * @param x the x-coordinate of the mouse
     * @param y the y-coordinate of the mouse
     * @see #GamePanel(long) 
     */
     private void mousePress(int x, int y) {
         if (!isPaused && !gameOver) {
             // do something with x and y
             try {
                 customizeMousePress(x, y);
                 
             }
             catch (UnsupportedOperationException e){}
         }
     } // end of testPresss()
    
    
    /**
     * This method is used wait for the JPanel to be added to the JFrame/JApplet before starting.
     */
    @Override
    public void addNotify() {
        System.out.println("add Notify executed");
        super.addNotify(); // creates the peer
        startGame();        
    } // end of addNotify()
    
    /**
     * This method is used to invoke the game by creating a thread for the game loop
     * to execute on. 
     */
    public void startGame() {
        if (animator == null || !running) {
            animator = new Thread(this);
            animator.start();
        }
    }  // end of startGame()
    
    
    
    /**
     * This method is used to terminate the game loop after the current iteration is 
     * completed. 
     */
    public void stopGame() {
        running = false;
    } // end of stopGame()
    
    
    /**
     * This is the game loop that is central to the operation of the game engine. It 
     * Repeatedly updates, renders, etc. by calling the following methods in this order:<p>
     *    1. private gameUpdate(): This updates the game state and calls the public method 
     * customizeGameUpdate() that can be overridden by a derived class.<br>
     *    2. private gameRender(): This draws the next frame onto the game canvas. It 
     * calls the public method customizeGameRender() that can be overridden by a derived class<br>
     *    3. private paintScreen(): This draws the next frame onto the screen. <br>
     *    4. private storeStats(): Gathers and stores statistics for Frames Per Second (FPS)
     * and Updates Per Second (UPS). These stats can be accessed via a collection of getters.<br>
     *    5. protected insideGame(): An overridable utility method that can optionally be used to 
     * perform additional housekeeping.<br>
     * <p>
     * Outside of the game loop there are other methods called that can be overridden in a 
     * derived class:<p>
     *    1. protected preGameLoop(): This method is called immediately before the game loop
     * starts.<br>
     *    2. protected postGameLoop(): This method is called immediately following the termination
     * of the game loop.<br>
     *  <p>
     * In addition, the game loop regulates itself to maintain FPS goal. 
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
           
        
        
        gameStartTime = java.lang.System.nanoTime(); 
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
            
            afterTime = java.lang.System.nanoTime(); 
            timeDiff = afterTime - beforeTime;
            sleepTime = (period - timeDiff) - overSleepTime;
            
            if (sleepTime > 0){
                try {
                    Thread.sleep(sleepTime/1000000L); // nano -> ms
                }
                catch(InterruptedException ex) {
                    overSleepTime = (java.lang.System.nanoTime() -
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
            
            beforeTime = java.lang.System.nanoTime(); 
            
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
        

        
        if (theCanvasBufferedImage == null) {
            theCanvasBufferedImage = (java.awt.image.BufferedImage) createImage(panelWidth, panelHeight);
            
            if (theCanvasBufferedImage == null) {
                System.out.println("Error: dbImage is null");
                System.exit(1);
//                return;
            }
        } else {
            theCanvas = (Graphics2D)theCanvasBufferedImage.getGraphics();
            theCanvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            theCanvas.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
        }
        
        // draw game elements
        if (gameOver) {
            gameOverMessage(theCanvas);
        }
        
        try {
            // calls the customization method for rendering
           if (theCanvas != null) {
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

            int x = (panelWidth - (gameOverMessage.length() / 2 * sizePerChar)) / 2;
            int y = panelHeight / 2;
            g.setColor(java.awt.Color.BLACK);
            g.drawString(gameOverMessage, x, y);
        }
        
    } // end of gameOverMessage
    
    @Override
    public void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        
        if (theCanvasBufferedImage != null)
            g.drawImage(theCanvasBufferedImage, 0, 0, null);
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setPreferredSize(new java.awt.Dimension(panelWidth, panelHeight));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 10, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 10, Short.MAX_VALUE)
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
            if ((g != null) && (theCanvasBufferedImage != null))
                g.drawImage(theCanvasBufferedImage, 0, 0, null);
            
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
            
            long timeNow = java.lang.System.nanoTime(); 
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

    public static int getPanelWidth() {
        return panelWidth;
    }

    public static int getPanelHeight() {
        return panelHeight;
    }

    public static void setScreenDimensions(int width, int height) {
        if (width > 0)
            panelWidth = width;
        
        if (width > 0)
            panelHeight = height;
    }
    
   
}  // end of paintScreen()
