package controller.map.misc;

import static view.util.SystemOut.SYS_OUT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNull;

import model.map.HasName;
import util.EqualsAny;
import util.IsNumeric;
import util.NullCleaner;

/**
 * A helper class to let help CLIs interact with the user.
 *
 * TODO: Write tests (using string streams).
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class CLIHelper implements ICLIHelper {
	/**
	 * The input stream we'll read from.
	 */
	private final BufferedReader istream;
	/**
	 * A parser for numbers.
	 */
	private static final NumberFormat NUM_PARSER = NullCleaner
			.assertNotNull(NumberFormat.getIntegerInstance());

	/**
	 * No-arg constructor.
	 */
	@SuppressWarnings("null") // System.in cannot be null
	public CLIHelper() {
		this(System.in);
	}

	/**
	 * Constructor.
	 *
	 * @param in the stream to read from.
	 */
	public CLIHelper(final InputStream in) { // NOPMD
		istream = new BufferedReader(new InputStreamReader(in));
	}

	/**
	 * Print a list of things by name and number.
	 *
	 * @param ostream the stream to write to
	 * @param list the list to print.
	 * @throws IOException on I/O error writing to stream
	 */
	private static void printList(final Appendable ostream,
			final List<? extends HasName> list) throws IOException {
		for (int i = 0; i < list.size(); i++) {
			ostream.append(Integer.toString(i));
			ostream.append(": ");
			ostream.append(list.get(i).getName());
			ostream.append('\n');
		}
	}

	/**
	 * Have the user choose an item from a list.
	 *
	 * @param <T> The type of things in the list
	 * @param items the list of items
	 * @param desc the description to give before printing the list
	 * @param none what to print if there are none
	 * @param prompt what to prompt the user with
	 * @param auto whether to automatically choose if there's only one choice
	 * @return the user's selection, or -1 if there are none
	 * @throws IOException on I/O error getting the user's input
	 */
	@Override
	public <T extends HasName> int chooseFromList(
			final List<@NonNull ? extends T> items, final String desc,
			final String none, final String prompt, final boolean auto)
			throws IOException {
		if (items.isEmpty()) {
			SYS_OUT.println(none);
			return -1; // NOPMD
		}
		SYS_OUT.println(desc);
		if (auto && items.size() == 1) {
			SYS_OUT.print("Automatically choosing only item, ");
			SYS_OUT.println(items.get(0));
			return 0; // NOPMD
		} else {
			printList(SYS_OUT, items);
			return inputNumber(prompt);
		}
	}

	/**
	 * Turn an Iterable into a List. This is, of course, an eager
	 * implementation; make sure not to use on anything with an infinite
	 * iterator!
	 *
	 * FIXME: This is probably more generally useful and should be moved
	 * elsewhere, if it's not already somewhere I forgot about.
	 *
	 * @param <T> the type contained in the iterable.
	 * @param iter the thing to iterate over
	 * @return a List representing the same data.
	 */
	public static <T> List<T> toList(final Iterable<T> iter) {
		final List<T> retval = new ArrayList<>();
		for (final T item : iter) {
			retval.add(item);
		}
		return retval;
	}

	/**
	 * Read input from stdin repeatedly until a nonnegative integer is entered,
	 * and return it.
	 *
	 * @param prompt The prompt to prompt the user with
	 * @return the number entered
	 * @throws IOException on I/O error
	 */
	@Override
	public int inputNumber(final String prompt) throws IOException {
		int retval = -1;
		while (retval < 0) {
			SYS_OUT.print(prompt);
			final String input = istream.readLine();
			if (input == null) {
				throw new IOException("Null line of input");
			} else if (IsNumeric.isNumeric(input)) {
				try {
					retval = NUM_PARSER.parse(input).intValue();
				} catch (final ParseException e) {
					NumberFormatException nexcept = new NumberFormatException(
							"Failed to parse number from input");
					nexcept.initCause(e);
					throw nexcept;
				}
			}
		}
		return retval;
	}

	/**
	 * Read input from stdin. (The input is trimmed of leading and trailing
	 * whitespace.)
	 *
	 * @param prompt The prompt to prompt the user with
	 * @return the string entered.
	 * @throws IOException on I/O error
	 */
	@Override
	public String inputString(final String prompt) throws IOException {
		SYS_OUT.print(prompt);
		final String line = istream.readLine();
		if (line == null) {
			return ""; // NOPMD
		} else {
			return NullCleaner.assertNotNull(line.trim());
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
		for (String input = NullCleaner.assertNotNull(
				inputString(prompt).toLowerCase(Locale.US));; input = NullCleaner
						.assertNotNull(inputString(prompt).toLowerCase(Locale.US))) {
			if (EqualsAny.equalsAny(input, "yes", "true", "y", "t")) {
				return true; // NOPMD
			} else if (EqualsAny.equalsAny(input, "no", "false", "n", "f")) {
				return false;
			}
			// else
			SYS_OUT.println("Please enter 'yes', 'no', 'true', or 'false',");
			SYS_OUT.println("or the first character of any of those.");
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "CLIHelper";
	}
}
