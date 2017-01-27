package controller.map.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import model.map.HasName;
import org.eclipse.jdt.annotation.NonNull;
import util.EqualsAny;
import util.IsNumeric;
import view.util.SystemIn;
import view.util.SystemOut;

/**
 * A helper class to let help CLIs interact with the user.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class CLIHelper implements ICLIHelper {
	/**
	 * A parser for numbers.
	 */
	private static final NumberFormat NUM_PARSER = NumberFormat.getIntegerInstance();
	/**
	 * The input stream we'll read from.
	 */
	private final BufferedReader istream;
	/**
	 * The output stream we'll write to.
	 */
	private final PrintWriter ostream;
	/**
	 * The current state of the yes-to-all or no-to-all possibility. Absent if not set,
	 * present if set, and the boolean value is what to return.
	 */
	private final Map<String, Boolean> seriesState = new HashMap<>();

	/**
	 * No-arg constructor.
	 */
	public CLIHelper() {
		this(new InputStreamReader(SystemIn.SYS_IN),
				new OutputStreamWriter(SystemOut.SYS_OUT));
	}

	/**
	 * Constructor.
	 *
	 * @param in  the stream to read from.
	 * @param out the writer to write to
	 */
	public CLIHelper(final Reader in, final Writer out) {
		istream = new BufferedReader(in);
		ostream = new PrintWriter(out);
	}

	/**
	 * Convert a string to lowercase.
	 * @param str a string
	 * @return its lower case equivalent
	 */
	private static String lower(final String str) {
		return str.toLowerCase(Locale.US);
	}

	/**
	 * Ask the user to choose an item from the list, and if he does carry out an
	 * operation on it and then ask if he wants to do another.
	 * @param <T> the type of things in the list
	 * @param choice how to ask the user to choose an item from the list
	 * @param prompt the prompt to use to ask if the user wants to continue
	 * @param operation what to do with the chosen item in the list
	 * @throws IOException on I/O error getting the user's choice
	 */
	@Override
	public <T> void loopOnList(final List<T> list,
							   final ChoiceOperation choice,
							   final String prompt,
							   final ThrowingConsumer<T> operation) throws IOException {
		for (int num = choice.choose(this); num >= 0 && num < list.size();
				num = choice.choose(this)) {
			operation.accept(list.remove(num), this);
			if (list.isEmpty() || !inputBoolean(prompt)) {
				break;
			}
		}
	}

	/**
	 * Ask the user to choose an item from the list, and if he does carry out an
	 * operation on it and then ask if he wants to do another.
	 * @param <T> the type of things in the list
	 * @param choice how to ask the user to choose an item from the list
	 * @param prompt the prompt to use to ask if the user wants to continue
	 * @param addition what to do if the user chooses "add a new one"
	 * @param operation what to do with the chosen item in the list
	 * @throws IOException on I/O error getting the user's choice
	 */
	@Override
	public <T> void loopOnMutableList(final List<T> list,
									  final ChoiceOperation choice,
									  final String prompt,
									  final ListAmendment<T> addition,
									  final ThrowingConsumer<T> operation)
			throws IOException {
		for (int num = choice.choose(this); num <= list.size();
				num = choice.choose(this)) {
			final T item;
			if (num < 0 || num == list.size()) {
				final Optional<T> temp = addition.amendList(list, this);
				if (temp.isPresent()) {
					item = temp.get();
				} else {
					println("Select the new item at the next prompt.");
					continue;
				}
			} else {
				item = list.get(num);
			}
			operation.accept(item, this);
			if (!inputBoolean(prompt)) {
				break;
			}
		}
	}

	/**
	 * Print a list of things by name and number.
	 *
	 * @param <T> the type of things in the list
	 * @param list the list to print.
	 * @param func the function to turn each item into a String
	 */
	private <T> void printList(final List<? extends T> list,
							   final Function<T, String> func) {
		for (int i = 0; i < list.size(); i++) {
			ostream.append(Integer.toString(i));
			ostream.append(": ");
			ostream.println(func.apply(list.get(i)));
		}
		ostream.flush();
	}

	/**
	 * Have the user choose an item from a list.
	 *
	 * @param <T>    The type of things in the list
	 * @param items  the list of items
	 * @param desc   the description to give before printing the list
	 * @param none   what to print if there are none
	 * @param prompt what to prompt the user with
	 * @param auto   whether to automatically choose if there's only one choice
	 * @return the user's selection, or -1 if there are none
	 * @throws IOException on I/O error getting the user's input
	 */
	@Override
	public <T extends HasName> int chooseFromList(final List<@NonNull ? extends T> items,
												  final String desc, final String none,
												  final String prompt, final boolean
																			   auto)
			throws IOException {
		return chooseFromListImpl(items, desc, none, prompt, auto, HasName::getName);
	}

	/**
	 * Read input from stdin repeatedly until a non-negative integer is entered, and
	 * return it.
	 *
	 * @param prompt The prompt to prompt the user with
	 * @return the number entered
	 * @throws IOException on I/O error
	 */
	@Override
	public int inputNumber(final String prompt) throws IOException {
		int retval = -1;
		while (retval < 0) {
			ostream.print(prompt);
			ostream.flush();
			final String input = istream.readLine();
			if (input == null) {
				throw new IOException("Null line of input");
			} else if (IsNumeric.isNumeric(input)) {
				try {
					retval = NUM_PARSER.parse(input).intValue();
				} catch (final ParseException e) {
					// In practice we can't get here, as IsNumeric generally works
					//noinspection ObjectAllocationInLoop
					final NumberFormatException numFormatExcept =
							new NumberFormatException("Failed to parse number from " +
															  "input");
					numFormatExcept.initCause(e);
					throw numFormatExcept;
				}
			}
		}
		return retval;
	}

	/**
	 * Read input from stdin repeatedly until a valid non-negative decimal number is
	 * entered, and return it.
	 *
	 * @param prompt the prompt to prompt the user with
	 * @return the number entered
	 * @throws IOException on I/O error
	 */
	@Override
	public BigDecimal inputDecimal(final String prompt) throws IOException {
		BigDecimal retval = BigDecimal.ZERO.subtract(BigDecimal.ONE);
		while (retval.compareTo(BigDecimal.ZERO) < 0) {
			ostream.print(prompt);
			ostream.flush();
			final String input = istream.readLine();
			if (input == null) {
				throw new IOException("Null line of input");
			} else {
				try {
					retval = new BigDecimal(input.trim(), MathContext.UNLIMITED);
				} catch (final NumberFormatException except) {
					ostream.println("Invalid number.");
					// continue;
				}
			}
		}
		return retval;
	}

	/**
	 * Read input from stdin. (The input is trimmed of leading and trailing whitespace.)
	 *
	 * @param prompt The prompt to prompt the user with
	 * @return the string entered.
	 * @throws IOException on I/O error
	 */
	@Override
	public String inputString(final String prompt) throws IOException {
		ostream.print(prompt);
		ostream.flush();
		final String line = istream.readLine();
		if (line == null) {
			return "";
		} else {
			return line.trim();
		}
	}

	/**
	 * Ask the user a yes-or-no question.
	 *
	 * @param prompt the string to prompt the user with
	 * @return true if yes, false if no
	 * @throws IOException on I/O error
	 */
	@Override
	public boolean inputBoolean(final String prompt) throws IOException {
		//noinspection ForLoopWithMissingComponent
		for (String input = lower(inputString(prompt)); ;
				input = lower(inputString(prompt))) {
			if (EqualsAny.equalsAny(input, "yes", "true", "y", "t")) {
				return true;
			} else if (EqualsAny.equalsAny(input, "no", "false", "n", "f")) {
				return false;
			} else {
				ostream.println("Please enter 'yes', 'no', 'true', or 'false',");
				ostream.println("or the first character of any of those.");
			}
		}
	}

	/**
	 * Ask the user a yes-or-no question, allowing "yes to all" or "no to all" to skip
	 * further questions.
	 *
	 * @param prompt the string to prompt the user with
	 * @param key    a string passed here for all questions that should be skipped if "X
	 *               to all"
	 * @return the user's answer (minus the "to all")
	 * @throws IOException on I/O error
	 */
	@Override
	public boolean inputBooleanInSeries(final String prompt, final String key)
			throws IOException {
		if (seriesState.containsKey(key)) {
			ostream.print(prompt);
			final boolean retval = seriesState.get(key).booleanValue();
			if (retval) {
				ostream.println("yes");
			} else {
				ostream.println("no");
			}
			return retval;
		} else {
			//noinspection ForLoopWithMissingComponent
			for (String input = lower(inputString(prompt)); ;
					input = lower(inputString(prompt))) {
				if (EqualsAny.equalsAny(input, "all", "ya", "ta", "always")) {
					seriesState.put(key, Boolean.TRUE);
					return true;
				} else if (EqualsAny.equalsAny(input, "yes", "true", "y", "t")) {
					return true;
				} else if (EqualsAny.equalsAny(input, "none", "na", "fa", "never")) {
					seriesState.put(key, Boolean.FALSE);
					return false;
				} else if (EqualsAny.equalsAny(input, "no", "false", "n", "f")) {
					return false;
				} else {
					ostream.println(
							"Please enter 'yes', 'no', 'true', or 'false', the first");
					ostream.println(
							"character of any of those, or 'all', 'none', 'always'");
					ostream.println(
							"or 'never' to use the same answer for all further " +
									"questions");
				}
			}
		}
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "CLIHelper";
	}

	/**
	 * Have the user choose an item from a list.
	 *
	 * @param items  the list of items
	 * @param desc   the description to give before printing the list
	 * @param none   what to print if there are none
	 * @param prompt what to prompt the user with
	 * @param auto   whether to automatically choose if there's only one choice
	 * @return the user's selection, or -1 if there are none
	 * @throws IOException on I/O error getting the user's input
	 */
	@Override
	public int chooseStringFromList(final List<String> items, final String desc,
									final String none, final String prompt,
									final boolean auto)
			throws IOException {
		return chooseFromListImpl(items, desc, none, prompt, auto, x -> x);
	}

	/**
	 * Print a formatted string.
	 *
	 * @param format the format string
	 * @param args   the arguments to fill into the format string.
	 */
	@Override
	public void printf(final String format, final Object... args) {
		ostream.printf(format, args);
		ostream.flush();
	}

	/**
	 * Print the specified string, then a newline.
	 *
	 * @param line the line to print
	 */
	@Override
	public void println(final String line) {
		ostream.println(line);
		ostream.flush();
	}

	/**
	 * Print the specified string.
	 *
	 * @param text the string to print
	 */
	@Override
	public void print(final String text) {
		ostream.print(text);
		ostream.flush();
	}

	/**
	 * Close I/O streams.
	 */
	@Override
	public void close() throws IOException {
		istream.close();
		ostream.close();
	}
	/**
	 * Implementation of chooseFromList() and chooseStringFromList().
	 * @param <T> the type of things in the list
	 * @param items the list of items
	 * @param desc the description to give before printing the list
	 * @param none what to print if there are none
	 * @param func the function to call to get each item's description
	 * @param prompt what to prompt the user with
	 * @param auto whether to automatically choose if there's only one choice
	 * @return the user's selection, or -1 if there are none
	 * @throws IOException on I/O error getting the user's input
	 */
	private <T> int chooseFromListImpl(final List<@NonNull ? extends T> items,
									   final String desc, final String none,
									   final String prompt, final boolean auto,
									   final Function<T, String> func) throws IOException {
		if (items.isEmpty()) {
			ostream.println(none);
			ostream.flush();
			return -1;
		}
		ostream.println(desc);
		if (auto && (items.size() == 1)) {
			ostream.print("Automatically choosing only item, ");
			ostream.println(func.apply(items.get(0)));
			ostream.flush();
			return 0;
		} else {
			printList(items, func);
			return inputNumber(prompt);
		}
	}
}
