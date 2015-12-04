package util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A wrapper around an Enumeration to make it fit the Iterable interface.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2015-2015 Jonathan Lovelace
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
 * @param <T> the type parameter
 * @author Jonathan Lovelace
 */
public final class EnumerationWrapper<T> implements Iterator<T> {
	/**
	 * The object we're wrapping.
	 */
	private final Enumeration<T> wrapped;

	/**
	 * @param enumer the object we're wrapping.
	 */
	public EnumerationWrapper(@Nullable final Enumeration<T> enumer) {
		if (enumer == null) {
			wrapped = new Enumeration<T>() {
				@Override
				public boolean hasMoreElements() {
					return false;
				}

				@Override
				public T nextElement() {
					throw new NoSuchElementException(
							"No elements in empty enumeration (replacing null)");
				}
			};
		} else {
			wrapped = enumer;
		}
	}

	/**
	 * @return whether there are more elements
	 */
	@Override
	public boolean hasNext() {
		return wrapped.hasMoreElements();
	}

	/**
	 * @return the next element
	 * @throws NoSuchElementException if no more elements
	 */
	// ESCA-JAVA0126:
	// ESCA-JAVA0277:
	@Override
	public T next() throws NoSuchElementException { // NOPMD: throws clause
													// required by
													// superclass
		return wrapped.nextElement();
	}

	/**
	 * Not supported.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"Not supported by Enumeration");
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "EnumerationWrapper";
	}
}
