package printer.Test;


import printer.PrintImageUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
/**
 *  This is a test for the PrintImageUtils Class
 *
 *  @author Arghya Chatterjee
 */
public class testTrimImage {

    public static void main(String args[]) throws Exception{

        BufferedImage image = ImageIO.read(new File("TestScribble.png"));

        JOptionPane.showMessageDialog(null,"Hello","Hello", JOptionPane.INFORMATION_MESSAGE,new ImageIcon(image));

        BufferedImage imageTrimHorizontalRight = PrintImageUtils.trimImageHorizontally(image,true,Integer.MAX_VALUE);
        JOptionPane.showMessageDialog(null,"Hello","Hello", JOptionPane.INFORMATION_MESSAGE,new ImageIcon(imageTrimHorizontalRight));


        BufferedImage imageTrimVerticalBottom = PrintImageUtils.trimImageVertically(imageTrimHorizontalRight, true, Integer.MAX_VALUE);
        JOptionPane.showMessageDialog(null, "Hello", "Hello", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(imageTrimVerticalBottom));


        BufferedImage imageTrimHorizontalLeft = PrintImageUtils.trimImageHorizontally(imageTrimVerticalBottom,false,Integer.MAX_VALUE);
        JOptionPane.showMessageDialog(null,"Hello","Hello", JOptionPane.INFORMATION_MESSAGE,new ImageIcon(imageTrimHorizontalLeft));


        BufferedImage imageTrimVerticalTop = PrintImageUtils.trimImageVertically(imageTrimHorizontalLeft, false, Integer.MAX_VALUE);
        JOptionPane.showMessageDialog(null, "Hello", "Hello", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(imageTrimVerticalTop));


    }
}
