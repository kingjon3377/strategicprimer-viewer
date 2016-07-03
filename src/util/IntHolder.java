package util;

/**
 * A class to hold a mutable value, accept modifications to it, and report
 * its current value. This is necessary to work around the fact that any local
 * variable referenced in a lambda expression must be final or effectively final.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class IntHolder implements Accumulator {
	/**
	 * The accumulation.
	 */
	private int count;
	/**
	 * @param initial the initial value
	 */
	public IntHolder(final int initial) {
		count = initial;
	}
	/**
	 * Add to the accumulation.
	 * @param addend how much to add
	 */
	@Override
	public void add(int addend) {
		count += addend;
	}
	/**
	 * @return the current value of the accumulation.
	 */
	@Override
	public int getValue() {
		return count;
	}
}
