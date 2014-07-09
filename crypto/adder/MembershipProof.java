package crypto.adder;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;

/**
 *  Zero-knowledge proof of set membership.
 * 
 * Suppose we have a ciphertext \f$\langle G, H \rangle = \langle
 * g^r, h^r f^m \rangle\f$ and we wish to prove that \f$m \in
 * \{i_1, \ldots, i_n\}\f$. Furthermore, suppose that \f$m =
 * i_x\f$. We can use the following OR-composition proof of
 * knowledge.
 * 
 * <table border=0>
 * <tr><th>Prover</th><th></th><th>Verifier</th>
 * <tr><td>\f$c_1, \ldots, c_n \stackrel{\texttt{r}}{\leftarrow}
 * \mathrm{Z}_q\f$</td><td></td><td></td></tr>
 * 
 * <tr><td>\f$s_1, \ldots, s_n \stackrel{\texttt{r}}{\leftarrow}
 * \mathrm{Z}_q\f$</td><td></td><td></td></tr>
 * 
 * <tr><td>if \f$i \neq x\f$, then \f$y_i \leftarrow g^{s_i}G^{-c_i}\f$ and \f$z_i \leftarrow h^{s_i}(H / f^{i_i})^{-c_i}\f$<br> otherwise, \f$y_i = g^t\f$ and \f$z_i
 *  = h^t\f$</td><td></td><td></td></tr>
 * 
 * <tr><td></td><td>\f$\stackrel{y_1, \ldots, y_n, z_1, \ldots,
 * z_n}{\longrightarrow}\f$</td><td></td></tr>
 * 
 * <tr><td></td><td></td><td>\f$c \stackrel{\texttt{r}}{\leftarrow}
 * \mathrm{Z}_q\f$</td></tr>
 * 
 * <tr><td></td><td>\f$\stackrel{c}{\longleftarrow}\f$</td><td></td></tr>
 * 
 * <tr><td>\f$c_x = c - c_1 - \cdots -
 * c_n\f$</td><td></td><td></td></tr>
 * 
 * <tr><td>\f$s_x = t + c_x r\f$</td><td></td><td></td></tr>
 * 
 * <tr><td></td><td>\f$\stackrel{s_1, \ldots, s_n, c_1, \ldots,
 * c_n}{\longrightarrow}\f$</td><td></td></tr>
 * 
 * <tr><td></td><td></td>
 * <td>
 * \f$g^{s_i} \stackrel{?}{=} y_i G^{c_i}\f$ <br>
 * \f$h^{s_i} \stackrel{?}{=} z_i (H/f^{i_i})^{c_i}, i \in
 * \{i_1, \ldots, i_n\}\f$<br>
 * \f$c \stackrel{?}{=} c_1 + \cdots + c_n\f$ </td></tr>
 * 
 * </table>
 * 
 * Now, we can make this proof non-interactive by employing the
 * Fiat-Shamir heuristic.  Then, the prover will send the tuple
 * \f$\langle y_1, z_1, \ldots, y_n, z_n, c, s_1, \ldots, s_n,
 * c_1, \ldots, c_n\rangle\f$, where \f$c = \mathcal{H}(g, h, G,
 * H, y_1, z_1, \ldots, y_n, z_n)\f$ and \f$\mathcal{H}\f$ is a
 * cryptographic hash function. Verification is performed by
 * testing that \f$c_1 + \cdots + c_n = \mathcal{H}(g, h, G, H,
 * g^{s_1}G^{-c_1}, h^{s_1}(H/f^{i_1})^{-c_1}, \ldots,
 * g^{s_n}G^{-c_n}, h^{s_n}(H/f^{i_n})^{-c_n})\f$.
 * 
 *  @author David Walluck
 *  @version $LastChangedRevision$ $LastChangedDate$
 *  @since 0.0.1
 */
public class MembershipProof {

	private AdderInteger p;
	private AdderInteger q;
	private AdderInteger c;
	private List<AdderInteger> yList;
	private List<AdderInteger> zList;
	private List<AdderInteger> sList;
	private List<AdderInteger> cList;

	/**
	 * Constructs a new <code>MembershipProof</code> object with the specified prime.
	 */
	public MembershipProof() {

		yList = new ArrayList<>();
		zList = new ArrayList<>();
		sList = new ArrayList<>();
		cList = new ArrayList<>();
	}

	/**
	 * Constructs a new <code>MembershipProof</code> object with the specified
	 * prime.
	 *
	 * @param p             the prime
	 * @param q             the sub-prime
	 * @param yList         the y list
	 * @param zList         the z list
	 * @param sList         the s list
	 * @param cList         the c list
	 */
	private MembershipProof(AdderInteger p, AdderInteger q, List<AdderInteger> yList, List<AdderInteger> zList,
                            List<AdderInteger> sList, List<AdderInteger> cList) {

		this.p = p;
		this.q = q;
		this.yList = yList;
		this.zList = zList;
		this.sList = sList;
		this.cList = cList;
	}

