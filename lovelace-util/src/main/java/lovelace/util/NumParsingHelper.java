package lovelace.util;

import java.text.NumberFormat;
import java.text.ParseException;

import java.util.OptionalInt;

public final class NumParsingHelper {
	private static final NumberFormat PARSER = NumberFormat.getIntegerInstance();

	private NumParsingHelper() {
	}

	/**
	 * Whether the given string contains numeric data, such as will be successfully parsed by {@link #parseInt}.
	 */
	public static boolean isNumeric(final String string) {
		try {
			PARSER.parse(string);
			return true;
		} catch (final NumberFormatException|ParseException ignored) {
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
		} catch (final NumberFormatException|ParseException except) {
			LovelaceLogger.debug(except,
				"Failed to parse a number from the following input: {}", string);
			return OptionalInt.empty();
		}
	}
}


