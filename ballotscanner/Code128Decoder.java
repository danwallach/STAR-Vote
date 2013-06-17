package ballotscanner;

import com.google.zxing.*;
import com.google.zxing.oned.Code128Reader;

import java.util.Hashtable;

/**
 * @author Matt Bernhard
 * 6/17/13
 *
 * This class provides a wrapper for the zxing Code128Reader
 */
public class Code128Decoder {
    // reader for decoding
    private Code128Reader reader;

    private Hashtable<DecodeHintType, Object> hints;

    /**
     * Constructor for the decoder
     */
    public Code128Decoder() {
        reader = new Code128Reader();
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
            e.printStackTrace();
            return null;
        } catch (FormatException e) {
            e.printStackTrace();
            return null;
        }
    }
}
