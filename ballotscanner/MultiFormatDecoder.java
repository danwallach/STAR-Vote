package ballotscanner;

import com.google.zxing.*;

import java.util.Hashtable;

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

  private Hashtable<DecodeHintType, Object> hints;

  /**
   * Constructor for the decoder
   */
  public MultiFormatDecoder() {
    reader = new MultiFormatReader();
    hints = new Hashtable<DecodeHintType, Object>();
    hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

  }

  /**
   * Method that decodes a bitmap
   * @param bitmap input bitmap to decode
   * @return String from decoded bitmap.  returns null if nothing is found.
   */
  public String decode(BinaryBitmap bitmap) {
    try {
      Result result = reader.decode(bitmap, hints);
      return result.getText();
    }
    catch(NotFoundException e) {
      return null;
    }
  }
}
