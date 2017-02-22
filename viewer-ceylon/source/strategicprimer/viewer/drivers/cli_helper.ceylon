import controller.map.misc {
    CLIHelper,
    ICLIHelper
}
import java.io {
    StringWriter,
    StringReader,
    OutputStreamWriter,
    IOException
}
import java.lang {
    JString=String
}
import ceylon.interop.java {
    JavaList,
    javaString
}
import ceylon.test {
    assertEquals,
    test,
    assertThatException
}
import ceylon.collection {
    ArrayList
}
import model.map {
    PlayerImpl,
    HasName,
    PointFactory
}
import java.util {
    JCollections = Collections
}
import java.math {
    BigDecimal
}
import util {
    NullStream
}
// ICLIHelper and CLIHelper will eventually go here, but we start with the tests.
"A helper method to condense tests."
void assertCLI<out T>(
        "The method under test, partially applied so all it lacks is the CLIHelper."
        T(ICLIHelper) method,
        "The lines of input to pass to the CLIHelper's input stream."
        {String*} input,
        "What the CLIHelper is expected to print to its output stream. If an Iterable,
         it's the lines of output, each *except the last* followed by a newline."
        String|{String*} expectedOutput,
        "The expected result of the method under test."
        T expectedResult,
        "The assertion message for the assertion that the result is as expected."
        String resultMessage = "CLIHelper method result was as expected",
        "The assertion message for the assertion that the output is as expected."
        String outputMessage = "CLIHelper output was as expected") {
    StringBuilder inputBuilder = StringBuilder();
    for (string in input) {
        inputBuilder.append(string);
        inputBuilder.appendNewline();
    }
    String expectedOutputReal;
    if (is String expectedOutput) {
        expectedOutputReal = expectedOutput;
    } else if (exists first = expectedOutput.first) {
        StringBuilder outputBuilder = StringBuilder();
        outputBuilder.append(first);
        for (string in expectedOutput.rest) {
            outputBuilder.appendNewline();
            outputBuilder.append(string);
        }
        expectedOutputReal = outputBuilder.string;
    } else {
        expectedOutputReal = "";
    }
// FIXME: Figure out how to make compiler accept multiple disparate resources in one stmt
    try (ostream = StringWriter()) {
        try (cli = CLIHelper(StringReader(inputBuilder.string), ostream)) {
            assertEquals(method(cli), expectedResult, resultMessage);
        }
        assertEquals(ostream.string, expectedOutput, outputMessage);
    }
}

