package util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An empty iterator.
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
 * @param <T> the type parameter
 * @author Jonathan Lovelace
 */
public final class EmptyIterator<T> implements Iterator<T> {
	/**
	 * Always returns false, as befits an empty iterator.
	 * @return false
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public boolean hasNext() {
		return false;
	}

	/**
	 * Always throws, as befits an empty iterator.
	 * @return nothing
	 */
	@Override
	public T next() {
		throw new NoSuchElementException("No elements in an empty iterator");
	}

	/**
	 * Always throws.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Nothing to remove in an empty " +
														"iterator");
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "EmptyIterator";
	}
}
