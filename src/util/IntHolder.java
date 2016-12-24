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
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class IntHolder implements Accumulator {
	/**
	 * The accumulation.
	 */
	private int count;

	/**
	 * Constructor.
	 * @param initial the initial value
	 */
	public IntHolder(final int initial) {
		count = initial;
	}

	/**
	 * Add to the accumulation.
	 *
	 * @param addend how much to add
	 */
	@Override
	public void add(final int addend) {
		count += addend;
	}

	/**
	 * The current value.
	 * @return the current value of the accumulation.
	 */
	@Override
	public int getValue() {
		return count;
	}

	/**
	 * A diagnostic String.
	 * @return a diagnostic String
	 */
	@Override
	public String toString() {
		return "IntHolder: " + count;
	}
}
