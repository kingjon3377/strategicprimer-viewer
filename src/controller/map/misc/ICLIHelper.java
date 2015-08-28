package controller.map.misc;

import java.io.IOException;
import java.util.List;

import model.map.HasName;

/**
 * An interface for the "CLI helper", to make automated testing of exploration
 * possible.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public interface ICLIHelper {

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
	<T extends HasName> int chooseFromList(List<? extends T> items,
			String desc, String none, String prompt,
			boolean auto) throws IOException;

	/**
	 * Read input from stdin repeatedly until a nonnegative integer is entered,
	 * and return it.
	 *
	 * @param prompt The prompt to prompt the user with
	 * @return the number entered
	 * @throws IOException on I/O error
	 */
	int inputNumber(String prompt) throws IOException;

	/**
	 * Read input from stdin. (The input is trimmed of leading and trailing
	 * whitespace.)
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
	boolean inputBoolean(String prompt) throws IOException;
}
