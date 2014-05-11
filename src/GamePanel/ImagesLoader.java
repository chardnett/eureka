/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GamePanel;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;

/**
 * This class is based on the concepts presented in Chapter 6 of Killer Game 
 * Programming in Java. This class is used in conjunction with Sprites.
 * @author hardnett
 */
public class ImagesLoader {
    private HashMap imagesMap, gNamesMap;
    private final String IMAGE_DIR = "images/";
    private GraphicsConfiguration graphicsConfig;
    
    
    public ImagesLoader() {
        imagesMap = new HashMap();
        gNamesMap = new HashMap();
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        graphicsConfig = ge.getDefaultScreenDevice().getDefaultConfiguration();
        
    }
    
    /**
     * Loads a single image from a file. This is the external interface for 
     * loading one image from a file. It adds the image to the image list.
     * @param fileName is the name of the file that contains the image
     * @return status of the image load (true = success)
     */
    public boolean loadSingleImage(String fileName) {
        String name = getPrefix(fileName);
        
        if (imagesMap.containsKey(name)) {
            System.out.println("Error: " + name + " already used");
            return false;
        }
        
        BufferedImage bimage = loadImage(fileName);
        if (bimage != null) {
            ArrayList imagesList = new ArrayList();
            imagesList.add(bimage);
            gNamesMap.put(name, imagesList);
            System.out.println(" Stored " + name + "/" + fileName);
            return true;
        } else {
            return false;
        }
    } // end of loadSingleImage()
    
    /**
     * Loads an image from a file. The image file is typically a .jpg, .bmp, 
     * .gif, or .png image file. 
     * @param fileName is the name of the image file to be loaded
     * @return BufferedImage for the loaded image
     */
    private BufferedImage loadImage(String fileName) {
        try {
            BufferedImage image = ImageIO.read(new File(IMAGE_DIR + fileName));
            int transparency = image.getColorModel().getTransparency();
            BufferedImage copy = graphicsConfig.createCompatibleImage(image.getWidth(),
                    image.getHeight(), transparency);
            // create a graphics context
            Graphics2D g2d = copy.createGraphics();
            
            // debug output
            reportTransparency(IMAGE_DIR + fileName, transparency);
            
            // copy image
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            return copy;
            
        }
        catch (IOException e) {
            System.out.println("load Image error for " + IMAGE_DIR +
                    "/" + fileName + ":\n" + e);
            return null;
        }
              
    } // end of loadImage() using ImageIO

    
    /**
     * Returns the prefix of a filename. The prefix is the name of the file up
     * to the extension. The file extension starts with '.' and is usually 
     * 2-3 characters in length.
     * @param fileName
     * @return prefix of the file
     */
    private String getPrefix(String fileName) {
        int startExt = fileName.lastIndexOf('.');
        return fileName.substring(0, startExt);
    } // end of loadImage()

    /**
     * Debug function to output transparency in an image
     * @param string is the filename that is being reported
     * @param transparency  is the transparency value for the image
     */
    private void reportTransparency(String string, int transparency) {
        System.out.println("Transparency Report for " + string + " has transparency = " + transparency);
    } // end of reportTransparency

    public BufferedImage getImage(String imageName) {
        
        if (gNamesMap.containsKey(imageName))
            return (BufferedImage)((ArrayList)gNamesMap.get(imageName)).get(0);
        else
            return null;
    }
}
