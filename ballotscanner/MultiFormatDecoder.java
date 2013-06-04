package ballotscanner;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;

/**
 * User: Aaron
 * Date: 11/13/12
 * Time: 1:10 AM
 *
 * Concrete class for a decoder, which uses the MultiFormatReader zxing library
 */
public class MultiFormatDecoder implements IDecoder {

  // reader for decoding
  private MultiFormatReader reader;

  /**
   * Constructor for the decoder
   */
  public MultiFormatDecoder() {
    reader = new MultiFormatReader();
  }

  /**
   * Method that decodes a bitmap
   * @param bitmap input bitmap to decode
   * @return String from decoded bitmap.  returns null if nothing is found.
   */
  public String decode(BinaryBitmap bitmap) {
    try {
      Result result = reader.decode(bitmap);
      return result.getText();
    }
    catch(NotFoundException e) {
      return null;
    }
  }
}