	/**
	 * Computes the proof as detailed above by forming one proof for each
     * element of the domain. All but one of the proofs (the one corresponding
     * to \em value) will be fake.
     *
	 * @param ciphertext    the ciphertext that the proof is being performed on.
	 * @param pubKey        the public key used to encrypt the message.
	 * @param value         the plaintext value of the ciphertext.
	 * @param domain        the domain of possible values of the plaintext.
	 */
	public void compute(ElgamalCiphertext ciphertext, PublicKey pubKey, AdderInteger value, List domain) {

        /* Get p and q */
		this.p = pubKey.getP();
		this.q = pubKey.getQ();

        /* Get g, h, and f */
		AdderInteger g = new AdderInteger(pubKey.getG(), this.p);
		AdderInteger h = pubKey.getH();
		AdderInteger f = pubKey.getF();

        /* Get bigG, bigH, and r */
		AdderInteger bigG = ciphertext.getG();
		AdderInteger bigH = ciphertext.getH();
		AdderInteger r = ciphertext.getR();

        /* Generate t */
		AdderInteger t = AdderInteger.random(q);

        /* Create a StringBuffer for holding information to create a String */
		StringBuilder sb = new StringBuilder(4096);

        /* Append all the numbers */
		sb.append(g);
		sb.append(h);
		sb.append(bigG);
		sb.append(bigH);

		int indexInDomain = 0;

        /* Iterate over the domain */
		for (int i = 0; i < domain.size(); i++) {

			AdderInteger y;
			AdderInteger z;
			AdderInteger d = (AdderInteger) domain.get(i);

            /* See if the value is this particular member of the domain */
			if (d.equals(value)) {

				sList.add(AdderInteger.ZERO);
				cList.add(AdderInteger.ZERO);
				y = g.pow(t);
				z = h.pow(t);
				indexInDomain = i;
			}
            else {

				sList.add(AdderInteger.random(q));
				cList.add(AdderInteger.random(q));
				AdderInteger s = sList.get(i);
				AdderInteger c = cList.get(i);
				AdderInteger negC = c.negate();
				AdderInteger fpow = f.pow(d);
				y = g.pow(s).multiply(bigG.pow(negC));
				z = h.pow(s).multiply(bigH.divide(fpow).pow(negC));
			}

			yList.add(y);
			zList.add(z);

			sb.append(y);
			sb.append(z);
		}

		String s = sb.toString();
		String cHash = Util.sha1(s);

		this.c = new AdderInteger(cHash, q, 16).mod(q);
		AdderInteger realC = new AdderInteger(this.c, q);

        for (AdderInteger fakeC : cList)
            realC = realC.subtract(fakeC);

		sList.set(indexInDomain, realC.multiply(r).add(t));
		cList.set(indexInDomain, realC);
	}

	/**
	 * Verifies the proof given the ciphertext, public key, and
	 * domain.
	 *
	 * @param ciphertext    the ciphertext
	 * @param pubKey        the public key
	 * @param domain        the domain
	 * @return              true if the proof is valid
	 */
	public boolean verify(ElgamalCiphertext ciphertext, PublicKey pubKey, List<AdderInteger> domain) {

		p = pubKey.getP();
		q = pubKey.getQ();

		AdderInteger g = pubKey.getG();
		AdderInteger h = pubKey.getH();
		AdderInteger f = pubKey.getF();

		AdderInteger bigG = ciphertext.getG();
		AdderInteger bigH = ciphertext.getH();

		AdderInteger cChoices = new AdderInteger(AdderInteger.ZERO, q);

		StringBuilder sb = new StringBuilder(4096);

		sb.append(g);
		sb.append(h);
		sb.append(bigG);
		sb.append(bigH);

		int size = cList.size();

		for (int i = 0; i < size; i++) {

			AdderInteger d = domain.get(i);

            AdderInteger fpow = f.pow(d);

            AdderInteger s = sList.get(i);

            AdderInteger c = cList.get(i);

            AdderInteger negC = c.negate();

			cChoices = cChoices.add(c);

			sb.append(g.pow(s).multiply(bigG.pow(negC)));
			sb.append(h.pow(s).multiply(bigH.divide(fpow).pow(negC)));
		}

		String cHash = Util.sha1(sb.toString());
		AdderInteger newC = new AdderInteger(cHash, q, 16).mod(q);

		return (cChoices.equals(newC));
	}

