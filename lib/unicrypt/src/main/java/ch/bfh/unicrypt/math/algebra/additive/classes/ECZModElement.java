package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.classes;

import ch.bfh.unicrypt.helper.Point;
import ch.bfh.unicrypt.math.algebra.additive.abstracts.AbstractECElement;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import java.math.BigInteger;

public class ECZModElement
	   extends AbstractECElement<BigInteger, ZModElement, ECZModElement> {

	protected ECZModElement(ECZModPrime ecGroup) {
		super(ecGroup);

	}

	protected ECZModElement(ECZModPrime ecGroup, Point<ZModElement> value) {
		super(ecGroup, value);

	}

}