"Test chooseFromList()."
test
void testChooseFromList() {
    assertCLI((cli) => cli.chooseFromList(JavaList(ArrayList { PlayerImpl(1, "one"),
            PlayerImpl(2, "two") }), "test desc", "none present", "prompt", false), {"0"},
        {"test desc", "0: one", "1: two", "prompt"}, 0,
        "chooseFromList chooses the one specified by the user",
        "chooseFromList prompted the user");
    assertCLI((cli) => cli.chooseFromList(JavaList(ArrayList { PlayerImpl(1, "one"),
            PlayerImpl(2, "two") }), "test desc", "none present", "prompt", true), {"1"},
        {"test desc", "0: one", "1: two", "prompt"}, 1,
        "chooseFromList chooses the one specified by the user",
        "chooseFromList prompted the user");
    assertCLI((cli) => cli.chooseFromList(JCollections.singletonList(
            PlayerImpl(1, "one")), "test desc", "none present", "prompt", true),
        {}, {"test desc", "Automatically choosing only item, one", ""}, 0,
        "chooseFromList chooses only choice when this is specified",
        "chooseFromList automatically chose only choice");
    assertCLI((cli) => cli.chooseFromList(JCollections.singletonList(
            PlayerImpl(1, "one")), "test desc", "none present", "prompt", false), {"0"},
        {"test desc", "0: one", "prompt"}, 0,
        "chooseFromList doesn't always auto-choose only choice",
        "chooseFromList didn't automatically choose only choice");
}
"A second test of chooseFromList"
test
void testChooseFromListMore() {
    assertCLI((cli) => cli.chooseFromList(JavaList(ArrayList { PlayerImpl(1, "one"),
            PlayerImpl(2, "two") }), "test desc", "none present", "prompt ", false),
        {"-1", "0"}, {"test desc", "0: one", "1: two", "prompt prompt "}, 0,
        "chooseFromList prompts again when negative index given",
        "chooseFromList prompts again when negative index given");
    assertCLI((cli) => cli.chooseFromList(JavaList(ArrayList { PlayerImpl(1, "one"),
            PlayerImpl(2, "two") }), "test desc", "none present", "prompt", false), {"3"},
        {"test desc", "0: one", "1: two", "prompt"}, 3,
        "chooseFromList allows too-large choice",
        "chooseFromList allows too-large choice");
    assertCLI((cli) => cli.chooseFromList(JavaList(ArrayList { PlayerImpl(1, "one"),
            PlayerImpl(2, "two") }), "test desc", "none present", "prompt", true), {"0"},
        {"test desc", "0: one", "1: two", "prompt"}, 0,
        "chooseFromList asks even if 'auto' when multiple items",
        "chooseFromList prompted the user");
    assertCLI((cli) => cli.chooseFromList(JCollections.emptyList<HasName>(),
            "test desc", "none present", "prompt", false), {}, {"none present", ""}, -1,
        "chooseFromList handles no-item case", "chooseFromList didn't prompt the user");
}
"Test inputNumber"
test
void testInputNumber() {
    assertCLI((cli) => cli.inputNumber("test prompt"), {"2"}, "test prompt", 2,
        "inputNumber works", "inputNumber uses given prompt");
    assertCLI((cli) => cli.inputNumber("test prompt two"), {"8"}, "test prompt two", 8,
        "inputNumber works", "inputNumber uses given prompt");
    assertCLI((cli) => cli.inputNumber("test prompt three "), {"-1", "0"},
        "test prompt three test prompt three ", 0,
        "inputNumber asks again on negative input",
        "inputNumber asks again on negative input");
    assertCLI((cli) => cli.inputNumber("test prompt four "), {"not-number", "9"},
        "test prompt four test prompt four ", 9,
        "inputNumber asks again on non-numeric input",
        "inputNumber asks again on non-numeric input");
    try (cli = CLIHelper(StringReader(""), OutputStreamWriter(NullStream()))) {
        assertThatException(() => cli.inputNumber("test prompt")).hasType(`IOException`);
    }
}
"Test inputDecimal"
test
void testInputDecimal() {
    assertCLI((cli) => cli.inputDecimal("test prompt"), {"10"}, "test prompt",
        BigDecimal.ten, "inputDecimal works with integers",
        "inputDecimal uses given prompt");
    assertCLI((cli) => cli.inputDecimal("test prompt two"), {"2.5"}, "test prompt two",
        BigDecimal(5).divide(BigDecimal(2)), "inputDecimal works with decimals",
        "inputDecimal uses given prompt");
    assertCLI((cli) => cli.inputDecimal("test prompt three "), {"-2.5", "0.5"},
        "test prompt three test prompt three ", BigDecimal.one.divide(BigDecimal(2)),
        "inputDecimal asks again on negative input",
        "inputDecimal asks again on negative input");
    assertCLI((cli) => cli.inputDecimal("test prompt four "), {"non-number", ".1"},
        {"test prompt four Invalid number.", "test prompt four "},
        BigDecimal.one.divide(BigDecimal.ten),
        "inputDecimal asks again on non-numerc input",
        "inputDecimal asks again on non-numeric input");
}
"Test for inputString()"
test
void testInputString() {
    assertCLI((cli) => cli.inputString("string prompt"), {"first"}, "string prompt",
        "first", "inputString returns the entered string", "inputString displays prompt");
    assertCLI((cli) => cli.inputString("second prompt"), {"second"}, "second prompt",
        "second", "inputString returns the entered string", "inputString displays prompt");
    assertCLI((cli) => cli.inputString("third prompt"), {}, "third prompt", "",
        "inputString returns empty on EOF", "inputString displays prompt");
}
"Test for inputBoolean()"
test
void testInputBoolean() {
    for (arg in {"yes", "true", "y", "t"}) {
        assertCLI((cli) => cli.inputBoolean("bool prompt"), {arg}, "bool prompt", true,
            "inputBoolean returns true on '``arg``", "inputBoolean displays prompt");
    }
    for (arg in {"no", "false", "n", "f"}) {
        assertCLI((cli) => cli.inputBoolean("prompt two"), {arg}, "prompt two", false,
            "inputBoolean returns false on ``arg``", "inputBoolean displays prompt");
    }
    assertCLI((cli) => cli.inputBoolean("prompt three "), {"yoo-hoo", "yes"},
        {"prompt three Please enter 'yes', 'no', 'true', or 'false',",
            "or the first character of any of those.", "prompt three "}, true,
        "inputBoolean rejects other input",
        "inputBoolean gives message on invalid input");
}

