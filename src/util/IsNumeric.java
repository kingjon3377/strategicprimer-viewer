package util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A helper class to determine whether a String is numeric or not.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("UtilityClassCanBeEnum")
public final class IsNumeric {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(IsNumeric.class);
	/**
	 * The NumberFormat object we use for parsing.
	 */
	private static final NumberFormat PARSER = NumberFormat.getIntegerInstance();

	/**
	 * Do not instantiate.
	 */
	private IsNumeric() {
		// Do nothing.
	}

	/**
	 * Whether the given String contains only numeric (integer) data.
	 * @param input a String
	 * @return whether it contains numeric data or not
	 */
	public static boolean isNumeric(final String input) {
		try {
			PARSER.parse(input);
			return true;
		} catch (final NumberFormatException | ParseException except) {
			LOGGER.log(Level.FINE, "Non-numeric input", except);
			return false;
		}
	}

}
