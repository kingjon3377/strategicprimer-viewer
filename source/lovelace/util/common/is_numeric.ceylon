import ceylon.test {
    test,
    assertTrue,
    assertFalse,
    assertNull,
    assertEquals,
    assumeTrue
}

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

Logger log = logger(`module lovelace.util.common`);

native object numParsingHelper {
    native shared Boolean isNumeric(String string) =>
        Integer.parse(string) is Integer;
    native shared Integer? parseInt(String string) {
        if (is Integer retval = Integer.parse(string)) {
            return retval;
        } else {
            return null;
        }
    }
    shared test
    void testIsNumeric() {
        assertTrue(isNumeric("1"), "1 is numeric");
        assertFalse(isNumeric("xyzzy"), "xyzzy is not numeric");
    }
    shared native test
    void testJVMIsNumeric() {
        assumeTrue(false);
    }
    shared native test
    void testJVMParseInt() {
        assumeTrue(false);
    }
}
native("jvm") object numParsingHelper {
    NumberFormat parser = NumberFormat.integerInstance;
    native("jvm") shared Boolean isNumeric(String string) {
        try {
            parser.parse(string);
            return true;
        } catch (NumberFormatException|ParseException|JParseException ignored) {
            return false;
        }
    }
    native("jvm") shared Integer? parseInt(String string) {
        try {
            return parser.parse(string).intValue();
        } catch (NumberFormatException|ParseException|JParseException except) {
            log.debug("Failed to parse a number from the following input: ``string``",
                except);
            return null;
        }
    }
    shared native("jvm") test
    void testJVMIsNumeric() {
        assertTrue(isNumeric("1"), "1 is numeric");
        assertFalse(isNumeric("xyzzy"), "xyzzy is not numeric");
        assertTrue(isNumeric("1,000"), "A number with commas is numeric");
    }
    shared native("jvm") test
    void testJVMParseInt() {
        assertEquals(parseInt("-5,127"), -5127,
            "parseInt() parses comma-containing numbers");
        assertEquals(parseInt("2345"), 2345, "parseInt() doesn't require commas");
        assertNull(parseInt("alphabetic"));
    }
}
"Whether the given string contains numeric data"
shared Boolean isNumeric(String string) => numParsingHelper.isNumeric(string);

"Parse an integer, returning null if non-numeric. On the JVM, the number may
 contain commas."
shared Integer? parseInt(String string) => numParsingHelper.parseInt(string);
