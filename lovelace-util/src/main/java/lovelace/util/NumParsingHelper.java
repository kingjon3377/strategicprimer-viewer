package lovelace.util;

import java.text.NumberFormat;
import java.text.ParseException;

import java.util.OptionalInt;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class NumParsingHelper {
	private static final Logger LOGGER = Logger.getLogger(NumParsingHelper.class.getName());

	private static final NumberFormat PARSER = NumberFormat.getIntegerInstance();

	/**
	 * Whether the given string contains numeric data, such as will be successfully parsed by {@link parseInt}.
	 */
	public static boolean isNumeric(final String string) {
		try {
			PARSER.parse(string);
			return true;
		} catch (NumberFormatException|ParseException ignored) {
			return false;
		}
	}

	// TODO: Should probably just return 'int', or 'long', and declare as throwing ParseException.
	/**
	 * Parse an integer, which may contain commas, returning an empty Optional if non-numeric.
	 */
	public static OptionalInt parseInt(final String string) {
		try {
			return OptionalInt.of(PARSER.parse(string).intValue());
		} catch (NumberFormatException|ParseException except) {
			LOGGER.log(Level.FINE, except,
				() -> "Failed to parse a number from the following input: " + string);
			return OptionalInt.empty();
		}
	}
}


