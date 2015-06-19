/**
  * This file is part of VoteBox.
  * 
  * VoteBox is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as published by
  * the Free Software Foundation.
  * 
  * You should have received a copy of the GNU General Public License
  * along with VoteBox, found in the root of any distribution or
  * repository containing all or part of VoteBox.
  * 
  * THIS SOFTWARE IS PROVIDED BY WILLIAM MARSH RICE UNIVERSITY, HOUSTON,
  * TX AND IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS, IMPLIED OR
  * STATUTORY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, WARRANTIES OF
  * ACCURACY, COMPLETENESS, AND NONINFRINGEMENT.  THE SOFTWARE USER SHALL
  * INDEMNIFY, DEFEND AND HOLD HARMLESS RICE UNIVERSITY AND ITS FACULTY,
  * STAFF AND STUDENTS FROM ANY AND ALL CLAIMS, ACTIONS, DAMAGES, LOSSES,
  * LIABILITIES, COSTS AND EXPENSES, INCLUDING ATTORNEYS' FEES AND COURT
  * COSTS, DIRECTLY OR INDIRECTLY ARISING OUR OF OR IN CONNECTION WITH
  * ACCESS OR USE OF THE SOFTWARE.
 */

package crypto;

import java.io.*;

import sexpression.ASEParser;
import sexpression.ASExpression;

import auditorium.Key;

/**
 * Used to generate ElGamal public/private key pairs.<BR>
 * Usage:    java crypto.ElGamalKeyGenerator [generator string] [number of keys] [output directory]
 *
 * @author Kevin Montrose
 *
 */
public class ElGamalKeyGenerator {

	/**
	 * @param args      generator string | number of keys | output directory
	 */
	public static void main(String[] args) {

        /* Ensure there is correct number of arguments */
		if (args.length != 3) {
			System.err.println("Usage:");
			System.err.println("\tjava crypto.ElGamalKeyGenerator [generator string] [number of keys] [output directory]");
			System.exit(0);
		}

        /* Set a default limit value */
		int numKeys = -1;

        /* Parse the first argument as the number of keys, expecting an integer */
		try { numKeys = Integer.parseInt(args[1]); }
        catch (Exception e) {
			System.out.println("Expected integer for [number of keys], found \""+args[1]+"\".");
            e.printStackTrace();
			System.exit(0);
		}
		
		File dir = null;

        /* Create directory with output path in args[2] if it already does not exist */
		try {
            dir = new File(args[2]);
			if(!dir.exists()) dir.mkdirs();
		}
        catch (Exception e) {
			System.out.println("Expected path for [output directory], found \""+args[2]+"\".");
			System.out.println("\tError: "+e.getMessage());
			System.exit(0);
		}

        /* Iterate over the number of keys to be generated  */
		for (int i = 0; i < numKeys; i++) {

            /* Generate ElGamal Private and Public key Pair. */
            Pair<Key> keys = ElGamalCrypto.SINGLETON.generate(args[0]);
			Key publicKey = keys.get1();
			Key privateKey = keys.get2();

            /* Here we parse the keys into an s-expression */
			ASExpression pub = ASEParser.convert(publicKey);
			ASExpression priv = ASEParser.convert(privateKey);

            /* The generated pair of public and private keys are stored in the <index>public.key and <index>private.key respectively. */
			File pubFile = new File(dir, i+"public.key");
			File privFile = new File(dir, i+"private.key");

			try {

                /* Create a new output stream for the public key file */
                OutputStream out = new FileOutputStream(pubFile);

                /* Convert the s-expression into Rivest Verbatim format and then write it to the <index>public.key */
				out.write(pub.toVerbatim());
				out.flush();
				out.close();

                /* Create a new output stream for the private key file*/
				out = new FileOutputStream(privFile);

                /* Convert the s-expression into Rivest Verbatim format and then write it to the <index>private.key */
				out.write(priv.toVerbatim());
				out.flush();
				out.close();
			}
            catch (IOException e) {
                System.err.println("Encountered error writing key files.");
                e.printStackTrace();
                System.exit(0);
            }
        }
	}
}
