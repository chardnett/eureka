/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package javagamechapter6;

import GamePanel.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

/**
 *
 * @author hardnett
 */
public class ImagesTests extends GamePanel {
    private ImagesLoader imsLoader;
    private int counter;
    private boolean justStarted;
    //private ImageSFXs imageSfx;
    private GraphicsDevice gd;
    private int accelMemory;
    private DecimalFormat df;
    
    private BufferedImage atomic, balls, pumpkin;
    /**
     * Constructor to set up the image testing objects
     */
    public ImagesTests() {
        df = new DecimalFormat("0.0");  // sets to 1 decimal place
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        gd  = ge.getDefaultScreenDevice();
        
        accelMemory = gd.getAvailableAcceleratedMemory(); // returns # of bytes
        System.out.println("Initial Accelerated Memory: " + 
                df.format (((double)accelMemory)/(1024*1024)) + " MB");
        
        this.setBackground(Color.WHITE);
        this.setPreferredSize(new Dimension(PWIDTH, PHEIGHT));
        
        imsLoader = new ImagesLoader();
        imsLoader.loadSingleImage("atomic.gif");
        imsLoader.loadSingleImage("balls.jpg");
        imsLoader.loadSingleImage("pumpkin.png");
    }
    
    
    @Override
    public void preGameLoop() {
          // initialize the single images
        atomic = imsLoader.getImage("atomic");
        balls = imsLoader.getImage("balls");
        pumpkin = imsLoader.getImage("pumpkin"); 
    }
    
    @Override
    public void customizeGameRender() {
        dbg.setColor(Color.blue);
        dbg.fillRect(0, 0, PWIDTH, PHEIGHT);
        dbg.drawImage(atomic, 10, 25, this);
    }
    
    @Override
    public void customizeGameUpdate() {
        
    }
}
