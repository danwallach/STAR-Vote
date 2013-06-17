package printer;


import java.awt.*;
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

import javax.swing.*;


/**
 * Slightly modified version of code found at http://stackoverflow.com/questions/2489048/qr-code-encoding-and-decoding-using-zxing
 * utilizing the ZXing QR code library
 */
public class QRCodeGenerator {

    private byte[] b;
    public static BitMatrix getCode(String toEncode){

        Charset charset = Charset.forName("UTF-8");
        CharsetEncoder encoder = charset.newEncoder();
        byte[] b = null;
        try {
            // Convert a string to UTF-8 bytes in a ByteBuffer
            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(toEncode));
            b = bbuf.array();
        } catch (CharacterCodingException e) {
            System.out.println(e.getMessage());
        }

        String data;
        BitMatrix matrix = null;
        int h = 150;
        int w = 150;
        try {
            data = new String(b, "UTF-8");
            // get a byte matrix for the data

            com.google.zxing.Writer writer = new MultiFormatWriter();
            try {
                Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>(2);
                hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                matrix = writer.encode(data,
                        com.google.zxing.BarcodeFormat.QR_CODE, w, h, hints);
            } catch (com.google.zxing.WriterException e) {
                System.out.println(e.getMessage());
            }

            // change this path to match yours (this is my mac home folder, you can use: c:\\qr_png.png if you are on windows)
            //String filePath = "C:/Users/Matt/workspace/starvote/QROut";
            //File file = new File(filePath);

        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }

        return matrix;
    }

    public static BufferedImage getImage(String toEncode){
        BitMatrix matrix = getCode(toEncode);
        return MatrixToImageWriter.toBufferedImage(matrix);


    }

}