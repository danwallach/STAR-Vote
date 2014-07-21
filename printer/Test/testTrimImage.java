package printer.Test;


import javax.swing.*;
import javax.imageio.*;
import java.awt.image.BufferedImage;
import java.io.File;

import printer.PrintImageUtils;
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
