package printer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

/**
 * A class which provides image manipulation support for printing
 *
 * @author Matt Bernhard
 */
public class PrintImageUtils {

    /**
     * Trims the given image so that there is no trailing white/transparent block.
     *
     * @param image - Image to trim
     * @return trimmed image
     *
     * This was taken from BallotImageHelper
     */
    //TODO Alter this code so it removes trailing whitespace
    public static Image trimImage(BufferedImage image) {
        try{
            int[] pix = new int[image.getWidth() * image.getHeight()];
            PixelGrabber grab = new PixelGrabber(image, 0, 0, image.getWidth(), image.getHeight(), pix, 0, image.getWidth());
            if(!grab.grabPixels()) return image;

            int lastClearRow = 0;
            out:
            for(int x = image.getWidth()-1; x > 0; x++){
                for(int y = 0; y < image.getHeight(); y++){
                    int i = y*image.getWidth() + x;
                    int pixel = pix[i];

                    int alpha = (pixel >> 24) & 0xff;
                    int red   = (pixel >> 16) & 0xff;
                    int green = (pixel >>  8) & 0xff;
                    int blue  = (pixel      ) & 0xff;

                    if(alpha == 0) continue;
                    if(red == 255 && green == 255 && blue == 255) continue;

                    break out;
                }//for
                lastClearRow = x;
            }//for

            return image.getSubimage(lastClearRow, 0, image.getWidth() - lastClearRow, image.getHeight());
        }catch(InterruptedException e){ return image; }
    }

    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     *
     * Taken from  https://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
     */
    public static BufferedImage getScaledInstance(BufferedImage img,
                                           int targetWidth,
                                           int targetHeight,
                                           Object hint,
                                           boolean higherQuality)
    {
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
                BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage)img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }
}
