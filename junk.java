import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.sun.deploy.util.ArrayUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: matt
 * Date: 6/25/13
 * Time: 4:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class junk {

    public static void main(String [] args){

        try{
            Code128Writer writer = new Code128Writer();
            BitMatrix bar = writer.encode("8675309", BarcodeFormat.CODE_128, 264, 48, new HashMap<EncodeHintType,Object>());
            System.out.println("Width: " + bar.getWidth() + " | Height: " + bar.getHeight());


            BitArray arr = bar.getRow(9, null);
            System.out.println("Arr size: " + arr.getSize());
            byte[] a = new byte[arr.getSizeInBytes()];

            arr.toBytes(0, a, 0, arr.getSizeInBytes());
            int[] b = new int[a.length];

            256341215
                    256341215


            System.out.println("B's length: " + b.length);
            for(int i = a.length - 1; i >= 0; i--){


                b[a.length - i - 1] = a[i];

                System.out.println(b[i]);
            }

            System.out.println("\nB's length: " + b.length);

            BitMatrix bar2 = new BitMatrix(264, 48);

            for(int x = 0; x < bar.getHeight(); x++){
                if(b[x] > 0)
                    bar2.setRegion(x, 0, 1, bar.getHeight());
            }

            BufferedImage code = MatrixToImageWriter.toBufferedImage(bar);
            BufferedImage flip = MatrixToImageWriter.toBufferedImage(bar2);
            ImageIO.write(code, "PNG", new File("barcode.png"));
            ImageIO.write(flip, "PNG", new File("flippedBarcode.png"));
        }
        catch (WriterException e){
        } catch (IOException e) {
        }
    }
}
