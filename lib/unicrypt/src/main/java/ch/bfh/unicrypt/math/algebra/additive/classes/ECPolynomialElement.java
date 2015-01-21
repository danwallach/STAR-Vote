package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.classes;

import ch.bfh.unicrypt.helper.Point;
import ch.bfh.unicrypt.helper.Polynomial;
import ch.bfh.unicrypt.math.algebra.additive.abstracts.AbstractECElement;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.PolynomialElement;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModTwo;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.DualisticElement;

/**
 *
 * @author Christian Lutz
 * <p>
 */
public class ECPolynomialElement
	   extends AbstractECElement<Polynomial<? extends DualisticElement<ZModTwo>>, PolynomialElement<ZModTwo>, ECPolynomialElement> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	protected ECPolynomialElement(ECPolynomialField ecGroup, Point<PolynomialElement<ZModTwo>> value) {
		super(ecGroup, value);
	}

	public ECPolynomialElement(ECPolynomialField ecGroup) {
		super(ecGroup);
	}

}
