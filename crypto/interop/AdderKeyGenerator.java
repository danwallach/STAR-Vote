package crypto.interop;

import crypto.adder.AdderPrivateKeyShare;
import crypto.adder.AdderPublicKeyShare;
import sexpression.ASEConverter;
import sexpression.ASExpression;

import java.io.File;
import java.io.FileOutputStream;

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

        /*If it exists then it checks whether its a directory or not.*/
        else{

			if(!destDir.isDirectory()){
				System.out.println("Usage: java "+AdderKeyGenerator.class.getName()+" [destination directory]");
				System.exit(-1);
			}
		}
		
		
		System.out.println("Generating keys");
		AdderPublicKeyShare pubKeyShare = AdderPublicKeyShare.makePublicKeyShare(512);
		AdderPrivateKeyShare privKey = pubKeyShare.genKeyPair();
		
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

        ASExpression pubASE = ASEConverter.convertToASE(pubKeyShare);
        System.out.println("Public key: " + pubASE);

        /* Writes the public key to the 'public.adder.key' file. */
		pubOut.write(pubASE.toVerbatim());

        ASExpression privASE = ASEConverter.convertToASE(privKey);
        System.out.println("Public key: " + privASE);

        /* Writes the private key to the 'private.adder.key' file. */
		privOut.write(privASE.toVerbatim());

        /* clear out the file pointer buffers */
		pubOut.flush();
		privOut.flush();

        /* close the file */
		pubOut.close();
		privOut.close();
		
		System.exit(0);
	}

}