"Test the input-boolean-with-skipping functionality."
test
void testInputBooleanInSeries() {
    for (arg in {"yes", "true", "y", "t"}) {
        assertCLI((cli) => cli.inputBooleanInSeries("bool prompt"), {arg}, "bool prompt",
            true, "inputBooleanInSeries returns true on '``arg``",
            "inputBooleanInSeries displays prompt");
    }
    for (arg in {"no", "false", "n", "f"}) {
        assertCLI((cli) => cli.inputBooleanInSeries("prompt two"), {arg}, "prompt two",
            false, "inputBooleanInSeries returns false on ``arg``",
            "inputBooleanInSeries displays prompt");
    }
    assertCLI((cli) => cli.inputBooleanInSeries("prompt three "), {"nothing", "true"},
        {"prompt three Please enter 'yes', 'no', 'true', or 'false', the first",
            "character of any of those, or 'all', 'none', 'always'",
            "or 'never' to use the same answer for all further questions",
            "prompt three "}, true,
        "inputBoolean rejects other input",
        "inputBoolean gives message on invalid input");
    try (ostream = StringWriter()) {
        try (cli = CLIHelper(StringReader("""all
                                             """), ostream)) {
            assertEquals(cli.inputBooleanInSeries("prompt four "), true,
                "inputBooleanInSeries allows yes-to-all");
            assertEquals(cli.inputBooleanInSeries("prompt four "), true,
                "inputBooleanInSeries honors yes-to-all when prompt is the same");
            assertEquals(ostream.string, """prompt four prompt four yes
                                            """",
                "inputBooleanInSeries shows automatic yes");
            assertThatException(() => cli.inputBooleanInSeries("other prompt"))
                .hasType(`IOException`);
        }
    }
    try (ostream = StringWriter()) {
        try (cli = CLIHelper(StringReader("""none
                                             """), ostream)) {
            assertEquals(cli.inputBooleanInSeries("prompt five "), false,
                "inputBooleanInSeries allows no-to-all");
            assertEquals(cli.inputBooleanInSeries("prompt five "), false,
                "inputBooleanInSeries honors no-to-all when prompt is the same");
            assertEquals(ostream.string, """prompt five prompt five no
                                            """,
                "inputBooleanInSeries shows automatic no");
            assertThatException(() => cli.inputBooleanInSeries("other prompt"))
                .hasType(`IOException`);
        }
    }
    try (ostream = StringWriter()) {
        try (cli = CLIHelper(StringReader("""always
                                             """), ostream)) {
            assertEquals(cli.inputBooleanInSeries("prompt six ", "key"), true,
                "inputBooleanInSeries allows yes-to-all");
            assertEquals(cli.inputBooleanInSeries("prompt seven ", "key"), true,
                "inputBooleanInSeries honors yes-to-all if prompt differs, same key");
            assertEquals(ostream.string, """prompt six prompt seven yes
                                            """,
                "inputBooleanInSeries shows automatic yes");
        }
    }
    try (ostream = StringWriter()) {
        try (cli = CLIHelper(StringReader("""never
                                             """), ostream)) {
            assertEquals(cli.inputBooleanInSeries("prompt eight ", "secondKey"), false,
                "inputBooleanInSeries allows no-to-all");
            assertEquals(cli.inputBooleanInSeries("prompt nine ", "secondKey"), false,
                "inputBooleanInSeries honors no-to-all if prompt differs, same key");
            assertEquals(ostream.string, """prompt eight prompt nine no
                                            """,
                "inputBooleanInSeries shows automatic no");
        }
    }
    try (ostream = StringWriter()) {
        try (cli = CLIHelper(StringReader("""all
                                             none
                                             """), ostream)) {
            assertEquals(cli.inputBooleanInSeries("prompt ten ", "thirdKey"), true,
                "inputBooleanInSeries allows yes-to-all with one key");
            assertEquals(cli.inputBooleanInSeries("prompt eleven ", "fourthKey"), false,
                "inputBooleanInSeries allows no-to-all with second key");
            assertEquals(cli.inputBooleanInSeries("prompt twelve ", "thirdKey"), true,
                "inputBooleanInSeries then honors yes-to-all");
            assertEquals(cli.inputBooleanInSeries("prompt thirteen ", "fourthKey"), false,
                "inputBooleanInSeries then honors no-to-all");
            assertEquals(ostream.string, """prompt ten prompt eleven prompt twelve yes
                                            prompt thirteen no
                                            """", "inputBooleanInSeries shows prompts");
        }
    }
}
"Test of chooseStringFromList()"
test
void testChooseStringFromList() {
    assertCLI((cli) => cli.chooseStringFromList(JavaList(ArrayList{javaString("one"),
            javaString("two")}), "test desc", "none present", "prompt", false), {"0"},
        {"test desc", "0: one", "1: two", "prompt"}, 0,
        "chooseStringFromList chooses the one specified by the user",
        "chooseStringFromList prompts the user");
    assertCLI((cli) => cli.chooseStringFromList(JavaList(ArrayList{javaString("one"),
            javaString("two"), javaString("three")}), "test desc", "none present",
            "prompt two", true), {"1"},
        {"test desc", "0: one", "1: two", "2: three", "prompt two"}, 1,
        "chooseStringFromList chooses the one specified by the user",
        "chooseStringFromList prompts the user");
    assertCLI((cli) => cli.chooseStringFromList(JCollections.singletonList(
            javaString("one")), "test desc", "none present", "prompt", true), {},
        {"test desc", "Automatically choosing only item, one", ""}, 0,
        "chooseStringFromList automatically chooses only choice when told to",
        "chooseStringFromList automatically chose only choice");
    assertCLI((cli) => cli.chooseStringFromList(JCollections.singletonList(
            javaString("one")), "test desc", "none present", "prompt", false), {"0"},
        {"test desc", "0: one", "prompt"}, 0,
        "chooseStringFromList doesn't always auto-choose",
        "chooseStringFromList didn't automatically choose only choice");
}
"A second test of chooseStringFromList"
test
void testChooseStringFromListMore() {
    assertCLI((cli) => cli.chooseStringFromList(JavaList(ArrayList{
            javaString("zero"), javaString("one"), javaString("two")}), "test desc",
        "none present", "prompt", true), {"1"},
        {"test desc", "0: zero", "1: one", "2: two", "prompt"}, 1,
        "chooseStringFromList doesn't auto-choose when more than one item",
        "chooseStringFromList doesn't auto-choose when more than one item");
    assertCLI((cli) => cli.chooseStringFromList(JavaList(ArrayList{javaString("one"),
            javaString("two")}), "test desc", "none present", "prompt", false),
        {"-1", "0"}, {"test desc", "0: one", "1: two", "promptprompt"}, 0,
        "chooseStringFromList prompts again when negative index given",
        "chooseStringFromList prompts again when negative index given");
    assertCLI((cli) => cli.chooseStringFromList(JavaList(ArrayList{javaString("one"),
            javaString("two")}), "test desc", "none present", "prompt", false), {"3"},
        {"test desc", "0: one", "1: two", "prompt"}, 3,
        "chooseStringFromList allows too-large choice",
        "chooseStringFromList allows too-large choice");
    assertCLI((cli) => cli.chooseStringFromList(JCollections.emptyList<JString>(),
            "test desc", "none present", "prompt", false), {}, {"none present", ""}, -1,
        "chooseStringFromList handles empty list",
        "chooseStringFromList handles empty list");
}
"Test print() and friends"
test
void testPrinting() {
    void assertHelper(Anything(ICLIHelper) method, String expected, String message) {
        try (ostream = StringWriter()) {
            try (cli = CLIHelper(StringReader(""), ostream)) {
                method(cli);
                assertEquals(ostream.string, expected, message);
            }
        }
    }
    assertHelper((cli) => cli.print("test string"), "test string",
        "print() prints string");
    assertHelper((cli) => cli.println("test two"), """test two
                                                      """, "println() adds newline");
    assertHelper((cli) => cli.printf("test %s", "three"), "test three", "printf() works");
}

"Test inputPoint()"
test
void testInputPoint() {
    assertCLI((cli) => cli.inputPoint("point prompt one "), {"2", "3"},
        "point prompt one Row: Column: ", PointFactory.point(2, 3),
        "reads row then column", "prompts as expected");
    assertCLI((cli) => cli.inputPoint("point prompt two "), {"-1", "0", "-2", "4"},
        "point prompt two Row: Row: Column: Column: ", PointFactory.point(0, 4),
        "doesn't accept negative row or column", "prompts as expected");
}