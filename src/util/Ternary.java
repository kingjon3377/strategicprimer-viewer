package util;

/**
 * A class to house our ternary() method.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class Ternary {
	/**
	 * Do not instantiate.
	 */
	private Ternary() {
		// Static-only methods.
	}
	/**
	 * Ternary.
	 * @param condition a boolean variable
	 * @param truth what to return if true
	 * @param falsehood what to return if false
	 * @param <T> the type of truth and falsehood
	 * @return truth if condition is true, falsehood otherwise
	 */
	public static <T> T ternary(final boolean condition, final T truth,
								 final T falsehood) {
		if (condition) {
			return truth;
		} else {
			return falsehood;
		}
	}
}
