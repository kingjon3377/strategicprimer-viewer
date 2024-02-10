package lovelace.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import static lovelace.util.NumParsingHelper.isNumeric;
import static lovelace.util.NumParsingHelper.parseInt;

public final class TestIsNumeric {
	@Test
	public void testIsNumeric() {
		assertTrue(isNumeric("1"), "1 is numeric");
		assertFalse(isNumeric("non-numeric"), "a word is not numeric");
		assertTrue(isNumeric("1,000"), "A number with commas is numeric");
	}

	@SuppressWarnings("NewExceptionWithoutArguments")
	@Test
	public void testParseInt() {
		assertEquals(-5127, parseInt("-5,127").orElseThrow(IllegalStateException::new),
			"parseInt() parses comma-containing numbers");
		assertEquals(2345, parseInt("2345").orElseThrow(IllegalStateException::new),
			"parseInt() doesn't require commas");
		assertFalse(parseInt("alphabetic").isPresent(), "parsing of non-numeric data fails");
	}
}
