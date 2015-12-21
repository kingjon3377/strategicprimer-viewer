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
	 * @param firstInt the first int
	 * @param secondInt the second int.
	 */
	public IntPair(final int firstInt, final int secondInt) {
		first = firstInt;
		second = secondInt;
	}

	/**
	 * @param firstInt a first int
	 * @param secondInt a second int
	 * @return a pair of those ints
	 */
	public static IntPair of(final int firstInt, final int secondInt) { // NOPMD
		return new IntPair(firstInt, secondInt);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return String.format("(%d, %d)", Integer.valueOf(first), Integer.valueOf(second));
	}

}
