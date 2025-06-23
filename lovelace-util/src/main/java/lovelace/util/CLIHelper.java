package lovelace.util;

import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * A collection of methods to help a command-line interface interact with the user.
 */
public final class CLIHelper {
	/**
	 * No instance methods or state.
	 */
	private CLIHelper() {
	}

	/**
	 * If the given list contains exactly one item, return it; if it is empty return null; otherwise ask the user to
	 * choose an item from the list, and return the item the user chooses, or null if the process
	 * fails for any reason.
	 *
	 * @param header  The header to print before the list. If null, no header is printed.
	 * @param prompt  The prompt to print to ask the user for his or her choice.
	 * @param choices The list for the user to choose from, defined as tuples of user-friendly descriptions and the
	 *                objects themselves.
	 */
	@SafeVarargs
	public static @Nullable <Element> Element chooseFromListWithAuto(
			final @Nullable String header,
			final @NotNull String prompt,
			final Pair<String, Element>... choices) {
		if (choices.length > 0) {
			if (choices.length == 1) {
				final String desc = choices[0].getValue0();
				final Element item = choices[0].getValue1();
				System.out.print("Automatically choosing only item, ");
				System.out.println(desc);
				return item;
			} else {
				if (Objects.nonNull(header)) {
					System.out.println(header);
				}
				printList(Stream.of(choices).map(Pair::getValue0).toArray(String[]::new));
				final @Nullable Integer index = inputNumber(prompt);
				if (Objects.nonNull(index) && index >= 0 && index < choices.length) {
					return choices[index].getValue1();
				} else {
					return null;
				}
			}
		} else {
			return null;
		}
	}

	/**
	 * Ask the user to choose an item from the given list; return the item the user chooses, or null if the process
	 * fails for any reason or the list was empty.
	 *
	 * @param header  The header to print before the list. If null, no header is printed.
	 * @param prompt  The prompt to print to ask the user for his or her choice.
	 * @param choices The list for the user to choose from, defined as tuples of user-friendly descriptions and the
	 *                objects themselves.
	 */
	@SafeVarargs
	public static @Nullable <Element> Element chooseFromList(
			final @Nullable String header,
			final @NotNull String prompt,
			final Pair<String, Element>... choices) {
		if (choices.length > 0) {
			if (Objects.nonNull(header)) {
				System.out.println(header);
			}
			printList(Stream.of(choices).map(Pair::getValue0).toArray(String[]::new));
			final @Nullable Integer index = inputNumber(prompt);
			if (Objects.nonNull(index) && index >= 0 && index < choices.length) {
				return choices[index].getValue1();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Print a list of strings to the standard output, each starting on a
	 * new line with its index in the list prepended.
	 */
	public static void printList(final String... list) {
		int i = 0;
		for (final String string : list) {
			System.out.printf("%d. %s%n", i, string);
			i++;
		}
	}

	/**
	 * Print a prompt to the standard output, adding a space if the
	 * provided prompt does not end with whitespace.
	 */
	static void writePrompt(final String prompt) {
		System.out.print(prompt);
		if (!prompt.isEmpty() && !Character.isWhitespace(prompt.charAt(prompt.length() - 1))) {
			System.out.print(" ");
		}
	}

	/**
	 * Parse a single number from a Scanner's next line. Returns null on EOF or error reading the line; returns -1 on
	 * non-numeric input.
	 */
	private static @Nullable Integer inputNumberImpl(final Scanner scanner) {
		try {
			final String input = scanner.nextLine();
			return Integer.parseInt(input);
		} catch (final NoSuchElementException | IllegalStateException except) {
			return null;
		} catch (final NumberFormatException except) {
			return -1;
		}
	}

	/**
	 * Ask the user to enter a nonnegative integer. Loops until one is
	 * provided on the standard input. Returns null on EOF.
	 */
	public static @Nullable Integer inputNumber(final String prompt) {
		int retval = -1;
		try (final Scanner scanner = new Scanner(SystemIn.STDIN)) {
			while (retval < 0) {
				writePrompt(prompt);
				final Integer temp = inputNumberImpl(scanner);
				if (temp == null) {
					return null;
				} else {
					retval = temp;
				}
			}
			return retval;
		}
	}

	/**
	 * Ask the user a yes-or-no question. Returns true if "yes", "true", "y", or "t" is provided on the standard input,
	 * returns false if "no", "false", "n", or "f" is provided, and on any other input asks again and again (loops)
	 * until an acceptable input is provided. (Those answers are parsed case-insensitively.) Returns null on EOF.
	 */
	public static @Nullable Boolean inputBoolean(final String prompt) {
		try (final Scanner scanner = new Scanner(SystemIn.STDIN)) {
			while (true) {
				writePrompt(prompt);
				final String input = scanner.nextLine().strip().toLowerCase();
				switch (input) {
					case "yes":
					case "true":
					case "y":
					case "t":
						return true;
					case "no":
					case "false":
					case "n":
					case "f":
						return false;
					default:
						System.out.println("Please enter 'yes', 'no', 'true', or 'false',");
						System.out.println("or the first character of any of those.");
				}
			}
		} catch (final NoSuchElementException | IllegalStateException except) {
			return null;
		}
	}
}
