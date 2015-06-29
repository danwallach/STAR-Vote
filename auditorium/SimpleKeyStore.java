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

package auditorium;

import crypto.adder.AdderPrivateKeyShare;
import crypto.adder.AdderPublicKey;
import crypto.adder.AdderPublicKeyShare;
import sexpression.ASEParser;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.stream.ASEInputStreamReader;
import sexpression.stream.InvalidVerbatimStreamException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Simple keystore implementation which looks for keys and certificates in a
 * specified directory. Keys are represented by files named
 * <tt><i>nodeID</i>.key</tt> and containing an S-expression matching the
 * format expected by {@link Key}; similarly, certificate files are named
 * <tt><i>nodeID</i>.cert</tt> and contain an S-expression recognized by the
 * constructor for {@link Certificate}.
 *
 * Keys and Certs are cached internally, so multiple requests for the same key
 * will not result in multiple loads from disk.
 * 
 * @see auditorium.Key
 * @see Certificate
 * @author Kyle Derr
 * 
 */
public class SimpleKeyStore implements IKeyStore {
	/** Name of file containing keys/classes/etc for votebox (generally, a jar file) */
	private static final String ROOT_JARS[] = {"Votebox.jar", "Supervisor.jar", "Scanner.jar"};

    /** The path of directory containing the keys */
	private final String dir;

    /** A mapping of key references, for easy access */
	private HashMap<String,Key> keyCache;

    /** A mapping of certificate references, for easy access*/
	private HashMap<String, Certificate> certCache;

	/**
     * Constructor.
     *
	 * @param dir The directory where the key and cert files are stored.
	 */
	public SimpleKeyStore(String dir) {
		this.dir = dir;

        /* Initialize the maps */
		keyCache = new HashMap<>();

		certCache = new HashMap<>();
	}

    public AdderPublicKey loadPEK() throws AuditoriumCryptoException{

        File PEKFile = new File("PEK.adder.key");
        Path PEKPath = PEKFile.toPath();
        System.out.println(PEKPath.toAbsolutePath());


        try {
            byte[] verbatimPEK = Files.readAllBytes(PEKPath);
            ASExpression PEKASE = ASExpression.makeVerbatim(verbatimPEK);
            System.out.println(PEKASE);
            return ASEParser.convertFromASE((ListExpression)PEKASE);
        }
        catch (Exception e) { e.printStackTrace(); throw new RuntimeException("Couldn't use the key file");}


/* todo see if this matters
        ASExpression asePEK = null;

        try {

            asePEK = load("PEK.key");

            return ASEParser.convertFromASE((ListExpression) asePEK);
        }
        catch (AuditoriumCryptoException e){ throw new AuditoriumCryptoException("Error during loadPEK(): ", e); }
        */
    }

	/**
	 * Load the key from a file in the "keys" directory.
	 * 
	 * @see auditorium.IKeyStore#loadKey(java.lang.String)
	 */
	public Key loadKey(String nodeID) throws AuditoriumCryptoException {
		if (!keyCache.containsKey(nodeID)) {
			try {
				keyCache.put(nodeID, new Key(load(nodeID + ".key")));
			}
			catch (Exception e) {
				throw new AuditoriumCryptoException("loadKey(\"" + nodeID + "\")", e);
			}
		}

		return keyCache.get(nodeID);
	}

	/**
	 * Load the published certificate from a file in the "keys" directory.
	 * 
	 * @see auditorium.IKeyStore#loadCert(java.lang.String)
	 */
	public Certificate loadCert(String nodeID) throws AuditoriumCryptoException {
		if (!certCache.containsKey(nodeID)) {
			try {
				certCache.put(nodeID, new Certificate(load(nodeID + ".cert")));
			}
			catch (Exception e) {
				throw new AuditoriumCryptoException("loadCert(\"" + nodeID + "\")", e);
			}
		}

		return certCache.get(nodeID);
	}

