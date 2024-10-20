package drivers.common.cli;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.function.Consumer;

import lovelace.util.LovelaceLogger;
import lovelace.util.SystemIn;
import org.eclipse.jdt.annotation.NonNull;
import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

import common.map.HasName;

import static lovelace.util.NumParsingHelper.isNumeric;
import static lovelace.util.NumParsingHelper.parseInt;

import java.util.List;
import java.util.function.Function;
import java.io.IOException;
import java.util.stream.Stream;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * A helper class to help command-line apps interact with the user,
 * encapsulating input and output streams.
 *
 * TODO: Port to use java.io.Console more directly and extensively?
 */
public final class CLIHelper implements ICLIHelper {
	@FunctionalInterface
	public interface IOSource {
		@Nullable String readLine() throws IOException;
	}

	public CLIHelper(final IOSource istream, final Consumer<String> ostream, final Runnable flushOstream) {
		this.istream = istream;
		this.ostream = ostream;
		this.flushOstream = flushOstream;
	}

	private static IOSource stdin() {
		final Console console = System.console();
		if (Objects.isNull(console)) {
			// TODO: Does this consume the newline character?
			return new BufferedReader(new InputStreamReader(SystemIn.STDIN))::readLine;
		} else {
			return console::readLine;
		}
	}

	public CLIHelper() {
		this(stdin(), System.out::print, System.out::flush);
	}

	/**
	 * A way to read a line at a time, presumably from the user.
	 */
	private final IOSource istream;

	/**
	 * A consumer of output, presumably sending it to the user.
	 */
	private final Consumer<String> ostream;

	/**
	 * A method to flush the output stream, or a noop if that doesn't make sense.
	 */
	private final Runnable flushOstream;

	/**
	 * The current state of the yes-to-all/no-to-all possibility. Absent if
	 * not set, present if set, and the boolean value is what to return.
	 */
	private final Map<String, Boolean> seriesState = new HashMap<>();

	private final Map<String, Long> intervals = new HashMap<>();

	/**
	 * Print the specified string.
	 */
	@Override
	public void print(final String... text) {
		final long newlines = Stream.of(text)
				.mapToLong(s -> s.chars().filter(c -> c == '\n' || c == '\r').count()).sum();
		if (newlines > 0) {
			intervals.replaceAll((key, lines) -> lines + newlines);
		}
		for (final String part : text) {
			ostream.accept(part);
		}
	}

	@Override
	public void printf(final String format, final Object... arguments) {
		print(format.formatted(arguments));
	}

	/**
	 * Flush the output stream, if a means to do so has been provided.
	 */
	@Override
	public void flush() {
		flushOstream.run();
	}

	/**
	 * Print the specified string, then a newline.
	 */
	@Override
	public void println(final String line) {
		print(line);
		print(System.lineSeparator());
	}

	/**
	 * Print a prompt, adding whitespace if the prompt didn't end with it.
	 */
	private void writePrompt(final String prompt) {
		print(prompt);
		if (!prompt.isEmpty() && !Character.isWhitespace(prompt.charAt(prompt.length() - 1))) {
			print(" ");
		}
	}

	/**
	 * Ask the user a yes-or-no question. Returns null on EOF.
	 */
	@Override
	public Boolean inputBoolean(final String prompt, final TrinaryPredicate<String> quitResultFactory) {
		while (true) {
			final String input = Optional.ofNullable(inputString(prompt))
					.map(String::toLowerCase).orElse(null);
			if (Objects.isNull(input) || Objects.isNull(quitResultFactory.test(input))) {
				return null;
			} else if ("yes".equals(input) || "true".equals(input) ||
					"y".equals(input) || "t".equals(input)) {
				return true;
			} else if ("no".equals(input) || "false".equals(input) ||
					"n".equals(input) || "f".equals(input)) {
				return false;
			} else {
				println("Please enter \"yes\", \"no\", \"true\", or \"false\",");
				println("or the first character of any of those.");
			}
		}
	}

	/**
	 * Print a list of things by name and number.
	 */
	private <Element> void printList(final Iterable<? extends Element> list, final Function<Element, String> func) {
		int index = 0;
		for (final Element item : list) {
			printf("%d: %s%n", index, func.apply(item));
			index++;
		}
	}

	/**
	 * Implementation of {@link #chooseFromList} and {@link #chooseStringFromList}.
	 */
	private <Element> Pair<Integer, @Nullable Element> chooseFromListImpl(final List<@NonNull ? extends Element> items,
																		  final String description, final String none,
																		  final String prompt,
																		  final ListChoiceBehavior behavior,
																		  final Function<? super Element, String> func) {
		if (items.isEmpty()) {
			println(none);
			return Pair.with(-1, null);
		}
		println(description);
		if (behavior == ListChoiceBehavior.AUTO_CHOOSE_ONLY && items.size() == 1) {
			final Element first = items.getFirst();
			printf("Automatically choosing only item, %s.%n", func.apply(first));
			return Pair.with(0, first);
		} else {
			printList(items, func);
			final Integer retval = inputNumber(prompt);
			if (Objects.isNull(retval)) {
				return Pair.with(-2, null);
			} else if (retval < 0 || retval >= items.size()) {
				return Pair.with(retval, null);
			} else {
				return Pair.with(retval, items.get(retval));
			}
		}
	}

