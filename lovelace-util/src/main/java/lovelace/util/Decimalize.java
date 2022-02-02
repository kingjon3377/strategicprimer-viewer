package lovelace.util;

import java.math.BigInteger;
import java.math.BigDecimal;

public class Decimalize {
	private Decimalize() {
	}

	/**
	 * If {@link number} is a {@link BigDecimal}, return it; otherwise,
	 * return a BigDecimal representation of the same number. Note that
	 * this will most likely throw if it is a floating-point number and
	 * either infinity or `NaN`.
	 */
	public static BigDecimal decimalize(Number number) {
		if (number instanceof BigDecimal) {
			return (BigDecimal) number;
		} else if (number instanceof BigInteger) {
			return new BigDecimal((BigInteger) number);
		} else if (number instanceof Integer) {
			return BigDecimal.valueOf((Integer) number);
		} else if (number instanceof Long) {
			return BigDecimal.valueOf((Long) number);
		} else {
			// Most likely float or double
			return new BigDecimal(number.doubleValue());
		}
	}
}
