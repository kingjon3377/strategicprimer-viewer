import ceylon.test {
    test,
    assertEquals
}
import ceylon.collection {
    ArrayList
}
import strategicprimer.model.map {
    PlayerImpl,
    pointFactory
}
import ceylon.math.decimal {
    decimalNumber
}
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
	StringBuilder ostream = StringBuilder();
	ICLIHelper cli = CLIHelper(ArrayList { *input }.accept, ostream.append);
	assertEquals(method(cli), expectedResult, resultMessage);
	assertEquals(ostream.string, expectedOutputReal, outputMessage);
}

"Test chooseFromList()."
test
void testChooseFromList() {
	assertCLI((cli) => cli.chooseFromList([PlayerImpl(1, "one"),
		PlayerImpl(2, "two")], "test desc", "none present", "prompt", false), {"0"},
		{"test desc", "0: one", "1: two", "prompt "}, 0->PlayerImpl(1, "one"),
		"chooseFromList chooses the one specified by the user",
		"chooseFromList prompted the user");
	assertCLI((cli) => cli.chooseFromList([PlayerImpl(1, "one"),
		PlayerImpl(2, "two")], "test desc", "none present", "prompt", true), {"1"},
		{"test desc", "0: one", "1: two", "prompt "}, 1->PlayerImpl(2, "two"),
		"chooseFromList chooses the one specified by the user",
		"chooseFromList prompted the user");
	assertCLI((cli) => cli.chooseFromList([PlayerImpl(1, "one")],
		"test desc", "none present", "prompt", true), {},
		{"test desc", "Automatically choosing only item, one.", ""},
		0->PlayerImpl(1, "one"),
		"chooseFromList chooses only choice when this is specified",
		"chooseFromList automatically chose only choice");
	assertCLI((cli) => cli.chooseFromList([PlayerImpl(1, "one")],
		"test desc", "none present", "prompt", false), {"0"},
		{"test desc", "0: one", "prompt "}, 0->PlayerImpl(1, "one"),
		"chooseFromList doesn't always auto-choose only choice",
		"chooseFromList didn't automatically choose only choice");
}
"A second test of chooseFromList"
test
void testChooseFromListMore() {
	assertCLI((cli) => cli.chooseFromList([PlayerImpl(1, "one"),
		PlayerImpl(2, "two")], "test desc", "none present", "prompt ", false),
		{"-1", "0"}, {"test desc", "0: one", "1: two", "prompt prompt "},
		0->PlayerImpl(1, "one"), "chooseFromList prompts again when negative index given",
		"chooseFromList prompts again when negative index given");
	assertCLI((cli) => cli.chooseFromList([PlayerImpl(1, "one"),
		PlayerImpl(2, "two")], "test desc", "none present", "prompt", false), {"3"},
		{"test desc", "0: one", "1: two", "prompt "}, 3->null,
		"chooseFromList allows too-large choice",
		"chooseFromList allows too-large choice");
	assertCLI((cli) => cli.chooseFromList([PlayerImpl(1, "one"),
		PlayerImpl(2, "two")], "test desc", "none present", "prompt", true), {"0"},
		{"test desc", "0: one", "1: two", "prompt "}, 0->PlayerImpl(1, "one"),
		"chooseFromList asks even if 'auto' when multiple items",
		"chooseFromList prompted the user");
	assertCLI((cli) => cli.chooseFromList([], "test desc", "none present",
		"prompt", false), {}, {"none present", ""}, -1->null,
		"chooseFromList handles no-item case", "chooseFromList didn't prompt the user");
}
"Test inputNumber"
test
void testInputNumber() {
	assertCLI((cli) => cli.inputNumber("test prompt"), {"2"}, "test prompt ", 2,
		"inputNumber works", "inputNumber uses given prompt");
	assertCLI((cli) => cli.inputNumber("test prompt two"), {"8"}, "test prompt two ", 8,
		"inputNumber works", "inputNumber uses given prompt");
	assertCLI((cli) => cli.inputNumber("test prompt three "), {"-1", "0"},
		"test prompt three test prompt three ", 0,
		"inputNumber asks again on negative input",
		"inputNumber asks again on negative input");
	assertCLI((cli) => cli.inputNumber("test prompt four "), {"not-number", "9"},
		"test prompt four test prompt four ", 9,
		"inputNumber asks again on non-numeric input",
		"inputNumber asks again on non-numeric input");
	// https://github.com/ceylon/ceylon/issues/5448
//    try (cli = CLIHelper(ArrayList { "" }.accept, noop)) {
//       assertThatException(() => cli.inputNumber("test prompt")).hasType(`IOException`);
//    }
}
"Test inputDecimal"
test
void testInputDecimal() {
	assertCLI((cli) => cli.inputDecimal("test prompt"), {"10"}, "test prompt ",
		decimalNumber(10), "inputDecimal works with integers",
		"inputDecimal uses given prompt");
	assertCLI((cli) => cli.inputDecimal("test prompt two"), {"2.5"}, "test prompt two ",
		decimalNumber(5) / decimalNumber(2), "inputDecimal works with decimals",
		"inputDecimal uses given prompt");
	assertCLI((cli) => cli.inputDecimal("test prompt three "), {"-2.5", "0.5"},
		"test prompt three test prompt three ", decimalNumber(1) / decimalNumber(2),
		"inputDecimal asks again on negative input",
		"inputDecimal asks again on negative input");
	assertCLI((cli) => cli.inputDecimal("test prompt four "), {"non-number", ".1"},
		{"test prompt four Invalid number.", "test prompt four "},
		decimalNumber(1) / decimalNumber(10),
		"inputDecimal asks again on non-numerc input",
		"inputDecimal asks again on non-numeric input");
}
"Test for inputString()"
test
void testInputString() {
	assertCLI((cli) => cli.inputString("string prompt"), {"first"}, "string prompt ",
		"first", "inputString returns the entered string", "inputString displays prompt");
	assertCLI((cli) => cli.inputString("second prompt"), {"second"}, "second prompt ",
		"second", "inputString returns the entered string",
		"inputString displays prompt");
	assertCLI((cli) => cli.inputString("third prompt"), {}, "third prompt ", "",
		"inputString returns empty on EOF", "inputString displays prompt");
}
"Test for inputBoolean()"
test
void testInputBoolean() {
	for (arg in {"yes", "true", "y", "t"}) {
		assertCLI((cli) => cli.inputBoolean("bool prompt"), {arg}, "bool prompt ", true,
			"inputBoolean returns true on '``arg``", "inputBoolean displays prompt");
	}
	for (arg in {"no", "false", "n", "f"}) {
		assertCLI((cli) => cli.inputBoolean("prompt two"), {arg}, "prompt two ", false,
			"inputBoolean returns false on ``arg``", "inputBoolean displays prompt");
	}
	assertCLI((cli) => cli.inputBoolean("prompt three "), {"yoo-hoo", "no"},
		{"""prompt three Please enter "yes", "no", "true", or "false",""",
			"or the first character of any of those.", "prompt three "}, false,
		"inputBoolean rejects other input",
		"inputBoolean gives message on invalid input");
}

