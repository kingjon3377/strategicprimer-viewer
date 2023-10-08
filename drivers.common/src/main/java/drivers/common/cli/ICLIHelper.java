package drivers.common.cli;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;

import common.map.HasName;
import common.map.Point;

import java.math.BigDecimal;
import java.util.List;

/**
 * An interface for the "CLI helper," which encapsulates input and output
 * streams, allowing automated testing of command-line apps and graphical
 * wrappers around them.
 *
 * TODO: Add printf(), wrapping String.format() (to reduce caller verbosity)
 */
public interface ICLIHelper {
    // TODO: Split up methods instead of using this boolean-replacement enum?
    enum ListChoiceBehavior {ALWAYS_PROMPT, AUTO_CHOOSE_ONLY}

    /**
     * Have the user choose an item from a list. Returns the index and the
     * item, if any.  On EOF, returns an index of -2.
     *
     * @param items The list of items to choose from.
     * @param description The description to give before printing the list.
     * @param none What to print if there are none.
     * @param prompt What to prompt the user with.
     * @param behavior How to handle a list of only one element
     */
    <Element extends HasName> Pair<Integer, @Nullable Element> chooseFromList(
            List<? extends Element> items, String description, String none, String prompt, ListChoiceBehavior behavior);

    /**
     * Have the user choose an item from a list. Returns the index and the
     * item, if any. On EOF, returns an index of -2.
     *
     * TODO: rename back to chooseFromList()?
     *
     * @param items The list of items to choose from.
     * @param description The description to give before printing the list.
     * @param none What to print if there are none.
     * @param prompt What to prompt the user with.
     * @param behavior How to handle a list of only one element
     */
    Pair<Integer, @Nullable String> chooseStringFromList(List<String> items, String description,
                                                         String none, String prompt, ListChoiceBehavior behavior);

    /**
     * Read from the input stream until a non-negative integer is entered,
     * then return it.  Returns null on EOF.
     *
     * TODO: Throw IOException on EOF instead and return int?
     *
     * @param prompt The prompt to prompt the user with.
     */
    @Nullable Integer inputNumber(String prompt);

    /**
     * Read from the input stream repeatedly until a valid non-negative
     * decimal number is entered, then return it. Returns null on EOF.
     *
     * TODO: Throw IOException on EOF instead and return int?
     *
     * @param prompt The prompt to prompt the user with.
     */
    @Nullable BigDecimal inputDecimal(String prompt);

    /**
     * Read a line of input. It is trimmed of leading and trailing whitespace. Returns null on EOF.
     *
     * TODO: Throw IOException on EOF instead and return int?
     *
     * @param prompt The prompt to prompt the user with.
     */
    @Nullable String inputString(String prompt);

    /**
     * Read a multiline string from the user. It is trimmed of leading and
     * trailing whitespace, except that if it ends with multiple newlines
     * two of them will be retained. Returns null on EOF.
     *
     * TODO: Throw IOException on EOF instead and return int?
     *
     * @param prompt The prompt to prompt the user with.
     */
    @Nullable String inputMultilineString(String prompt);

    // TODO: Why not just use Predicate?
    @FunctionalInterface
    interface TrinaryPredicate<Input> {
        @Nullable Boolean test(Input item);
    }

    /**
     * Returns null if "input" is "quit" and false otherwise.
     */
    default @Nullable Boolean defaultQuitHandler(final String input) {
        if ("quit".equals(input)) {
            return null;
        } else {
            return false;
        }
    }

    /**
     * Ask the user a yes-or-no question. Returns null on EOF or if the
     * user enters "quit".
     *
     * @param prompt The prompt to prompt the user with.
     */
    default @Nullable Boolean inputBoolean(final String prompt) {
        return inputBoolean(prompt, this::defaultQuitHandler);
    }

    /**
     * Ask the user a yes-or-no question. Returns null on EOF or if
     * {@link #defaultQuitHandler} returns null.
     *
     * @param prompt The prompt to prompt the user with.
     * @param quitResultFactory A function to produce null (to return) if
     * an input should short-circuit the loop.
     */
    @Nullable Boolean inputBoolean(String prompt, TrinaryPredicate<String> quitResultFactory);

    /**
     * Ask the user a yes-or-no question, allowing "yes to all" or "no to
     * all" to forestall further similar questions. Returns null on EOF or
     * if {@link #defaultQuitHandler} returns null.
     *
     * @param prompt The prompt to prompt the user with, also used as the key to identify similar questions.
     */
    default @Nullable Boolean inputBooleanInSeries(final String prompt) {
        return inputBooleanInSeries(prompt, prompt);
    }

    /**
     * Ask the user a yes-or-no question, allowing "yes to all" or "no to
     * all" to forestall further similar questions. Returns null on EOF or
     * if {@link #defaultQuitHandler} returns null.
     *
     * @param prompt The prompt to prompt the user with.
     * @param key The prompt (or other key) to compare to others to define "similar" questions.
     */
    default @Nullable Boolean inputBooleanInSeries(final String prompt, final String key) {
        return inputBooleanInSeries(prompt, key, this::defaultQuitHandler);
    }

    /**
     * Ask the user a yes-or-no question, allowing "yes to all" or "no to
     * all" to forestall further similar questions. Returns null on EOF or
     * if "quitResultFactory" returns null.
     *
     * @param prompt The prompt to prompt the user with.
     * @param key The prompt (or other key) to compare to others to define "similar" questions.
     * @param quitResultFactory A function to produce null (to return) if
     * an input should short-circuit the loop.
     */
    @Nullable Boolean inputBooleanInSeries(String prompt, String key,
                                           TrinaryPredicate<String> quitResultFactory);

    /**
     * Print a newline.
     */
    default void println() {
        println("");
    }

    /**
     * Print the specified string, then a newline.
     * @param line The line to print
     */
    void println(String line);

    /**
     * Print the specified strings.
     *
     * TODO: Ceylon had String+; change to (String initial, String... remaining)?
     *
     * @param text The strings to print.
     */
    void print(String... text);

    /**
     * Print the specified string, if it hasn't been printed in the last 30 lines.
     *
     * TODO: Add test of this in CLITests
     *
     * @param line The line to print
     */
    default void printlnAtInterval(final String line) {
        printlnAtInterval(line, 30);
    }

    /**
     * Print the specified string, if it hasn't been printed in the last "interval" lines.
     *
     * TODO: Add test of this in CLITests
     *
     * @param line The line to print
     * @param interval The minimum number of lines between occurrences
     */
    void printlnAtInterval(String line, int interval);

    /**
     * Get a {@link Point} from the user. This is a convenience wrapper
     * around {@link #inputNumber}. Returns null on EOF.
     *
     * @param prompt The prompt to use to prompt the user.
     */
    default @Nullable Point inputPoint(final String prompt) {
        print(prompt);
        if (!prompt.isEmpty() && Character.isWhitespace(prompt.charAt(prompt.length() - 1))) {
            print(" ");
        }
        final Integer row = inputNumber("Row: ");
        if (row != null) {
            final Integer column = inputNumber("Column: ");
            if (column != null) {
                return new Point(row, column);
            }
        }
        return null;
    }
}
