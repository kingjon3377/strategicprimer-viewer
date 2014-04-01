package util;

/**
 * A helper class to determine whether a String is numeric or not.
 *
 * @author Jonathan Lovelace
 *
 */
public final class IsNumeric {
	/**
	 * Do not instantiate.
	 */
	private IsNumeric() {
		// Do nothing.
	}

	/**
	 * @param string a String
	 * @return whether it contains numeric data or not
	 */
	public static boolean isNumeric(final String string) {
		try {
			Integer.parseInt(string);
			return true; // NOPMD
		} catch (final NumberFormatException except) {
			return false;
		}
	}

	/**
	 * @param value a value
	 * @param lower a lower bound
	 * @param upper an upper bound
	 * @return whether the value is between them
	 */
	public static boolean isBetween(final int value, final int lower,
			final int upper) {
		return value >= lower && value < upper;
	}
}