    /**
     * Load a key used by the Adder crypto package, which has properties necessary for homomorphic ElGamal as well as
     * generating NIZKs
     *
     * @return the adder key
     */
    public AdderPublicKeyShare loadAdderPublicKeyShare() {
        try{
            InputStream in = getInput("public.adder.key");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            int i;
            while((i = in.read()) != -1){
                byteArrayOutputStream.write(i);
            }

            return ASEParser.convertFromASE((ListExpression) ASExpression.makeVerbatim(byteArrayOutputStream.toByteArray()));

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Load a key used by the Adder crypto package, which has properties necessary for homomorphic ElGamal as well as
     * generating NIZKs
     *
     * @return the adder key
     */
    public AdderPrivateKeyShare loadAdderPrivateKey() {
        try{
            InputStream in = getInput("private.adder.key");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            int i;
            while((i = in.read()) != -1){
                byteArrayOutputStream.write(i);
            }

            return ASEParser.convertFromASE((ListExpression) ASExpression.makeVerbatim(byteArrayOutputStream.toByteArray()));

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

	/**
	 * Loads a file for use in the KeyStore.<BR>
	 * First searches the ROOT_JAR, if it exists.<BR>
	 * Then looks for the path in the filesystem.<BR>
	 * The path itself is derived from dir (dir + "/" + file, approximately).
	 * 
	 * @param file The file to load
	 * @return An ASExpression representing the file resource
	 * @throws AuditoriumCryptoException - Should any exception occur during processing.
	 */
	private ASExpression load(String file) throws AuditoriumCryptoException {
		try {
			InputStream stream = getInput(file);

			return new ASEInputStreamReader(stream).read();
		} catch (IOException | InvalidVerbatimStreamException e1) {
			throw new AuditoriumCryptoException("load(\""+file+"\")", e1);
		}
    }
	
	/**
	 * Opens a stream for reading a key.
	 * 
	 * @param file the name of the file that contains the key.  This does not include the path to the key
	 * @return an open InputStream to read from.
	 * @throws AuditoriumCryptoException - If the key cannot be found, or an error occurs
	 */
	private InputStream getInput(String file) throws AuditoriumCryptoException{
        //TODO MAKE THIS WORK. RAWR.
//		InputStream stream = getClass().getResourceAsStream(dir+"/"+file); // "/" is the class loaders path component, not a hardcoded unix style path component separator
//		// leave it alone
//
//		if(stream != null)
//			return stream;
//
//		//This is very inelegant, but only comes into play in development scenarios
//		//Worth removing for a proper "deployment"

		//Check each jar that might have a keystore

		boolean[] jarsExist = new boolean[ROOT_JARS.length];

		String entry = dir;
		if(!entry.endsWith("/"))
			entry += "/" + file;
		else
			entry += file;

        for(int i = 0; i < ROOT_JARS.length; i++){
            File jarFile = new File(ROOT_JARS[i]);

			if(jarFile.exists())
				jarsExist[i] = true;

			try{
				InputStream in = null;

				if(jarFile.exists()){
					JarFile vbJar = new JarFile(jarFile);

					JarEntry jEntry;

					if(entry.startsWith("/"))
						jEntry = vbJar.getJarEntry(entry.substring(1));
					else
						jEntry = vbJar.getJarEntry(entry);

					in = vbJar.getInputStream(jEntry);
				}//if

				/*ASExpression exp = new ASEInputStreamReader(in).read();

				in.close();

				return exp;*/
				
				if(in != null)
					return in;
			}catch(Exception ignored){
            }//catch
		}//for

		//If that fails, check the working directory
		try{
			File rootFile = new File(entry.replace('/', File.separatorChar));

			if(!rootFile.exists() && entry.startsWith("/"))
				rootFile = new File(entry.substring(1).replace('/', File.separatorChar));

			return new FileInputStream(rootFile);

			/*ASExpression exp = new ASEInputStreamReader(in).read();

			in.close();

			return exp;*/
			
		}catch(Exception e){
			String msg = "load(); path = \""+entry+"\"";

			for(int i = 0; i < ROOT_JARS.length; i++)
				if(jarsExist[i])
					msg+=" with \""+ROOT_JARS[i]+"\" found";

			throw new AuditoriumCryptoException( msg, e );
		}//catch
		
//		throw new AuditoriumCryptoException("No key found for \""+file+"\"", null);
	}
}
