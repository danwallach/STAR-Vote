package printer;


import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Hashtable;

import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.*;


/**
 * Slightly modified version of code found at http://stackoverflow.com/questions/2489048/qr-code-encoding-and-decoding-using-zxing
 * utilizing the ZXing QR code library
 */
public class QRCodeGenerator {

    private byte[] b;

    /**
     * Encodes the given String as barcode data
     *
     * @param toEncode      the String to encode as a barcode
     * @return              the encoded String as a BitMatrix
     */
    public static BitMatrix getCode(String toEncode) {

        /* Set the encoding */
        Charset charset = Charset.forName("UTF-8");
        CharsetEncoder encoder = charset.newEncoder();

        /* Create a byte array*/
        byte[] b = null;

        /* Try to use a ByteBuffer to convert the String to UTF-8 bytes */
        try {
            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(toEncode));
            b = bbuf.array();
        }
        catch (CharacterCodingException e) { e.printStackTrace(); }

        String data;
        BitMatrix matrix = null;

        /* Set up the preferred height and width of the barcode */
        int h = 150;
        int w = 150;

        /* Try to create a new String of the data encoded in UTF-8*/
        try {

            data = new String(b, "UTF-8");

            com.google.zxing.Writer writer = new MultiFormatWriter();

            /* Set up the encoder with the character set */
            try {
                Hashtable<EncodeHintType, String> hints = new Hashtable<>(2);
                hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

                /* Write the matrix using the encoder*/
                matrix = writer.encode(data, com.google.zxing.BarcodeFormat.QR_CODE, w, h, hints);
            }
            catch (com.google.zxing.WriterException e) { e.printStackTrace(); }

        }
        catch (UnsupportedEncodingException e) { e.printStackTrace(); }

        return matrix;
    }

    /**
     * Converts a String to a barcode image
     *
     * @param toEncode      the String to encode as a barcode
     * @return              the encoded String as a barcode image
     */
    public static BufferedImage getImage(String toEncode) {
        BitMatrix matrix = getCode(toEncode);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

}