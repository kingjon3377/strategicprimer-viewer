import ceylon.collection {
    ArrayList,
    MutableMap,
    HashMap,
    MutableList
}
import ceylon.test {
    assertEquals,
    test,
    assertThatException
}

import java.io {
    StringWriter,
    StringReader,
    OutputStreamWriter,
    IOException,
    BufferedReader,
    PrintWriter,
    JReader=Reader,
    JWriter=Writer,
    InputStreamReader,
    Closeable
}
import java.lang {
    NumberFormatException
}
import java.math {
    BigDecimal,
    MathContext
}
import java.text {
    NumberFormat
}

import lovelace.util.common {
    todo
}

import model.map {
    PlayerImpl,
    HasName,
    PointFactory,
    Point
}

import util {
    NullStream,
    IsNumeric
}

import view.util {
    SystemIn,
    SystemOut
}
"""An interface for the "CLI helper," which encapsulates input and output streams,
   allowing automated testing of CLIs and GUI wrappers around CLIs."""
shared interface ICLIHelper satisfies Closeable {
    shared alias ListAmendment<Element> => Element?(MutableList<Element>, ICLIHelper);
    "Ask the user to choose an item from the list, and if he does carry out an
     operation on it and then ask if he wants to do another."
    shared formal void loopOnList<out Element>(
            "The list."
            Element[] list,
            "How to ask the user to choose an item from the list."
            Integer(ICLIHelper) choice,
            "The prompt to use to ask if the user wants to continue."
            String prompt,
            "What to do with the chosen item in the list."
            Anything(Element, ICLIHelper) operation) given Element satisfies Object;
    "Ask the user to choose an item from the list, and if he does carry out an
     operation on it and then ask if he wants to do another."
    shared formal void loopOnMutableList<out Element>(
            "The list."
            MutableList<Element> list,
            "How to ask the user to choose an item from the list."
            Integer(ICLIHelper) choice,
            "The prompt to use to ask if the user wants to continue."
            String prompt,
            """What to do if the user chooses "add a new one"."""
            ListAmendment<Element> addition,
            "What to do with the chosen item in the list."
            Anything(Element, ICLIHelper) operation) given Element satisfies Object;
    "Have the user choose an item from a list. Returns the index."
    todo("Return Entry, as in Iterable.indexed?")
    shared formal Integer chooseFromList<Element>(
            "The list of items to choose from."
            Element[]|List<Element> items,
            "The description to give before printing the list."
            String description,
            "What to print if there are none."
            String none,
            "What to prompt the user with."
            String prompt,
            "Whether to automatically choose if there's only one choice."
            Boolean auto) given Element satisfies Object&HasName;
    "Have the user choose an item from a list."
    todo("Return Entry, as in Iterable.indexed?")
    shared formal Integer chooseStringFromList(
            "The list of items to choose from."
            String[] items,
            "The description to give before printing the list."
            String description,
            "What to print if there are none."
            String none,
            "What to prompt the user with."
            String prompt,
            "Whether to automatically choose if there's only one choice."
            Boolean auto);
    "Read from the input stream until a non-negative integer is entered, then return it."
    shared formal Integer inputNumber(
            "The prompt to prompt the user with."
            String prompt);
    "Read from the input stream repeatedly until a valid non-negative decimal number is
     entered, then return it."
    shared formal BigDecimal inputDecimal(
            "The prompt to prompt the user with."
            String prompt);
    "Read a line of input. It is trimmed of leading and trailing whitespace."
    shared formal String inputString(
            "The prompt to prompt the user with."
            String prompt);
    "Ask the user a yes-or-no question."
    shared formal Boolean inputBoolean(
            "The prompt to prompt the user with."
            String prompt);
    """Ask the user a yes-or-no question, allowing "yes to all" or "no to all" to
       forestall further similar questions."""
    shared formal Boolean inputBooleanInSeries(
            "The prompt to prompt the user with." String prompt,
            """The prompt (or other key) to compare to others to define "similar"
               questions."""
            String key = prompt);
    "Print a formatted string."
    deprecated todo("Remove given Ceylon's interpolation features")
    shared formal void printf(
            "The format string."
            String format,
            "Format arguments."
            Object* args);
    "Print the specified string, then a newline."
    shared formal void println(
            "The line to print"
            String line);
    "Print the specified string."
    shared formal void print(
            "The string to print."
            String text);
    "Get a [[Point]] from the user. This is a convenience wrapper around [[inputNumber]]."
    shared default Point inputPoint(
            "The prompt to use to prompt the user."
            String prompt) {
        print(prompt);
        return PointFactory.point(inputNumber("Row: "), inputNumber("Column: "));
    }
}
"A helper class to let help CLIs interact with the user, encapsulating input and output
 streams."
