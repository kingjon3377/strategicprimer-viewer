package common.map;

import java.math.BigInteger;
import java.math.BigDecimal;

/**
 * An interface for fixtures representing large features that report their extent in acres.
 *
 * @see HasPopulation
 */
public interface HasExtent<Self extends HasExtent<Self>> extends IFixture, Subsettable<IFixture> {
	/**
	 * Determine whether the given number is positive.
	 */
	static boolean isPositive(Number number) {
		if (number instanceof Integer) {
			return number.intValue() > 0;
		} else if (number instanceof Long) {
			return number.longValue() > 0L;
		} else if (number instanceof Float) {
			return number.floatValue() > 0.0f;
		} else if (number instanceof Double) {
			return number.doubleValue() > 0.0;
		} else if (number instanceof BigInteger) {
			return ((BigInteger) number).signum() > 0;
		} else if (number instanceof BigDecimal) {
			return ((BigDecimal) number).signum() > 0;
		} else {
			return number.doubleValue() > 0.0;
		}
	}

	/**
	 * Adds two possibly-arbitrary-type numbers. To suit the needs of
	 * implementations of this interface, if one and only one is negative,
	 * it is treated as zero.
	 */
	static Number sum(Number one, Number two) {
		if (isPositive(one) && !isPositive(two)) {
			return one;
		} else if (isPositive(two) && !isPositive(one)) {
			return two;
		}

		if (one instanceof Integer || one instanceof Long) {
			if (two instanceof Integer || two instanceof Long) {
				return one.longValue() + two.longValue();
			} else if (two instanceof BigInteger) {
				return BigInteger.valueOf(one.longValue()).add((BigInteger) two);
			} else if (two instanceof Float || two instanceof Double) {
				return one.doubleValue() + two.doubleValue();
			} else if (two instanceof BigDecimal) {
				return BigDecimal.valueOf(one.longValue()).add((BigDecimal) two);
			} else {
				throw new IllegalStateException("Unhandled Number type for second argument");
			}
		} else if (one instanceof BigInteger) {
			if (two instanceof Integer || two instanceof Long) {
				return ((BigInteger) one).add(BigInteger.valueOf(two.longValue()));
			} else if (two instanceof BigInteger) {
				return ((BigInteger) one).add((BigInteger) two);
			} else if (two instanceof Float || two instanceof Double) {
				return one.doubleValue() + two.doubleValue();
			} else if (two instanceof BigDecimal) {
				return new BigDecimal((BigInteger) one).add((BigDecimal) two);
			} else {
				throw new IllegalStateException("Unhandled Number type for second argument");
			}
		} else if (one instanceof Float || one instanceof Double) {
			if (two instanceof Integer || two instanceof Long || two instanceof BigInteger ||
					two instanceof Float || two instanceof Double) {
				return one.doubleValue() + two.doubleValue();
			} else if (two instanceof BigDecimal) {
				return BigDecimal.valueOf(one.doubleValue()).add((BigDecimal) two);
			} else {
				throw new IllegalStateException("Unhandled Number type for second argument");
			}
		} else if (one instanceof BigDecimal) {
			if (two instanceof BigDecimal) {
				return ((BigDecimal) one).add((BigDecimal) two);
			} else if (two instanceof Integer || two instanceof Long) {
				return ((BigDecimal) one).add(BigDecimal.valueOf(two.longValue()));
			} else if (two instanceof BigInteger) {
				return ((BigDecimal) one).add(new BigDecimal((BigInteger) two));
			} else if (two instanceof Float || two instanceof Double) {
				return ((BigDecimal) one).add(BigDecimal.valueOf(two.doubleValue()));
			} else {
				throw new IllegalStateException("Unhandled Number type for second argument");
			}
		} else {
			throw new IllegalStateException("Unhandled Number subclass for first argument");
		}
	}

	/**
	 * The number of acres the fixture extends over.
	 */
	Number getAcres();

	/**
	 * Returns a copy of this object, except with its extent increased by
	 * the extent of the {@link addend}, which must be of the same type.
	 */
	Self combined(Self addend);

	/**
	 * Returns a copy of this object, except with its extent reduced by {@link subtrahend} acres.
	 */
	Self reduced(Number subtrahend);
}
