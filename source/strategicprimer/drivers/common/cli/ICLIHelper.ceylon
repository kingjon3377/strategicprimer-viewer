import strategicprimer.model.common.map {
    HasName,
    Point
}
import ceylon.decimal {
    Decimal
}

"""An interface for the "CLI helper," which encapsulates input and output streams,
   allowing automated testing of command-line apps and graphical wrappers around them."""
shared interface ICLIHelper {
    "Have the user choose an item from a list. Returns the index and the item, if any.
     On EOF, returns an index of -2."
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

    "Have the user choose an item from a list. Returns the index and the item, if any. On
     EOF, returns an index of -2."
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

    "Read a line of input. It is trimmed of leading and trailing whitespace. Returns
     [[null]] on EOF."
    shared formal String? inputString(
            "The prompt to prompt the user with."
            String prompt);

    "Read a multiline string from the user. It is trimmed of leading and trailing
     whitespace, except that if it ends with multiple newlines two of them will be
     retained. Returns [[null]] on EOF."
    shared formal String? inputMultilineString(
            "The prompt to prompt the user with."
            String prompt);

    """Returns [[null]] if [[input]] is "quit" and [[false]] otherwise."""
    shared default Boolean? defaultQuitHandler(String input) {
        if ("quit" == input) {
            return null;
        } else {
            return false;
        }
    }

    "Ask the user a yes-or-no question. Returns [[null]] on EOF or if [[quitResultFactory]]
     returns [[null]]."
    shared formal Boolean? inputBoolean(
            "The prompt to prompt the user with."
            String prompt,
            "A function to produce [[null]] (to return) if an input should
             short-circuit the loop. By default just returns [[false]] to keep looping."
            Boolean?(String) quitResultFactory = defaultQuitHandler);

    """Ask the user a yes-or-no question, allowing "yes to all" or "no to all" to
       forestall further similar questions. Returns [[null]] on EOF or if
       [[quitResultFactory]] returns [[null]]."""
    shared formal Boolean? inputBooleanInSeries(
            "The prompt to prompt the user with." String prompt,
            """The prompt (or other key) to compare to others to define "similar"
               questions."""
            String key = prompt,
            "A function to produce [[null]] (to return) if an input should
             short-circuit the loop. By default just returns [[false]] to keep looping."
            Boolean?(String) quitResultFactory = defaultQuitHandler);

    "Print the specified string, then a newline."
    shared formal void println(
            "The line to print"
            String line = "");

    "Print the specified strings."
    shared formal void print(
            "The strings to print."
            String+ text);

    "Get a [[Point]] from the user. This is a convenience wrapper around [[inputNumber]].
     Returns [[null]] on EOF."
    shared default Point? inputPoint(
            "The prompt to use to prompt the user."
            String prompt) {
        print(prompt);
        if (exists row = inputNumber("Row: "), exists column = inputNumber("Column: ")) {
            return Point(row, column);
        } else {
            return null;
        }
    }
}