todo("Port to ceylon.io or equivalent")
class CLIHelper satisfies ICLIHelper {
    // We use NumberFormat rather than ceylon.lang.Integer.parse because
    // we want to allow the user to use commas.
    static NumberFormat numParser = NumberFormat.integerInstance;
    BufferedReader istream;
    PrintWriter ostream;
    "The current state of the yes-to-all/no-to-all possibility. Absent if not set,
     present if set, and the boolean value is what to return."
    MutableMap<String, Boolean> seriesState = HashMap<String, Boolean>();
    shared new (JReader inStream = InputStreamReader(SystemIn.sysIn),
            JWriter outStream = OutputStreamWriter(SystemOut.sysOut)) {
        istream = BufferedReader(inStream);
        ostream = PrintWriter(outStream);
    }
    "Ask the user a yes-or-no question."
    shared actual Boolean inputBoolean(String prompt) {
        while (true) {
            String input = inputString(prompt).lowercased;
            switch(input)
            case ("yes"|"true"|"y"|"t") { return true; }
            case ("no"|"false"|"n"|"f") { return false; }
            else {
                ostream.println("""Please enter "yes", "no", "true", or "false",
                                   or the first character of any of those.""");
            }
        }
    }
    "Ask the user to choose an item from the list, and if he does carry out an
     operation on it and then ask if he wants to do another."
    shared actual void loopOnList<Element>(Element[] list,
            Integer(ICLIHelper) choice, String prompt,
            Anything(Element, ICLIHelper) operation)
            given Element satisfies Object {
        MutableList<Element> temp = ArrayList { *list };
        variable Integer number = choice(this);
        while (number >= 0, number < temp.size) {
            assert (exists item = temp.delete(number));
            operation(item, this);
            if (temp.empty || !inputBoolean(prompt)) {
                break;
            }
            number = choice(this);
        }
    }
    "Ask the user to choose an item from the list, and if he does carry out an
     operation on it and then ask if he wants to do another."
    shared actual void loopOnMutableList<Element>(MutableList<Element> list,
            Integer(ICLIHelper) choice, String prompt, ListAmendment<Element> addition,
            Anything(Element, ICLIHelper) operation) given Element satisfies Object {
        variable Integer number = choice(this);
        while (number <= list.size) {
            Element item;
            if (exists temp = list.get(number)) {
                item = temp;
            } else {
                if (exists temp = addition(list, this)) {
                    item = temp;
                } else {
                    println("Select the new item at the next prompt.");
                    continue;
                }
            }
            operation(item, this);
            if (!inputBoolean(prompt)) {
                break;
            }
        }
    }
    "Print a list of things by name and number."
    void printList<out Element>({Element*} list, String(Element) func) {
        for (index->item in list.indexed) {
            ostream.println("``index``: ``func(item)``");
        }
        ostream.flush();
    }
    "Implementation of chooseFromList() and chooseStringFromList()."
    Integer chooseFromListImpl<Element>({Element*} items, String description,
            String none, String prompt, Boolean auto, String(Element) func)
            given Element satisfies Object {
        if (items.empty) {
            ostream.println(none);
            ostream.flush();
            return -1;
        }
        ostream.println(description);
        if (auto, !items.rest.first exists) {
            assert (exists first = items.first);
            ostream.println("Automatically choosing only item, ``func(first)``.");
            ostream.flush();
            return 0;
        } else {
            printList(items, func);
            return inputNumber(prompt);
        }
    }
    "Have the user choose an item from a list."
    shared actual Integer chooseFromList<out Element>(
            Element[]|List<Element> list, String description, String none,
            String prompt, Boolean auto) given Element satisfies HasName&Object {
        return chooseFromListImpl<Element>(list, description, none, prompt,
            auto, HasName.name);
    }
    "Read input from the input stream repeatedly until a non-negative integer is entered,
     then return it."
    shared actual Integer inputNumber(String prompt) {
        variable Integer retval = -1;
        while (retval < 0) {
            ostream.print(prompt);
            ostream.flush();
            if (exists input = istream.readLine()) {
                if (IsNumeric.isNumeric(input)) {
                    // In Java we have to wrap this in a try-catch block and
                    // handle ParseException; we don't here because IsNumeric
                    // works to prevent non-numeric input from getting here.
                    retval = numParser.parse(input).intValue();
                }
            } else {
                throw IOException("Null line of input");
            }
        }
        return retval;
    }
    "Read from the input stream repeatedly until a valid non-negative decimal number is
     entered, then return it."
    shared actual BigDecimal inputDecimal(String prompt) {
        variable BigDecimal retval = BigDecimal.zero.subtract(BigDecimal.one);
        while (retval.compareTo(BigDecimal.zero) < 0) {
            ostream.print(prompt);
            ostream.flush();
            if (exists input = istream.readLine()) {
                try {
                    retval = BigDecimal(input.trimmed, MathContext.unlimited);
                } catch (NumberFormatException except) {
                    ostream.println("Invalid number.");
                    log.debug("Invalid number", except);
                }
            } else {
                throw IOException("Null line of input");
            }
        }
        return retval;
    }
    "Read a line of input from the input stream. It is trimmed of leading and trailing
     whitespace."
    shared actual String inputString(String prompt) {
        ostream.print(prompt);
        ostream.flush();
        if (exists line = istream.readLine()) {
            return line.trimmed;
        } else {
            return "";
        }
    }
    "Ask the user a yes-or-no question, allowing yes-to-all or no-to-all to skip further
     questions."
    shared actual Boolean inputBooleanInSeries(String prompt, String key) {
        if (exists retval = seriesState.get(key)) {
            ostream.print(prompt);
            ostream.println((retval) then "yes" else "no");
            return retval;
        } else {
            while (true) {
                String input = inputString(prompt).lowercased;
                switch(input)
                case ("all"|"ya"|"ta"|"always") {
                    seriesState.put(key, true);
                    return true;
                }
                case ("none"|"na"|"fa"|"never") {
                    seriesState.put(key, false);
                    return false;
                }
                case ("yes"|"true"|"y"|"t") { return true; }
                case ("no"|"false"|"n"|"f") { return false; }
                else {
                    ostream.println(
                        """Please enter "yes", "no", "true", or "false", the first
                           character of any of those, or "fall", "none", "always", or
                           "never" to use the same answer for all further questions""");
                }
            }
        }
    }
    "Have the user choose an item from a list."
    shared actual Integer chooseStringFromList(String[] items, String description,
            String none, String prompt, Boolean auto) {
        return chooseFromListImpl<String>(items, description, none, prompt, auto,
                    (String x) => x);
    }
    "Print a formatted string."
    shared actual void printf(String format, Object* args) {
        ostream.printf(format, *args);
        ostream.flush();
    }
    "Print the specified string, then a newline."
    shared actual void println(String line) {
        ostream.println(line);
        ostream.flush();
    }
    "Print the specified string."
    shared actual void print(String text) {
        ostream.print(text);
        ostream.flush();
    }
    "Close I/O streams."
    shared actual void close() {
        istream.close();
        ostream.close();
    }
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
    assertCLI((cli) => cli.chooseFromList([PlayerImpl(1, "one"),
            PlayerImpl(2, "two")], "test desc", "none present", "prompt", false), {"0"},
        {"test desc", "0: one", "1: two", "prompt"}, 0,
        "chooseFromList chooses the one specified by the user",
        "chooseFromList prompted the user");
    assertCLI((cli) => cli.chooseFromList([PlayerImpl(1, "one"),
            PlayerImpl(2, "two")], "test desc", "none present", "prompt", true), {"1"},
        {"test desc", "0: one", "1: two", "prompt"}, 1,
        "chooseFromList chooses the one specified by the user",
        "chooseFromList prompted the user");
    assertCLI((cli) => cli.chooseFromList([PlayerImpl(1, "one")],
        "test desc", "none present", "prompt", true), {},
        {"test desc", "Automatically choosing only item, one", ""}, 0,
        "chooseFromList chooses only choice when this is specified",
        "chooseFromList automatically chose only choice");
    assertCLI((cli) => cli.chooseFromList([PlayerImpl(1, "one")],
            "test desc", "none present", "prompt", false), {"0"},
        {"test desc", "0: one", "prompt"}, 0,
        "chooseFromList doesn't always auto-choose only choice",
        "chooseFromList didn't automatically choose only choice");
}
"A second test of chooseFromList"
test
void testChooseFromListMore() {
    assertCLI((cli) => cli.chooseFromList([PlayerImpl(1, "one"),
            PlayerImpl(2, "two")], "test desc", "none present", "prompt ", false),
        {"-1", "0"}, {"test desc", "0: one", "1: two", "prompt prompt "}, 0,
        "chooseFromList prompts again when negative index given",
        "chooseFromList prompts again when negative index given");
    assertCLI((cli) => cli.chooseFromList([PlayerImpl(1, "one"),
            PlayerImpl(2, "two")], "test desc", "none present", "prompt", false), {"3"},
        {"test desc", "0: one", "1: two", "prompt"}, 3,
        "chooseFromList allows too-large choice",
        "chooseFromList allows too-large choice");
    assertCLI((cli) => cli.chooseFromList([PlayerImpl(1, "one"),
            PlayerImpl(2, "two")], "test desc", "none present", "prompt", true), {"0"},
        {"test desc", "0: one", "1: two", "prompt"}, 0,
        "chooseFromList asks even if 'auto' when multiple items",
        "chooseFromList prompted the user");
    assertCLI((cli) => cli.chooseFromList([], "test desc", "none present",
            "prompt", false), {}, {"none present", ""}, -1,
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
    assertCLI((cli) => cli.chooseStringFromList(["one", "two"],
        "test desc", "none present", "prompt", false), {"0"},
        {"test desc", "0: one", "1: two", "prompt"}, 0,
        "chooseStringFromList chooses the one specified by the user",
        "chooseStringFromList prompts the user");
    assertCLI((cli) => cli.chooseStringFromList(["one",
            "two", "three"], "test desc", "none present",
            "prompt two", true), {"1"},
        {"test desc", "0: one", "1: two", "2: three", "prompt two"}, 1,
        "chooseStringFromList chooses the one specified by the user",
        "chooseStringFromList prompts the user");
    assertCLI((cli) => cli.chooseStringFromList(["one"], "test desc", "none present",
        "prompt", true), {}, {"test desc", "Automatically choosing only item, one", ""},
        0, "chooseStringFromList automatically chooses only choice when told to",
        "chooseStringFromList automatically chose only choice");
    assertCLI((cli) => cli.chooseStringFromList(["one"], "test desc", "none present",
            "prompt", false), {"0"}, {"test desc", "0: one", "prompt"}, 0,
        "chooseStringFromList doesn't always auto-choose",
        "chooseStringFromList didn't automatically choose only choice");
}
"A second test of chooseStringFromList"
test
void testChooseStringFromListMore() {
    assertCLI((cli) => cli.chooseStringFromList(["zero", "one", "two"],
            "test desc", "none present", "prompt", true), {"1"},
        {"test desc", "0: zero", "1: one", "2: two", "prompt"}, 1,
        "chooseStringFromList doesn't auto-choose when more than one item",
        "chooseStringFromList doesn't auto-choose when more than one item");
    assertCLI((cli) => cli.chooseStringFromList(["one", "two"],
            "test desc", "none present", "prompt", false),
        {"-1", "0"}, {"test desc", "0: one", "1: two", "promptprompt"}, 0,
        "chooseStringFromList prompts again when negative index given",
        "chooseStringFromList prompts again when negative index given");
    assertCLI((cli) => cli.chooseStringFromList(["one",
            "two"], "test desc", "none present", "prompt", false), {"3"},
        {"test desc", "0: one", "1: two", "prompt"}, 3,
        "chooseStringFromList allows too-large choice",
        "chooseStringFromList allows too-large choice");
    assertCLI((cli) => cli.chooseStringFromList([], "test desc", "none present",
            "prompt", false), {}, {"none present", ""}, -1,
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