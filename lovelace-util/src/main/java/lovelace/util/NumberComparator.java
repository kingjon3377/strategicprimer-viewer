package lovelace.util;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;

/**
 * A class providing a comparison for {@link Number numbers} of unknown or varied types.
 */
public class NumberComparator implements Comparator<Number>, Serializable {
	@Serial
	private static final long serialVersionUID = 0L;

	/**
	 * Compare two numbers. If they are the same type, delegate to their
	 * built-in comparison function; if not, convert both to doubles and
	 * return the result of comparing those.
	 */
	public static int compareNumbers(final Number one, final Number two) {
		if ((one instanceof Integer || one instanceof Long) &&
				(two instanceof Integer || two instanceof Long)) {
			return Long.compare(one.longValue(), two.longValue());
		} else if ((one instanceof Float || one instanceof Double) &&
				(two instanceof Float || two instanceof Double)) {
			return Double.compare(one.doubleValue(), two.doubleValue());
		} else if (one instanceof BigDecimal && two instanceof BigDecimal) {
			return ((BigDecimal) one).compareTo((BigDecimal) two);
		} else if (one instanceof BigInteger && two instanceof BigInteger) {
			return ((BigInteger) one).compareTo((BigInteger) two);
		} else {
			return Double.compare(one.doubleValue(), two.doubleValue());
		}
	}

	/**
	 * Compare two numbers. If they are the same type, delegate to their
	 * built-in comparison function; if not, convert both to doubles and
	 * return the result of comparing those.
	 */
	@Override
	public final int compare(final Number one, final Number two) {
		return compareNumbers(one, two);
	}
}
