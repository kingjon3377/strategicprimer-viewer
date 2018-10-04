import ceylon.test {
    test,
    assertEquals,
    parameters
}
import ceylon.collection {
    LinkedList
}
import strategicprimer.model.common.map {
    PlayerImpl,
    Player,
    Point
}
import ceylon.decimal {
    decimalNumber
}
import ceylon.language.meta.model {
    Method
}
String[] truePossibilities = ["yes", "true", "y", "t"];
String[] falsePossibilities = ["no", "false", "n", "f"];
object cliTests {
    "A helper method to condense tests."
    void assertCLI<out T, in Arguments>(
            "The method under test."
            Method<ICLIHelper, T, Arguments> method,
            "The arguments to pass to the method."
            Arguments arguments,
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
            String outputMessage = "CLIHelper output was as expected")
            given Arguments satisfies Anything[] {
        String expectedOutputReal;
        if (is String expectedOutput) {
            expectedOutputReal = expectedOutput;
        } else if (expectedOutput.empty) {
            expectedOutputReal = "";
        } else {
            expectedOutputReal = operatingSystem.newline.join(expectedOutput);
        }
        StringBuilder ostream = StringBuilder();
        ICLIHelper cli = CLIHelper(LinkedList(input).accept, ostream.append);
        assertEquals(method(cli)(*arguments), expectedResult, resultMessage);
        assertEquals(ostream.string, expectedOutputReal, outputMessage);
    }