	/**
	 * Creates a <tt>MembershipProof</tt> from the string standard representation
	 * as described in the {@link #toString} method.
	 *
	 * @param  s        a string that specifies a <tt>MembershipProof</tt>
	 * @return a        <tt>MembershipProof</tt> with the specified values
	 */
	public static MembershipProof fromString(String s) {
		StringTokenizer st = new StringTokenizer(s, "pyzsc", true);
		int numTokens = st.countTokens() - 2;

		if ((numTokens % 8) != 0) {
			throw new
			InvalidMembershipProofException("number of tokens not divisible by 8");
		}

		int count = numTokens / 8;

		try {
			if (!st.nextToken().equals("p")) {
				throw new InvalidMembershipProofException("expected token: `p\'");
			}

			AdderInteger p = new AdderInteger(st.nextToken());

			AdderInteger q
			= p.subtract(AdderInteger.ONE).divide(AdderInteger.TWO);

			List<AdderInteger> yList
			= new ArrayList<>(count);

			for (int ySize = 0; ySize < count; ySize++) {
				if (!st.nextToken().equals("y")) {
					throw new
					InvalidMembershipProofException("expected token: `y\'");
				}

				yList.add(new AdderInteger(st.nextToken(), p));
			}

			List<AdderInteger> zList
			= new ArrayList<>(count);

			for (int zSize = 0; zSize < count; zSize++) {
				if (!st.nextToken().equals("z")) {
					throw new
					InvalidMembershipProofException("expected token: `z\'");
				}

				zList.add(new AdderInteger(st.nextToken(), p));
			}

			List<AdderInteger> sList
			= new ArrayList<>(count);

			for (int sSize = 0; sSize < count; sSize++) {
				if (!st.nextToken().equals("s")) {
					throw new
					InvalidMembershipProofException("expected token: `s\'");
				}

				sList.add(new AdderInteger(st.nextToken(), q));
			}

			List<AdderInteger> cList
			= new ArrayList<>(count);

			for (int cSize = 0; cSize < count; cSize++) {
				if (!st.nextToken().equals("c")) {
					throw new
					InvalidMembershipProofException("expected token: `c\'");
				}

				cList.add(new AdderInteger(st.nextToken(), q));
			}

			return new MembershipProof(p, q, yList, zList, sList, cList);
		}
        catch (NoSuchElementException | NumberFormatException nsee) { throw new InvalidMembershipProofException(nsee.getMessage()); }
    }

	/**
	 * Returns a <code>String</code> object representing this <code>MembershipProof</code>.
     *
	 * @return the string representation of this proof
	 */
	public String toString() {

		StringBuilder sb = new StringBuilder(4096);

		sb.append("p");
		sb.append(p);

        for (AdderInteger y : yList) {
            sb.append("y");
            sb.append(y);
        }

        for (AdderInteger z : zList) {
            sb.append("z");
            sb.append(z);
        }

        for (AdderInteger s : sList) {
            sb.append("s");
            sb.append(s);
        }

        for (AdderInteger c1 : cList) {
            sb.append("c");
            sb.append(c1);
        }

		return sb.toString();
	}

	/**
     * Method for interop with VoteBox's S-Expression system.
     *
     * @return the S-Expression equivalent of this MembershipProof
     */
	public ASExpression toASE(){
		List<ASExpression> yListL = new ArrayList<>();
		for(AdderInteger y : yList)
			yListL.add(y.toASE());

		List<ASExpression> zListL = new ArrayList<>();
		for(AdderInteger z : zList)
			zListL.add(z.toASE());

		List<ASExpression> sListL = new ArrayList<>();
		for(AdderInteger s : sList)
			sListL.add(s.toASE());

		List<ASExpression> cListL = new ArrayList<>();
		for(AdderInteger c : cList)
			cListL.add(c.toASE());

		return new ListExpression(StringExpression.makeString("membership-proof"),
				p.toASE(),
				new ListExpression(yListL),
				new ListExpression(zListL),
				new ListExpression(sListL),
				new ListExpression(cListL));
	}

	/**
     * Method for interop with VoteBox's S-Expression system.
     *
     * @param ase    S-Expression representation of a MembershipProof
     * @return       the MembershipProof equivalent of ase
     */
	public static MembershipProof fromASE(ASExpression ase){
		ListExpression exp = (ListExpression)ase;

		if(!(exp.get(0)).toString().equals("membership-proof"))
			throw new RuntimeException("Not membership-proof");

		AdderInteger p = AdderInteger.fromASE(exp.get(1));

		List<AdderInteger> yList = new ArrayList<>();
		List<AdderInteger> zList = new ArrayList<>();
		List<AdderInteger> sList = new ArrayList<>();
		List<AdderInteger> cList = new ArrayList<>();

		ListExpression yListE = (ListExpression)exp.get(2);
		ListExpression zListE = (ListExpression)exp.get(3);
		ListExpression sListE = (ListExpression)exp.get(4);
		ListExpression cListE = (ListExpression)exp.get(5);

		for(int i = 0; i < yListE.size(); i++) yList.add(AdderInteger.fromASE(yListE.get(i)));
		for(int i = 0; i < zListE.size(); i++) zList.add(AdderInteger.fromASE(zListE.get(i)));
		for(int i = 0; i < sListE.size(); i++) sList.add(AdderInteger.fromASE(sListE.get(i)));
		for(int i = 0; i < cListE.size(); i++) cList.add(AdderInteger.fromASE(cListE.get(i)));

		AdderInteger q = p.subtract(AdderInteger.ONE).divide(AdderInteger.TWO);

		return new MembershipProof(p, q, yList, zList, sList, cList);
	}
}
