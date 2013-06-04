package ballotscanner;

import com.google.zxing.BinaryBitmap;

/**
 * User: Aaron
 * Date: 11/12/12
 * Time: 4:31 PM
 *
 * This class is an interface for any kind of webcam interaction
 */
public interface IWebcam {

  /**
   * Input: Nothing
   * Output: Returns the webcam's output as a BinaryBitmap.  If the webcam is inactive, return null
   */
  public BinaryBitmap getBitmap();

  /**
   * Input: Nothing
   * Output: Nothing
   * Tells the webcam to start capturing
   */
  public void startCapture();
}
