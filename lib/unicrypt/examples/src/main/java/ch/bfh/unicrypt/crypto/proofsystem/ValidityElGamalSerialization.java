package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.crypto.proofsystem;

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.keygenerator.classes.DiscreteLogarithmKeyGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.classes.ElGamalEncryptionValidityProofSystem;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.Alphabet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.concatenative.classes.StringElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Subset;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.classes.PseudoRandomOracle;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.interfaces.RandomOracle;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;

public class ValidityElGamalSerialization {

	public static void main(String[] args) {

		GStarModSafePrime gQ = GStarModSafePrime.getInstance(new BigInteger("1187"));
		ZMod zQ = gQ.getZModOrder();

		System.out.println("P: " + gQ.getModulus());
		System.out.println("Q: " + zQ.getModulus());

		StringElement proverId = StringMonoid.getInstance(Alphabet.BASE64).getElement("Prover1");

		DiscreteLogarithmKeyGenerator egkpg = DiscreteLogarithmKeyGenerator.getInstance(gQ);

		RandomOracle r1 = PseudoRandomOracle.getInstance();

		RandomOracle r2 = PseudoRandomOracle.getInstance();

		ZModElement privateKey = egkpg.generatePrivateKey();
		GStarModElement publicKey = (GStarModElement) egkpg.generatePublicKey(privateKey);

		System.out.println("PublicKey: " + publicKey);

		GStarModElement[] possibleMessages = new GStarModElement[4];
		ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(gQ);

		possibleMessages[0] = gQ.getDefaultGenerator().power(0);
		possibleMessages[1] = gQ.getDefaultGenerator().power(1);
		possibleMessages[2] = gQ.getDefaultGenerator().power(2);
		possibleMessages[3] = gQ.getDefaultGenerator().power(3);

		SigmaChallengeGenerator scg = ElGamalEncryptionValidityProofSystem.createNonInteractiveChallengeGenerator(elGamal, possibleMessages.length, proverId, r1);

		Subset plaintexts = Subset.getInstance(gQ, possibleMessages);

		ElGamalEncryptionValidityProofSystem pg = ElGamalEncryptionValidityProofSystem
			   .getInstance(scg, elGamal, publicKey,
							plaintexts);

		ZModElement randomization = zQ.getRandomElement();

		int index = 1;

		Tuple privateInput = pg.createPrivateInput(randomization, index);

		System.out.println("Cleartext: " + possibleMessages[1]);

		Tuple cipherText = elGamal.encrypt(publicKey, possibleMessages[index],
										   randomization);

		Tuple proof = pg.generate(privateInput, cipherText);

		try {
			OutputStream file = new FileOutputStream("proofs.ser");
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);

			output.writeObject(Tuple.getInstance(proof, cipherText, publicKey));
			output.close();
		} catch (IOException ex) {
			System.out.println("Problem");
		}

		SigmaChallengeGenerator scg2 = ElGamalEncryptionValidityProofSystem.createNonInteractiveChallengeGenerator(elGamal, possibleMessages.length, proverId);
		ElGamalEncryptionValidityProofSystem pg2 = ElGamalEncryptionValidityProofSystem
			   .getInstance(scg2, elGamal, publicKey,
							plaintexts);

		boolean v = pg2.verify(proof, cipherText);

		System.out.println("Proof valid: " + v);

		System.out.println("proof: " + proof);
		System.out.println("cipherText: " + cipherText);
		System.out.println("publickey: " + publicKey);
		System.out.println("proverId: " + proverId);

		GStarModElement recoveredCleartext = (GStarModElement) elGamal.decrypt(privateKey,
																			   cipherText);

		System.out.println("Recovered cleartext: " + recoveredCleartext);

	}

}
