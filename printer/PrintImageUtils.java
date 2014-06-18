package printer;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.oned.Code128Writer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.HashMap;

/**
 * A class which provides image manipulation support for printing
 *
 * @author Matt Bernhard, Mircea Berechet
 */
public class PrintImageUtils {

    /**
     * Trims the given image so that there is no trailing white/transparent block.
     *
     * @param image             image to be evaluated for trimming
     *
     * @param trimFromEnd       whether the image should have whitespace at the end removed instead of
     *                          that at the front
     *
     * @param maxToTrim         the maximum amount of whitespace to trim, necessary for uniform scaling
     *                          later on
     *
     * @return                  trimmed image
     *
     * @see tap.BallotImageHelper
     */

    public static BufferedImage trimImageHorizontally(BufferedImage image, boolean trimFromEnd, int maxToTrim) {

        BufferedImage outImage;

        /* Check if we need to trim from the end or the front */
        if (trimFromEnd) {

            /* Flip the image*/
            outImage = flipImageHorizontally(image);

            /* Trim the image */
            outImage = trimImageHorizontallyHelper(outImage, maxToTrim);

            /* Flip it back */
            outImage = flipImageHorizontally(outImage);

        } /* Otherwise, just trim it normally */
        else outImage = trimImageHorizontallyHelper(image, maxToTrim);

        return outImage;
    }

    /**
     * Trims the given image so that there is no trailing white/transparent block.
     *
     * @param image             image to trim
     *
     * @param trimFromBelow     whether the image should have whitespace from below the non-empty space
     *                          removed instead of that from above
     *
     * @param maxToTrim         the maximum amount of whitespace to trim, necessary for uniform scaling
     *                          later on
     *
     * @return                  trimmed image
     *
     * This was taken from BallotImageHelper
     */

    public static BufferedImage trimImageVertically(BufferedImage image, boolean trimFromBelow, int maxToTrim) {

        BufferedImage outImage;

        /* Check if we need to trim from above or from below */
        if (trimFromBelow) {

            /* Flip the image vertically */
            outImage = flipImageVertically(image);

            /* Trim the whitespace */
            outImage = trimImageVerticallyHelper(outImage, maxToTrim);

            /* Flip it back vertically */
            outImage = flipImageVertically(outImage);

        } /* Otherwise, just trim it normally */
        else outImage = trimImageVerticallyHelper(image, maxToTrim);

        return outImage;
    }

    /**
     * A method which will invert an image with respect to its y-axis
     *
     * @param image     image to be flipped
     * @return          a flipped image
     */
    public static BufferedImage flipImageHorizontally(BufferedImage image) {

        /* Create a new clean image of the same size/type */
        BufferedImage flipped = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        /* Instantiate Affine transformation for flipping and translating */
        AffineTransform tran = AffineTransform.getTranslateInstance(image.getWidth(), 0);
        AffineTransform flip = AffineTransform.getScaleInstance(-1d, 1d);

        /* Merge these */
        tran.concatenate(flip);

        /* Creates a Graphics2D object linked  */
        Graphics2D g = flipped.createGraphics();

        /* Set the transformation on the graphic */
        g.setTransform(tran);

        /* Draw the image onto the graphic */
        g.drawImage(image, 0, 0, null);

        /* Now dispose of the graphic */
        g.dispose();

        /* Return the flipped image */
        return flipped;
    }

    /**
     * A method which will flip an image with respect to its x-axis
     *
     * @param image     image to be flipped
     * @return          a flipped image
     */
    public static BufferedImage flipImageVertically(BufferedImage image) {


        /* Create a new clean image of the same size/type */
        BufferedImage flipped = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        /* Instantiate Affine transformation for flipping and translating */
        AffineTransform tran = AffineTransform.getTranslateInstance(0, image.getHeight());
        AffineTransform flip = AffineTransform.getScaleInstance(1d, -1d);

        /* First apply flip then apply tran, to enable the flipping transformation to work (we can't just do the flip without the translation) */
        tran.concatenate(flip);

        /* Creates a Graphics2D object linked  */
        Graphics2D g = flipped.createGraphics();

        /* Set the transformation on the graphic */
        g.setTransform(tran);

        /* Draw the image onto the graphic */
        g.drawImage(image, 0, 0, null);

        /* Now dispose of the graphic */
        g.dispose();

        /* Return the flipped image */
        return flipped;

    }

