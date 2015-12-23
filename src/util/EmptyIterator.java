package util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An empty iterator.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2013 Jonathan Lovelace
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
 * @param <T> the type parameter
 * @author Jonathan Lovelace
 */
public final class EmptyIterator<T> implements Iterator<T> {
	/**
	 * @return false
	 */
	@Override
	public boolean hasNext() {
		return false;
	}

	/**
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
		throw new UnsupportedOperationException(
				                                       "Nothing to remove in an empty " +
						                                       "iterator");
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "EmptyIterator";
	}
}
