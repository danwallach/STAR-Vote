package crypto.interop;

import java.io.File;
import java.io.FileOutputStream;

import edu.uconn.cse.adder.PrivateKey;
import edu.uconn.cse.adder.PublicKey;

/**
 * Using this class, specify a destination directory, you can generate a single public/private key-pair
 * to be used by Adder when the NIZK switch is enabled.
 * @author Montrose
 *
 */
public class AdderKeyGenerator {

	/**
	 * @param args   args.length should == 1 and args[0] should be a path to the destination directory
	 */

	public static void main(String[] args) throws Exception{
		if(args.length != 1){
			System.out.println("Usage: java "+AdderKeyGenerator.class.getName()+" [destination directory]");
			System.exit(-1);
		}

        /*File pointer for the destination directory as in args[0]*/
		File destDir = new File(args[0]);


         /* Checks whether the destination directory already exists, if not then make the directory.*/
		if(!destDir.exists()){
			destDir.mkdirs();
		}

        /*If it exits then it checks whether its a directory or not.*/
        else{

			if(!destDir.isDirectory()){
				System.out.println("Usage: java "+AdderKeyGenerator.class.getName()+" [destination directory]");
				System.exit(-1);
			}
		}
		
		
		System.out.println("Generating keys");
		PublicKey pubKey = PublicKey.makePartialKey(512);
		PrivateKey privKey = pubKey.genKeyPair();
		
		File pubFile = new File(destDir, "public.adder.key");
		File privFile = new File(destDir, "private.adder.key");

         /* Prints out the exact location of the files where the public key and the private key are stored on the console */
		System.out.println(pubFile.getAbsolutePath());
		System.out.println(privFile.getAbsolutePath());

        /* create public.adder.key and private.adder.key file in the same location path destDir */
		pubFile.createNewFile();
		privFile.createNewFile();
		
		FileOutputStream pubOut = new FileOutputStream(pubFile);
		FileOutputStream privOut = new FileOutputStream(privFile);

        /* Writes the public key to the 'public.adder.key' file. */
		pubOut.write(pubKey.toASE().toVerbatim());

        /* Writes the private key to the 'private.adder.key' file. */
		privOut.write(privKey.toASE().toVerbatim());

        // clear out the file pointer buffers
		pubOut.flush();
		privOut.flush();

        // close the file
		pubOut.close();
		privOut.close();
		
		System.exit(0);
	}

}
