package preptool.model.layout.manager.test;

import preptool.model.layout.manager.RenderingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A test suite to verify the functionality of miscellaneous things in the RenderingUtils package
 */
public class RenderingUtilsTest /*extends TestCase*/ {

    private static JFrame testFrame;
    private static  JPanel testPanel;
    private static JLabel testLabel;

    /*
    public void setUp() throws Exception {
        super.setUp();
        testFrame = new JFrame("Rendering Utils Test");
    }
    */

    public static void main(String[] args) /*testDrawBox()*/{
        testFrame =  new JFrame("Rendering Utils Test");

        testPanel = new JPanel();


        //testDrawBox();
        testRenderPrintButton();

    }

    public static void testDrawBox(){

        BufferedImage wrappedImage = new BufferedImage(1000, 1000,
                BufferedImage.TYPE_INT_ARGB);


        int DPI_SCALE_FACTOR = Math.round(1.0f*300/72);

        int width = 15*DPI_SCALE_FACTOR;
        int height = 10*DPI_SCALE_FACTOR;


        Graphics2D g = wrappedImage.createGraphics();

        RenderingUtils.drawBox(g, 500 - width / 2, 500 - height, width / 2, height / 2, true, 10 * DPI_SCALE_FACTOR / 8);

        testLabel = new JLabel(new ImageIcon(wrappedImage));

        testPanel.add(testLabel);
        testFrame.add(testPanel);

        testFrame.pack();

        testFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        testFrame.setVisible(true);
    }

    public static void testRenderPrintButton(){
        BufferedImage img = RenderingUtils.renderPrintButton("B1", "Matt Bernhard", "", "GLD", 8, 281, false, true);


        testLabel = new JLabel(new ImageIcon(img));
        testPanel.add(testLabel);
        testFrame.add(testPanel);

        testFrame.pack();
        testFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        testFrame.setVisible(true);


    }



}
