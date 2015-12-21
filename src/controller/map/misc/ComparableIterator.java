package controller.map.misc;

import java.util.Iterator;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A wrapper around Iterator implementing Comparable, so we can put it in a
 * ComparablePair.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2014 Jonathan Lovelace
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
 * @param <T> the type of thing being iterated over
 * @author Jonathan Lovelace
 */
public final class ComparableIterator<@NonNull T> implements Iterator<@NonNull T>,
		                                                             Comparable<@NonNull
				                                                                        ComparableIterator<@NonNull T>> {
	/**
	 * The Iterator we're wrapping.
	 */
	private final Iterator<T> wrapped;

	/**
	 * Constructor.
	 *
	 * @param iter the Iterator to wrap.
	 */
	public ComparableIterator(final Iterator<T> iter) {
		wrapped = iter;
	}

	/**
	 * @param obj another iterator
	 * @return the result of a comparison between them.
	 */
	@Override
	public int compareTo(final ComparableIterator<T> obj) {
		return Objects.hashCode(obj) - hashCode();
	}

	/**
	 * @return whether there's a next element.
	 */
	@Override
	public boolean hasNext() {
		return wrapped.hasNext();
	}

	/**
	 * @return the next element
	 */
	@Override
	public T next() {
		return wrapped.next();
	}

	/**
	 * Remove the next element.
	 */
	@Override
	public void remove() {
		wrapped.remove();
	}

	/**
	 * @return the hash code of the wrapped iterator.
	 */
	@Override
	public int hashCode() {
		return wrapped.hashCode();
	}

	/**
	 * @param obj an object
	 * @return whether it equals this one
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof ComparableIterator)
				                         &&
				                         wrapped.equals(
						                         ((ComparableIterator) obj).wrapped));
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ComparableIterator";
	}
}
