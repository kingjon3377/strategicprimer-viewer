package controller.map.misc;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import model.map.HasName;
import model.map.Point;
import model.map.PointFactory;

/**
 * An interface for the "CLI helper", to make automated testing of exploration possible.
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
public interface ICLIHelper extends Closeable {

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
	<T extends HasName> int chooseFromList(List<? extends T> items, String desc,
										String none, String prompt, boolean auto)
			throws IOException;

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
	int chooseStringFromList(List<String> items, String desc, String none, String prompt,
							boolean auto) throws IOException;

	/**
	 * Read input from stdin repeatedly until a non-negative integer is entered, and
	 * return
	 * it.
	 *
	 * @param prompt The prompt to prompt the user with
	 * @return the number entered
	 * @throws IOException on I/O error
	 */
	int inputNumber(String prompt) throws IOException;

	/**
	 * Read input from stdin. (The input is trimmed of leading and trailing whitespace.)
	 *
	 * @param prompt The prompt to prompt the user with
	 * @return the string entered.
	 * @throws IOException on I/O error
	 */
	String inputString(String prompt) throws IOException;

	/**
	 * Ask the user a yes-or-no question.
	 *
	 * @param prompt the string to prompt the user with
	 * @return true if yes, false if no
	 * @throws IOException on I/O error
	 */
	@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
	boolean inputBoolean(String prompt) throws IOException;

	/**
	 * Ask the user a yes-or-no question, allowing "yes to all" or "no to all" to skip
	 * further similar questions.
	 *
	 * @param prompt the string to prompt the user with
	 * @throws IOException on I/O error
	 */
	default boolean inputBooleanInSeries(final String prompt) throws IOException {
		return inputBooleanInSeries(prompt, prompt);
	}

	/**
	 * Ask the user a yes-or-no question, allowing "yes to all" or "no to all" to skip
	 * further similar questions.
	 *
	 * @param prompt the string to prompt the user with
	 * @param key the prompt to compare to others to define "similar" questions.
	 * @throws IOException on I/O error
	 */
	boolean inputBooleanInSeries(String prompt, final String key) throws IOException;
	/**
	 * Print a formatted string.
	 * @param format the format string
	 * @param args the arguments to fill into the format string.
	 */
	void printf(String format, Object ... args);
	/**
	 * Print the specified string, then a newline.
	 * @param line the line to print
	 */
	void println(String line);
	/**
	 * Print the specified string.
	 * @param text the string to print
	 */
	void print(String text);

	/**
	 * Interface for getting-an-int-from-the-user operations.
	 */
	@FunctionalInterface
	interface ChoiceOperation {
		/**
		 * The operation.
		 * @return the value the user chose.
		 * @throws IOException on I/O error interacting with the user
		 */
		int choose() throws IOException;
	}
	/**
	 * Get a Point from the user. This is a convenience wrapper around two calls to
	 * inputNumber.
	 * @param prompt the prompt to use
	 */
	default Point inputPoint(final String prompt) throws IOException {
		print(prompt);
		return PointFactory.point(inputNumber("Row: "), inputNumber("Column: "));
	}
}