"Test the input-boolean-with-skipping functionality."
test
void testInputBooleanInSeries() {
	for (arg in {"yes", "true", "y", "t"}) {
		assertCLI((cli) => cli.inputBooleanInSeries("bool prompt"), {arg}, "bool prompt ",
			true, "inputBooleanInSeries returns true on '``arg``",
			"inputBooleanInSeries displays prompt");
	}
	for (arg in {"no", "false", "n", "f"}) {
		assertCLI((cli) => cli.inputBooleanInSeries("prompt two"), {arg}, "prompt two ",
			false, "inputBooleanInSeries returns false on ``arg``",
			"inputBooleanInSeries displays prompt");
	}
	assertCLI((cli) => cli.inputBooleanInSeries("prompt three "), {"nothing", "true"},
		{"""prompt three Please enter "yes", "no", "true", or "false", the first""",
			"""character of any of those, or "all", "none", "always", or""",
			""""never" to use the same answer for all further questions""",
			"prompt three "}, true,
		"inputBoolean rejects other input",
		"inputBoolean gives message on invalid input");
	StringBuilder ostream = StringBuilder();
	variable ICLIHelper cli = CLIHelper(ArrayList { "all" }.accept, ostream.append);
	assertEquals(cli.inputBooleanInSeries("prompt four "), true,
		"inputBooleanInSeries allows yes-to-all");
	assertEquals(cli.inputBooleanInSeries("prompt four "), true,
		"inputBooleanInSeries honors yes-to-all when prompt is the same");
	assertEquals(ostream.string, """prompt four prompt four yes
	                                """,
		"inputBooleanInSeries shows automatic yes");
	//  https://github.com/ceylon/ceylon/issues/5448
//        assertThatException(() => cli.inputBooleanInSeries("other prompt"))
//            .hasType(`IOException`);
	ostream.clear();
	cli = CLIHelper(ArrayList { """none""" }.accept, ostream.append);
	assertEquals(cli.inputBooleanInSeries("prompt five "), false,
		"inputBooleanInSeries allows no-to-all");
	assertEquals(cli.inputBooleanInSeries("prompt five "), false,
		"inputBooleanInSeries honors no-to-all when prompt is the same");
	assertEquals(ostream.string, "prompt five prompt five no\n",
		"inputBooleanInSeries shows automatic no");
	// https://github.com/ceylon/ceylon/issues/5448
//      assertThatException(() => cli.inputBooleanInSeries("other prompt"))
//            .hasType(`IOException`);
	ostream.clear();
	cli = CLIHelper(ArrayList  { "always" }.accept, ostream.append);
	assertEquals(cli.inputBooleanInSeries("prompt six ", "key"), true,
		"inputBooleanInSeries allows yes-to-all");
	assertEquals(cli.inputBooleanInSeries("prompt seven ", "key"), true,
		"inputBooleanInSeries honors yes-to-all if prompt differs, same key");
	assertEquals(ostream.string, "prompt six prompt seven yes\n",
		"inputBooleanInSeries shows automatic yes");
	ostream.clear();
	cli = CLIHelper(ArrayList { """never""" }.accept, ostream.append);
	assertEquals(cli.inputBooleanInSeries("prompt eight ", "secondKey"), false,
		"inputBooleanInSeries allows no-to-all");
	assertEquals(cli.inputBooleanInSeries("prompt nine ", "secondKey"), false,
		"inputBooleanInSeries honors no-to-all if prompt differs, same key");
	assertEquals(ostream.string, "prompt eight prompt nine no\n",
		"inputBooleanInSeries shows automatic no");
	ostream.clear();
	cli = CLIHelper(ArrayList { "all", "none" }.accept, ostream.append);
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
void testChooseStringFromList() {
	assertCLI((cli) => cli.chooseStringFromList(["one", "two"],
		"test desc", "none present", "prompt", false), {"0"},
		{"test desc", "0: one", "1: two", "prompt "}, 0->"one",
		"chooseStringFromList chooses the one specified by the user",
		"chooseStringFromList prompts the user");
	assertCLI((cli) => cli.chooseStringFromList(["one",
		"two", "three"], "test desc", "none present",
		"prompt two", true), {"1"},
		{"test desc", "0: one", "1: two", "2: three", "prompt two "}, 1->"two",
		"chooseStringFromList chooses the one specified by the user",
		"chooseStringFromList prompts the user");
	assertCLI((cli) => cli.chooseStringFromList(["one"], "test desc", "none present",
		"prompt", true), {}, {"test desc", "Automatically choosing only item, one.", ""},
		0->"one", "chooseStringFromList automatically chooses only choice when told to",
		"chooseStringFromList automatically chose only choice");
	assertCLI((cli) => cli.chooseStringFromList(["one"], "test desc", "none present",
		"prompt", false), {"0"}, {"test desc", "0: one", "prompt "}, 0->"one",
		"chooseStringFromList doesn't always auto-choose",
		"chooseStringFromList didn't automatically choose only choice");
}
"A second test of chooseStringFromList"
test
void testChooseStringFromListMore() {
	assertCLI((cli) => cli.chooseStringFromList(["zero", "one", "two"],
		"test desc", "none present", "prompt", true), {"1"},
		{"test desc", "0: zero", "1: one", "2: two", "prompt "}, 1->"one",
		"chooseStringFromList doesn't auto-choose when more than one item",
		"chooseStringFromList doesn't auto-choose when more than one item");
	assertCLI((cli) => cli.chooseStringFromList(["one", "two"],
		"test desc", "none present", "prompt", false),
		{"-1", "0"}, {"test desc", "0: one", "1: two", "prompt prompt "}, 0->"one",
		"chooseStringFromList prompts again when negative index given",
		"chooseStringFromList prompts again when negative index given");
	assertCLI((cli) => cli.chooseStringFromList(["one",
		"two"], "test desc", "none present", "prompt", false), {"3"},
		{"test desc", "0: one", "1: two", "prompt "}, 3->null,
		"chooseStringFromList allows too-large choice",
		"chooseStringFromList allows too-large choice");
	assertCLI((cli) => cli.chooseStringFromList([], "test desc", "none present",
		"prompt", false), {}, {"none present", ""}, -1->null,
		"chooseStringFromList handles empty list",
		"chooseStringFromList handles empty list");
}
"Test print() and friends"
test
void testPrinting() {
	void assertHelper(Anything(ICLIHelper) method, String expected, String message) {
		StringBuilder ostream = StringBuilder();
		method(CLIHelper(ArrayList<String>().accept, ostream.append));
		assertEquals(ostream.string, expected, message);
	}
	assertHelper((cli) => cli.print("test string"), "test string",
		"print() prints string");
	assertHelper((cli) => cli.println("test two"), "test two\n", "println() adds newline");
}

"Test inputPoint()"
test
void testInputPoint() {
	assertCLI((cli) => cli.inputPoint("point prompt one "), {"2", "3"},
		"point prompt one Row: Column: ", pointFactory(2, 3),
		"reads row then column", "prompts as expected");
	assertCLI((cli) => cli.inputPoint("point prompt two "), {"-1", "0", "-2", "4"},
		"point prompt two Row: Row: Column: Column: ", pointFactory(0, 4),
		"doesn't accept negative row or column", "prompts as expected");
}
