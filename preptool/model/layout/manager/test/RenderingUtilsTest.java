package preptool.model.layout.manager.test;

import preptool.model.layout.manager.RenderingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A test suite to verify the functionality of miscellaneous things in the RenderingUtils package
 */
public class RenderingUtilsTest /*extends TestCase*/ {



    /*
    public void setUp() throws Exception {
        super.setUp();
        testFrame = new JFrame("Rendering Utils Test");
    }
    */

    public static void main(String[] args) /*testDrawBox()*/{
        testDrawBox();

    }

    public static void testDrawBox(){
        JFrame testFrame =  new JFrame("Rendering Utils Test");

        JPanel testPanel = new JPanel();

        BufferedImage wrappedImage = new BufferedImage(1000, 1000,
                BufferedImage.TYPE_INT_ARGB);


        int DPI_SCALE_FACTOR = Math.round(1.0f*300/72);

        int width = 150*DPI_SCALE_FACTOR;
        int height = 100*DPI_SCALE_FACTOR;


        Graphics2D g = wrappedImage.createGraphics();

        RenderingUtils.drawBox(g, 500 - width / 2, 500 - height, width / 2, height / 2, true, 10 * DPI_SCALE_FACTOR / 8);

        JLabel testLabel = new JLabel(new ImageIcon(wrappedImage));

        testPanel.add(testLabel);
        testFrame.add(testPanel);

        testFrame.pack();

        testFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        testFrame.setVisible(true);
    }



}