    "Test chooseFromList()."
    test
    shared void testChooseFromList() {
        assertCLI(`ICLIHelper.chooseFromList<Player>`, [[PlayerImpl(1, "one"),
            PlayerImpl(2, "two")], "test desc", "none present", "prompt", false],
            Singleton("0"), ["test desc", "0: one", "1: two", "prompt "],
            0->PlayerImpl(1, "one"),
            "chooseFromList chooses the one specified by the user",
            "chooseFromList prompted the user");
        assertCLI(`ICLIHelper.chooseFromList<Player>`, [[PlayerImpl(1, "one"),
            PlayerImpl(2, "two")], "test desc", "none present", "prompt", true],
            Singleton("1"), ["test desc", "0: one", "1: two", "prompt "],
            1->PlayerImpl(2, "two"),
            "chooseFromList chooses the one specified by the user",
            "chooseFromList prompted the user");
        assertCLI(`ICLIHelper.chooseFromList<Player>`, [[PlayerImpl(1, "one")],
            "test desc", "none present", "prompt", true], [],
            ["test desc", "Automatically choosing only item, one.", ""],
            0->PlayerImpl(1, "one"),
            "chooseFromList chooses only choice when this is specified",
            "chooseFromList automatically chose only choice");
        assertCLI(`ICLIHelper.chooseFromList<Player>`, [[PlayerImpl(1, "one")],
            "test desc", "none present", "prompt", false], Singleton("0"),
            ["test desc", "0: one", "prompt "], 0->PlayerImpl(1, "one"),
            "chooseFromList doesn't always auto-choose only choice",
            "chooseFromList didn't automatically choose only choice");
    }
    "A second test of chooseFromList"
    test
    shared void testChooseFromListMore() {
        assertCLI(`ICLIHelper.chooseFromList<Player>`, [[PlayerImpl(1, "one"),
            PlayerImpl(2, "two")], "test desc", "none present", "prompt ", false],
            ["-1", "0"], ["test desc", "0: one", "1: two", "prompt prompt "],
            0->PlayerImpl(1, "one"),
            "chooseFromList prompts again when negative index given",
            "chooseFromList prompts again when negative index given");
        assertCLI(`ICLIHelper.chooseFromList<Player>`, [[PlayerImpl(1, "one"),
            PlayerImpl(2, "two")], "test desc", "none present", "prompt", false],
            Singleton("3"), ["test desc", "0: one", "1: two", "prompt "], 3->null,
            "chooseFromList allows too-large choice",
            "chooseFromList allows too-large choice");
        assertCLI(`ICLIHelper.chooseFromList<Player>`, [[PlayerImpl(1, "one"),
            PlayerImpl(2, "two")], "test desc", "none present", "prompt", true],
            Singleton("0"), ["test desc", "0: one", "1: two", "prompt "],
            0->PlayerImpl(1, "one"),
            "chooseFromList asks even if 'auto' when multiple items",
            "chooseFromList prompted the user");
        assertCLI(`ICLIHelper.chooseFromList<Player>`, [[], "test desc", "none present",
            "prompt", false], [], ["none present", ""], -1->null,
            "chooseFromList handles no-item case",
            "chooseFromList didn't prompt the user");
    }
    "Test inputNumber"
    test
    shared void testInputNumber() {
        assertCLI(`ICLIHelper.inputNumber`, ["test prompt"], Singleton("2"),
            "test prompt ", 2, "inputNumber works", "inputNumber uses given prompt");
        assertCLI(`ICLIHelper.inputNumber`, ["test prompt two"], Singleton("8"),
            "test prompt two ", 8, "inputNumber works", "inputNumber uses given prompt");
        assertCLI(`ICLIHelper.inputNumber`, ["test prompt three "], ["-1", "0"],
            "test prompt three test prompt three ", 0,
            "inputNumber asks again on negative input",
            "inputNumber asks again on negative input");
        assertCLI(`ICLIHelper.inputNumber`, ["test prompt four "], ["not-number", "9"],
            "test prompt four test prompt four ", 9,
            "inputNumber asks again on non-numeric input",
            "inputNumber asks again on non-numeric input");
        assertCLI(`ICLIHelper.inputNumber`, ["test prompt five "], [],
            "test prompt five ", null, "inputNumber produces null on EOF",
            "inputNumber doesn't ask again on EOF");
    }
    "Test inputDecimal"
    test
    shared void testInputDecimal() {
        assertCLI(`ICLIHelper.inputDecimal`, ["test prompt"], Singleton("10"),
            "test prompt ", decimalNumber(10), "inputDecimal works with integers",
            "inputDecimal uses given prompt");
        assertCLI(`ICLIHelper.inputDecimal`, ["test prompt two"], Singleton("2.5"),
            "test prompt two ", decimalNumber(5) / decimalNumber(2),
            "inputDecimal works with decimals", "inputDecimal uses given prompt");
        assertCLI(`ICLIHelper.inputDecimal`, ["test prompt three "], ["-2.5", "0.5"],
            "test prompt three test prompt three ", decimalNumber(1) / decimalNumber(2),
            "inputDecimal asks again on negative input",
            "inputDecimal asks again on negative input");
        assertCLI(`ICLIHelper.inputDecimal`, ["test prompt four "], ["non-number", ".1"],
            ["test prompt four Invalid number.", "test prompt four "],
            decimalNumber(1) / decimalNumber(10),
            "inputDecimal asks again on non-numerc input",
            "inputDecimal asks again on non-numeric input");
        assertCLI(`ICLIHelper.inputDecimal`, ["test prompt five "], [],
            ["test prompt five "], null,
            "inputDecimal produces null on EOF",
            "inputDecimal doesn't prompt again on EOF");
    }
    "Test for inputString()"
    test
    shared void testInputString() {
        assertCLI(`ICLIHelper.inputString`, ["string prompt"], Singleton("first"),
            "string prompt ", "first", "inputString returns the entered string",
            "inputString displays prompt");
        assertCLI(`ICLIHelper.inputString`, ["second prompt"], Singleton("second"),
            "second prompt ", "second", "inputString returns the entered string",
            "inputString displays prompt");
        assertCLI(`ICLIHelper.inputString`, ["third prompt"], [], "third prompt ", "",
            "inputString returns empty on EOF", "inputString displays prompt");
    }
    parameters(`value truePossibilities`)
    test
    shared void testInputBooleanSimpleTrue(String arg) {
        assertCLI(`ICLIHelper.inputBoolean`, ["bool prompt"], Singleton(arg),
            "bool prompt ", true, "inputBoolean returns true on ``arg``",
            "inputBoolean displays prompt");
    }
    parameters(`value falsePossibilities`)
    test
    shared void testInputBooleanSimpleFalse(String arg) {
        assertCLI(`ICLIHelper.inputBoolean`, ["prompt two"], Singleton(arg),
            "prompt two ", false, "inputBoolean returns false on ``arg``",
            "inputBoolean displays prompt");
    }
    "Test for inputBoolean()"
    test
    shared void testInputBooleanInvalidInput() {
        assertCLI(`ICLIHelper.inputBoolean`, ["prompt three "], ["yoo-hoo", "no"],
            ["""prompt three Please enter "yes", "no", "true", or "false",""",
                "or the first character of any of those.", "prompt three "], false,
            "inputBoolean rejects other input",
            "inputBoolean gives message on invalid input");
    }
    parameters(`value truePossibilities`)
    test
    shared void testInputBooleanInSeriesSimpleTrue(String arg) {
        assertCLI(`ICLIHelper.inputBooleanInSeries<Nothing>`, ["bool prompt"], {arg},
            "bool prompt ", true, "inputBooleanInSeries returns true on '``arg``",
            "inputBooleanInSeries displays prompt");
    }
    parameters(`value falsePossibilities`)
    test
    shared void testInputBooleanInSeriesSimpleFalse(String arg) {
        assertCLI(`ICLIHelper.inputBooleanInSeries<Nothing>`, ["prompt two"], {arg},
            "prompt two ", false, "inputBooleanInSeries returns false on ``arg``",
            "inputBooleanInSeries displays prompt");
    }
    "Test the input-boolean-with-skipping functionality."
    test
    shared void testInputBooleanInSeries() {
        assertCLI(`ICLIHelper.inputBooleanInSeries<Nothing>`, ["prompt three "],
            ["nothing", "true"],
            ["""prompt three Please enter "yes", "no", "true", or "false", the first""",
                """character of any of those, or "all", "none", "always", or""",
                """"never" to use the same answer for all further questions""",
                "prompt three "], true,
            "inputBoolean rejects other input",
            "inputBoolean gives message on invalid input");
        StringBuilder ostream = StringBuilder();
        variable ICLIHelper cli = CLIHelper(LinkedList(Singleton("all")).accept,
            ostream.append);
        assertEquals(cli.inputBooleanInSeries("prompt four "), true,
            "inputBooleanInSeries allows yes-to-all");
        assertEquals(cli.inputBooleanInSeries("prompt four "), true,
            "inputBooleanInSeries honors yes-to-all when prompt is the same");
        assertEquals(ostream.string, """prompt four prompt four yes
                                        """,
            "inputBooleanInSeries shows automatic yes");
        //  https://github.com/eclipse/ceylon/issues/5448
    //        assertThatException(defer(cli.inputBooleanInSeries, ["other prompt"]))
    //            .hasType(`IOException`);
        ostream.clear();
        cli = CLIHelper(LinkedList(Singleton("""none""")).accept, ostream.append);
        assertEquals(cli.inputBooleanInSeries("prompt five "), false,
            "inputBooleanInSeries allows no-to-all");
        assertEquals(cli.inputBooleanInSeries("prompt five "), false,
            "inputBooleanInSeries honors no-to-all when prompt is the same");
        assertEquals(ostream.string,
            "prompt five prompt five no``operatingSystem.newline``",
            "inputBooleanInSeries shows automatic no");
        // TODO: uncomment once eclipse/ceylon#5448 fixed
    //      assertThatException(defer(cli.inputBooleanInSeries, ["other prompt"]))
    //            .hasType(`IOException`);
        ostream.clear();
        cli = CLIHelper(LinkedList(Singleton("always")).accept, ostream.append);
        assertEquals(cli.inputBooleanInSeries("prompt six ", "key"), true,
            "inputBooleanInSeries allows yes-to-all");
        assertEquals(cli.inputBooleanInSeries("prompt seven ", "key"), true,
            "inputBooleanInSeries honors yes-to-all if prompt differs, same key");
        assertEquals(ostream.string,
            "prompt six prompt seven yes``operatingSystem.newline``",
            "inputBooleanInSeries shows automatic yes");
        ostream.clear();
        cli = CLIHelper(LinkedList(Singleton("""never""")).accept, ostream.append);
        assertEquals(cli.inputBooleanInSeries("prompt eight ", "secondKey"), false,
            "inputBooleanInSeries allows no-to-all");
        assertEquals(cli.inputBooleanInSeries("prompt nine ", "secondKey"), false,
            "inputBooleanInSeries honors no-to-all if prompt differs, same key");
        assertEquals(ostream.string,
            "prompt eight prompt nine no``operatingSystem.newline``",
            "inputBooleanInSeries shows automatic no");
        ostream.clear();
        cli = CLIHelper(LinkedList(["all", "none"]).accept, ostream.append);
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
                                        """, "inputBooleanInSeries shows prompts");
    }
    "Test of chooseStringFromList()"
    test
    shared void testChooseStringFromList() {
        assertCLI(`ICLIHelper.chooseStringFromList`, [["one", "two"],
            "test desc", "none present", "prompt", false], Singleton("0"),
            ["test desc", "0: one", "1: two", "prompt "], 0->"one",
            "chooseStringFromList chooses the one specified by the user",
            "chooseStringFromList prompts the user");
        assertCLI(`ICLIHelper.chooseStringFromList`, [["one",
            "two", "three"], "test desc", "none present",
            "prompt two", true], Singleton("1"),
            ["test desc", "0: one", "1: two", "2: three", "prompt two "], 1->"two",
            "chooseStringFromList chooses the one specified by the user",
            "chooseStringFromList prompts the user");
        assertCLI(`ICLIHelper.chooseStringFromList`, [["one"], "test desc",
            "none present", "prompt", true], [], ["test desc",
            "Automatically choosing only item, one.", ""], 0->"one",
            "chooseStringFromList automatically chooses only choice when told to",
            "chooseStringFromList automatically chose only choice");
        assertCLI(`ICLIHelper.chooseStringFromList`, [["one"], "test desc",
            "none present", "prompt", false], Singleton("0"),
            ["test desc", "0: one", "prompt "], 0->"one",
            "chooseStringFromList doesn't always auto-choose",
            "chooseStringFromList didn't automatically choose only choice");
    }
    "A second test of chooseStringFromList"
    test
    shared void testChooseStringFromListMore() {
        assertCLI(`ICLIHelper.chooseStringFromList`, [["zero", "one", "two"],
            "test desc", "none present", "prompt", true], Singleton("1"),
            ["test desc", "0: zero", "1: one", "2: two", "prompt "], 1->"one",
            "chooseStringFromList doesn't auto-choose when more than one item",
            "chooseStringFromList doesn't auto-choose when more than one item");
        assertCLI(`ICLIHelper.chooseStringFromList`, [["one", "two"],
            "test desc", "none present", "prompt", false],
            ["-1", "0"], ["test desc", "0: one", "1: two", "prompt prompt "], 0->"one",
            "chooseStringFromList prompts again when negative index given",
            "chooseStringFromList prompts again when negative index given");
        assertCLI(`ICLIHelper.chooseStringFromList`, [["one",
            "two"], "test desc", "none present", "prompt", false], Singleton("3"),
            ["test desc", "0: one", "1: two", "prompt "], 3->null,
            "chooseStringFromList allows too-large choice",
            "chooseStringFromList allows too-large choice");
        assertCLI(`ICLIHelper.chooseStringFromList`, [[], "test desc", "none present",
            "prompt", false], [], ["none present", ""], -1->null,
            "chooseStringFromList handles empty list",
            "chooseStringFromList handles empty list");
    }
    void assertPrintingOutput(Method<ICLIHelper, Anything, String[1]> method,
            String argument, String expected, String message) {
        StringBuilder ostream = StringBuilder();
        method(CLIHelper(LinkedList<String>().accept, ostream.append))(argument);
        assertEquals(ostream.string, expected, message);
    }
    "Test print() and friends"
    test
    shared void testPrinting() {
        assertPrintingOutput(`ICLIHelper.print`, "test string", "test string",
            "print() prints string");
        assertPrintingOutput(`ICLIHelper.println`, "test two",
            "test two``operatingSystem.newline``", "println() adds newline");
    }

    "Test inputPoint()"
    test
    shared void testInputPoint() {
        assertCLI(`ICLIHelper.inputPoint`, ["point prompt one "], ["2", "3"],
            "point prompt one Row: Column: ", Point(2, 3),
            "reads row then column", "prompts as expected");
        assertCLI(`ICLIHelper.inputPoint`, ["point prompt two "], ["-1", "0", "-2", "4"],
            "point prompt two Row: Row: Column: Column: ", Point(0, 4),
            "doesn't accept negative row or column", "prompts as expected");
    }
}