/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javagamechapter2;


import GamePanel.GamePanel;
/**
 * This from Kill Game Programming in Java. It only shows how to set up and 
 * execute the basic GamePanel.
 * 
 * @author hardnett
 */
public class JavaGameChapter2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
         java.awt.Frame mainFrame = new java.awt.Frame();
         GamePanel mainScreen = new GamePanel(0L);
         mainFrame.add(mainScreen);
         java.awt.Dimension screenDims = mainScreen.getPreferredSize();
         mainFrame.setSize(800, 600);
         mainFrame.show();

         mainScreen.run();
        
    }
}