    /**
     * A helper method which actually trims an image. This method trims columns.
     *
     * @param image             image to be trimmed
     * @param maxToTrim         the maximum whitespace that can be trimmed off this image
     * @return                  a trimmed image
     */
    private static BufferedImage trimImageHorizontallyHelper(BufferedImage image, int maxToTrim) {

        try {

            int[] pix = new int[image.getWidth() * image.getHeight()];

            PixelGrabber grab = new PixelGrabber(image, 0, 0, image.getWidth(), image.getHeight(), pix, 0, image.getWidth());

            if(!grab.grabPixels()) return image;

            int lastClearColumn = 0;

            /* TODO fix goto crap */
            out:
            for (int x = 1; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int i = y*image.getWidth() + x;
                    int pixel = pix[i];

                    int alpha = (pixel >> 24) & 0xff;
                    int red   = (pixel >> 16) & 0xff;
                    int green = (pixel >>  8) & 0xff;
                    int blue  = (pixel      ) & 0xff;

                    if(alpha == 0) continue;
                    if(red == 255 && green == 255 && blue == 255) continue;

                    break out;
                }
                lastClearColumn = x;
            }

            int trimmable = Math.min(lastClearColumn, maxToTrim);

            return image.getSubimage(trimmable, 0, image.getWidth() - trimmable, image.getHeight());
        }
        catch (InterruptedException e) { return image; }
    }


    /**
     * A helper method which actually trims an image. This method trims rows.
     *
     * @param image         image to be trimmed
     * @param maxToTrim     the maximum whitespace that can be trimmed off this image
     * @return              a trimmed image
     */
    private static BufferedImage trimImageVerticallyHelper(BufferedImage image, int maxToTrim){

        try {

            int[] pix = new int[image.getWidth() * image.getHeight()];

            PixelGrabber grab = new PixelGrabber(image, 0, 0, image.getWidth(), image.getHeight(), pix, 0, image.getWidth());

            if(!grab.grabPixels()) return image;

            int lastClearRow = 0;

            /* TODO fix goto crap */
            out:
            for(int y = 1; y < image.getHeight(); y++){
                for(int x = 0; x < image.getWidth(); x++){

                    int i = y*image.getWidth() + x;
                    int pixel = pix[i];

                    int alpha = (pixel >> 24) & 0xff;
                    int red   = (pixel >> 16) & 0xff;
                    int green = (pixel >>  8) & 0xff;
                    int blue  = (pixel      ) & 0xff;

                    if(alpha == 0) continue;
                    if(red == 255 && green == 255 && blue == 255) continue;

                    break out;
                }
                lastClearRow = y;
            }

            int trimmable = Math.min(lastClearRow, maxToTrim);

            return image.getSubimage(0, trimmable, image.getWidth(), image.getHeight() - trimmable);
        }
        catch (InterruptedException e) { return image; }
    }

    /**
     * A method for generating a barcode of some text
     *
     * @param string        the string to be converted to barcode, will be a bid in this case
     * @return              an image representing the barcode to be drawn on a ballot
     */
    public static BufferedImage getBarcode(String string){

        /* Try to encode the string as a barcode */
        try {

            Code128Writer writer = new Code128Writer();
            BitMatrix bar = writer.encode(string, BarcodeFormat.CODE_128, 264, 48, new HashMap<EncodeHintType,Object>());

            return MatrixToImageWriter.toBufferedImage(bar);
        }
        catch (WriterException e){ throw new RuntimeException(e); }

    }
}
