package ballotscanner;

import com.google.zxing.BinaryBitmap;

/**
 * User: Aaron
 * Date: 11/13/12
 * Time: 1:00 AM
 *
 * Interface for Decoders for codes
 */
public interface IDecoder {

  /**
   * method used to decode from a bitmap
   * @param bitmap input bitmap to decode
   * @return the string decoded
   */
  public String decode(BinaryBitmap bitmap);
}
