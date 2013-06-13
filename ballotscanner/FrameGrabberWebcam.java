package ballotscanner;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

/**
 * User: Aaron
 * Date: 11/12/12
 * Time: 4:40 PM
 *
 * Concrete class for a webcam using OPENCV framegrabber
 */
public class FrameGrabberWebcam implements IWebcam {

  // Where the frameGrabber is stored
  private FrameGrabber frameGrabber;

  /**
   * Constructor for a FrameGrabberWebcam.  Initializes the frameGrabber.
   */
  FrameGrabberWebcam() {
    frameGrabber = new OpenCVFrameGrabber("");
  }

  /**
   * method that gets the current bitmap from the webcam
   * @return the webcam's output as a BinaryBitmap.  If the webcam is inactive, return null
   */
  public BinaryBitmap getBitmap() {
    try {
      // grab image, and convert it to a BinaryBitmap as a BufferedImageLuminanceSource
      IplImage img = frameGrabber.grab();
      if(img!=null) {

        LuminanceSource source = new BufferedImageLuminanceSource(img.getBufferedImage());
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        return bitmap;
      }
      else
        return null;
    }
    catch (Exception e) {
      System.out.println("Exception Occurred: "+e.getMessage());
      return null;
    }
  }

  /**
   * Tells the webcam to start capturing
   */
  public void startCapture() {
    try {
      frameGrabber.start();
    }
    catch (Exception e) {
      System.out.println("Exception Occurred: "+e.getMessage());
    }
  }
}
