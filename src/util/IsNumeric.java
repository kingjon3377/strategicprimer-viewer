package util;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * A helper class to determine whether a String is numeric or not.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
public final class IsNumeric {
	/**
	 * Do not instantiate.
	 */
	private IsNumeric() {
		// Do nothing.
	}
	/**
	 * The NumberFormat object we use for parsing.
	 */
	private static final NumberFormat PARSER = NullCleaner
			.assertNotNull(NumberFormat.getIntegerInstance());

	/**
	 * @param string a String
	 * @return whether it contains numeric data or not
	 */
	public static boolean isNumeric(final String string) {
		try {
			PARSER.parse(string);
			return true; // NOPMD
		} catch (final NumberFormatException except) {
			return false;
		} catch (ParseException e) {
			return false;
		}
	}

	/**
	 * @param value a value
	 * @param lower a lower bound
	 * @param upper an upper bound
	 * @return whether the value is between them
	 */
	public static boolean isBetween(final int value, final int lower,
			final int upper) {
		return value >= lower && value < upper;
	}
}
