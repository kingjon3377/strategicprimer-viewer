package util;

import java.text.NumberFormat;
import java.text.ParseException;

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
	 * The NumberFormat object we use for parsing.
	 */
	private static final NumberFormat PARSER = NullCleaner.assertNotNull(NumberFormat.getIntegerInstance());
	/**
	 * @param string a String
	 * @return whether it contains numeric data or not
	 */
	public static boolean isNumeric(final String string) {
		try {
			PARSER.parse(string);
			return true; // NOPMD
		} catch (final NumberFormatException except) {
			return false;
		} catch (ParseException e) {
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
