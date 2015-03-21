/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Eureka;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 *
 * @author hardnett
 */
public class Sprite {
    
    // default step sizes
    private static final int XSTEP = 1;
    private static final int YSTEP = 1;
    
    // default image size when no image exists
    private static final int SIZE = 12;
    
    
    // image loading support
    protected ImagesLoader imsLoader;
    
    // CRH: not needed
    // panel dimensions
    // protected int pWidth, pHeight;
    
    
    // location data
    protected int locx;
    protected int locy;
    
    // deltas
    protected int dx;
    protected int dy;
    
    // image related attributes
    private String imageName;
    private BufferedImage image;
    private int width, height;
    
    // supports a loop of images  (not supported)
//    private ImagesPlayer player;
//    private boolean isLooping;    
    
    // sprite is updated and drawn only when active
    protected boolean isActive = true;
    
    public Sprite() {
        
    }
    
    
    public Sprite(String name, ImagesLoader imsLoader,int locx, int locy) {
        this.imsLoader = imsLoader;
        this.locx = locx;
        this.locy = locy;
        this.dx = XSTEP;
        this.dy = YSTEP;
        setImage(name);
    }

    public final void setImage(String name) {
       this.imageName = name;
       image = imsLoader.getImage(imageName);
       
       
       if (image == null) { // no image was found
           System.out.println("No sprite image for " + imageName);
           width = SIZE;
           height = SIZE;
       } else {
           width = image.getWidth();
           height = image.getHeight();
       }
       
       // no image loop playing (not supported)
       //player = null;
       //isLooping = false;
    }
    
    
    public void loopImage(int animPeriod, double seqDuration) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
    public void stopLooping() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
    public Rectangle getMyRectangle() {
        return new Rectangle(locx, locy, width, height);
    }
    
    public boolean isActive() {
        return this.isActive;
    }
    
    public void setActive(boolean a) {
        isActive = a;
    }
    
    
    public void updateSprite() {
        if (isActive()) {
            locx += dx;
            locy += dy;

            // not supported (sprite animation)
            // this is for supporting sprite animation when the image contains
            // multiple frames that can be cycled through
/*            if (isLooping)
               player.updateTick();
*/
        }
    }
    
    
    public void drawSprite(Graphics g) {
        if (isActive()) { // only draw if the sprite is active
            if (image == null) {
                g.setColor(Color.YELLOW);
                g.fillOval(locx, locy, SIZE, SIZE);
                g.setColor(Color.BLACK);
            } else {
                /* not supported (sprite animation)
             if (isLooping)
                image = player.getCurrentImage();
            */
                 g.drawImage(image, locx, locy, null);
            }
        }
    }
}
