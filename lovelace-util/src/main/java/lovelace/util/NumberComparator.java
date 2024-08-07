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

	private static boolean isIntegral(final Number number) {
		return number instanceof Integer || number instanceof Long;
	}

	private static boolean isFloatingPoint(final Number number) {
		return number instanceof Float || number instanceof Double;
	}

	/**
	 * Compare two numbers. If they are the same type, delegate to their
	 * built-in comparison function; if not, convert both to doubles and
	 * return the result of comparing those.
	 */
	@SuppressWarnings({"QuestionableName", "MethodWithMultipleReturnPoints"})
	public static int compareNumbers(final Number one, final Number two) {
		if (isIntegral(one) && isIntegral(two)) {
			return Long.compare(one.longValue(), two.longValue());
		} else if (isFloatingPoint(one) && isFloatingPoint(two)) {
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
	@SuppressWarnings("QuestionableName")
	@Override
	public final int compare(final Number one, final Number two) {
		return compareNumbers(one, two);
	}
}