	// A helper method to appease the compiler's "may produce NPE" warning in chooseFromList()
	private static String getElementName(final @Nullable HasName item) {
		if (item == null) {
			return "null";
		} else {
			return item.getName();
		}
	}

	/**
	 * Have the user choose an item from a list.
	 */
	@Override
	public <Element extends HasName> Pair<Integer, @Nullable Element> chooseFromList(
			final List<@NonNull ? extends Element> list, final String description, final String none,
			final String prompt, final ListChoiceBehavior behavior) {
		return chooseFromListImpl(list, description, none, prompt, behavior, CLIHelper::getElementName);
	}

	/**
	 * Read an input line, logging any exceptions but returning null on I/O exception.
	 */
	private @Nullable String readLine() {
		try {
			return istream.readLine();
		} catch (final IOException except) {
			LovelaceLogger.warning(except, "I/O error");
			return null;
		}
	}

	/**
	 * Read input from the input stream repeatedly until a non-negative
	 * integer is entered, then return it. Returns null on EOF.
	 */
	@Override
	public @Nullable Integer inputNumber(final String prompt) {
		int retval = -1;
		while (retval < 0) {
			writePrompt(prompt);
			final String input = readLine();
			if (Objects.isNull(input)) {
				return null;
			}
			if (isNumeric(input)) {
				final OptionalInt temp = parseInt(input);
				if (temp.isPresent()) {
					retval = temp.getAsInt();
				}
			}
		}
		return retval;
	}

	/**
	 * Read from the input stream repeatedly until a valid non-negative
	 * decimal number is entered, then return it. Returns null on EOF.
	 */
	@Override
	public @Nullable BigDecimal inputDecimal(final String prompt) {
		final BigDecimal zero = BigDecimal.ZERO;
		BigDecimal retval = zero.subtract(BigDecimal.ONE);
		while (retval.compareTo(zero) < 0) {
			writePrompt(prompt);
			final String input = readLine();
			if (Objects.isNull(input)) {
				return null;
			}
			try {
				retval = new BigDecimal(input.strip());
			} catch (final NumberFormatException except) {
				println("Invalid number.");
				LovelaceLogger.trace(except, "Invalid number");
			}
		}
		return retval;
	}

	/**
	 * Read a line of input from the input stream. It is trimmed of leading
	 * and trailing whitespace. Returns null on EOF (or other I/O error).
	 */
	@Override
	public @Nullable String inputString(final String prompt) {
		writePrompt(prompt);
		return Optional.ofNullable(readLine()).map(String::trim).orElse(null);
	}

	/**
	 * Ask the user a yes-or-no question, allowing yes-to-all or no-to-all to skip further questions.
	 */
	@Override
	public @Nullable Boolean inputBooleanInSeries(final String prompt, final String key,
												  final TrinaryPredicate<String> quitResultFactory) {
		if (seriesState.containsKey(key)) {
			writePrompt(prompt);
			final boolean retval = seriesState.get(key);
			println(retval ? "yes" : "no");
			return retval;
		} // else
		while (true) {
			final String input = Optional.ofNullable(inputString(prompt))
					.map(String::toLowerCase).orElse(null);
			if (Objects.isNull(input) || Objects.isNull(quitResultFactory.test(input))) {
				return null;
			}
			//noinspection SwitchStatementWithTooManyBranches
			switch (input) {
				case "all", "ya", "ta", "always" -> {
					seriesState.put(key, true);
					return true;
				}
				case "none", "na", "fa", "never" -> {
					seriesState.put(key, false);
					return false;
				}
				case "yes", "true", "y", "t" -> {
					return true;
				}
				case "no", "false", "n", "f" -> {
					return false;
				}
				default -> {
					println("Please enter \"yes\", \"no\", \"true\", or \"false\", the first");
					println("character of any of those, or \"all\", \"none\", \"always\", or");
					println("\"never\" to use the same answer for all further questions.");
				}
			}
		}
	}

	/**
	 * Have the user choose an item from a list.
	 */
	@Override
	public Pair<Integer, @Nullable String> chooseStringFromList(final List<String> items, final String description,
	                                                            final String none, final String prompt,
	                                                            final ListChoiceBehavior behavior) {
		return chooseFromListImpl(items, description, none, prompt, behavior, Function.identity());
	}

	/**
	 * Ask the user for a multiline string.
	 */
	@Override
	public @Nullable String inputMultilineString(final String prompt) {
		final StringBuilder builder = new StringBuilder();
		printlnAtInterval("Type . on a line by itself to end input, or , to start over.");
		while (true) {
			if (builder.isEmpty()) {
				writePrompt(prompt);
			} else {
				print("> ");
			}
			final String line = readLine();
			if (Objects.isNull(line)) {
				return null;
			} else if (".".equals(line.strip())) {
				final String retval = builder.toString();
				if (retval.endsWith(System.lineSeparator() + System.lineSeparator())) {
					return retval.strip() + System.lineSeparator() +
							System.lineSeparator();
				} else {
					return retval.strip();
				}
			} else if (",".equals(line.strip())) {
				builder.setLength(0);
			} else {
				builder.append(line);
				builder.append(System.lineSeparator());
			}
		}
	}

	@Override
	public void printlnAtInterval(final String line, final int interval) {
		if (!intervals.containsKey(line) || intervals.get(line) >= interval) {
			println(line);
			intervals.put(line, 0L);
		}
	}
}
