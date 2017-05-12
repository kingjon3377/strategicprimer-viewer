/*import ceylon.test {
    test,
    assertTrue,
    assertFalse,
    assertEquals,
    assertNull
}*/

import java.lang {
    NumberFormatException
}
import java.text {
    NumberFormat,JParseException=ParseException
}

import ceylon.logging {
    Logger,
    logger
}
NumberFormat intParser = NumberFormat.integerInstance;
"Whether the given string contains numeric data"
shared Boolean isNumeric(String string) {
    try {
        intParser.parse(string);
        return true;
    } catch (NumberFormatException|ParseException|JParseException ignored) {
        return false;
    }
}
Logger log = logger(`module lovelace.util.jvm`);
"Parse an integer, which may contain commas; return null if non-numeric."
shared Integer? parseInt(String string) {
    try {
        return intParser.parse(string).intValue();
    } catch (NumberFormatException|JParseException|ParseException ignored) {
        log.debug("Failed to parse a number from the following input: ``string``",
            ignored);
        return null;
    }
}
// TODO: Uncomment tests once Ceylon bug #6986 is fixed
/*test
void testIsNumeric() {
    assertTrue(isNumeric("1"), "1 is numeric");
    assertFalse(isNumeric("xyzzy"), "xyzzy is not numeric");
    assertTrue(isNumeric("1,000"), "A number with commas is numeric");
}

test
void testParseInt() {
    assertEquals(parseInt("-5,127"), -5127, "parseInt() parses comma-containing numbers");
    assertEquals(parseInt("2345"), 2345, "parseInt() doesn't require commas");
    assertNull(parseInt("alphabetic"));
}*/