package lovelace.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;

/**
 * A class providing a comparison for [[Numbers|Number]] of unknown or varied types. 
 */
public class NumberComparator implements Comparator<Number> {
	/**
	 * Compare two numbers. If they are the same type, delegate to their
	 * built-in comparison function; if not, convert both to doubles and
	 * return the result of comparing those.
	 */
	@Override
	public int compare(Number one, Number two) {
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
}
