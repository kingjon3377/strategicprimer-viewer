package util;

/**
 * A pair of ints.
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class IntPair {
	/**
	 * The first.
	 */
	public final int first;
	/**
	 * The second.
	 */
	public final int second;

	/**
	 * Constructor.
	 *
	 * @param one the first int
	 * @param two the second int.
	 */
	public IntPair(final int one, final int two) {
		first = one;
		second = two;
	}

	/**
	 * @param one a first int
	 * @param two a second int
	 * @return a pair of those ints
	 */
	public static IntPair of(final int one, final int two) { // NOPMD
		return new IntPair(one, two);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(15);
		builder.append('(');
		builder.append(first);
		builder.append(", ");
		builder.append(second);
		builder.append(')');
		return NullCleaner.assertNotNull(builder.toString());
	}

}
