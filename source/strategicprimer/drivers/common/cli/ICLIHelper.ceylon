import strategicprimer.model.common.map {
    HasName,
    Point
}
import ceylon.decimal {
    Decimal
}

"""An interface for the "CLI helper," which encapsulates input and output streams,
   allowing automated testing of command-line apps and graphical wrappers around them."""
// TODO: Methods beyond inputNumber/inputDecimal should abort (return null) on EOF.
shared interface ICLIHelper {
    "Have the user choose an item from a list. Returns the index."
    shared formal Integer->Element? chooseFromList<Element>(
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
    shared formal Integer->String? chooseStringFromList(
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

    "Read from the input stream until a non-negative integer is entered, then return it.
     Returns null on EOF."
    shared formal Integer? inputNumber(
            "The prompt to prompt the user with."
            String prompt);

    "Read from the input stream repeatedly until a valid non-negative decimal number is
     entered, then return it. Returns null on EOF."
    shared formal Decimal? inputDecimal(
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
    shared formal Boolean|Absent inputBooleanInSeries<Absent=Nothing>(
            "The prompt to prompt the user with." String prompt,
            """The prompt (or other key) to compare to others to define "similar"
               questions."""
            String key = prompt,
            "A function to produce an [[Absent]] value to return if an input should
             short-circuit the loop."
            <Absent|Boolean?>(String) quitResultFactory = (String str) => null);

    "Print the specified string, then a newline."
    shared formal void println(
            "The line to print"
            String line = "");

    "Print the specified string."
    shared formal void print(
            "The string to print."
            String text);

    "Get a [[Point]] from the user. This is a convenience wrapper around [[inputNumber]].
     On EOF, returns -1 for any remaining coordinates."
    shared default Point inputPoint(
            "The prompt to use to prompt the user."
            String prompt) {
        print(prompt);
        return Point(inputNumber("Row: ") else -1, inputNumber("Column: ") else -1);
    }
}
