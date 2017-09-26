import ceylon.test { test, assertTrue, assertFalse }
"Whether the given string contains numeric data"
shared Boolean isNumeric(String string) {
    value temp = Integer.parse(string);
    return temp is Integer;
}

test
void testIsNumeric() {
    assertTrue(isNumeric("1"), "1 is numeric");
    assertFalse(isNumeric("xyzzy"), "xyzzy is not numeric");
}
