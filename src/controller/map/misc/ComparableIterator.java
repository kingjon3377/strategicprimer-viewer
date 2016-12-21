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
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @param <T> the type of thing being iterated over
 * @author Jonathan Lovelace
 */
public final class ComparableIterator<@NonNull T>
		implements Comparable<@NonNull ComparableIterator<@NonNull T>>,
						   Iterator<@NonNull T> {
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
	 * Compare to another ComparableIterator.
	 * @param obj another iterator
	 * @return the result of a comparison between them.
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public int compareTo(final ComparableIterator<T> obj) {
		// TODO: Tests should cover each branch of this
		final int theirs = Objects.hashCode(obj);
		final int ours = hashCode();
		if (ours > theirs) {
			return 1;
		} else if (ours == theirs) {
			return 0;
		} else {
			return -1;
		}
	}

	/**
	 * We have a next element iff the wrapped iterator has a next element.
	 * @return whether there's a next element.
	 */
	@Override
	public boolean hasNext() {
		return wrapped.hasNext();
	}

	/**
	 * Get the next element from the wrapped iterator.
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
	 * We delegate the hash code calculation to the wrapped iterator.
	 * @return the hash code of the wrapped iterator.
	 */
	@Override
	public int hashCode() {
		return wrapped.hashCode();
	}

	/**
	 * A ComparableIterator is equal to an object iff that object is a
	 * ComparableIterator and that object's wrapped iterator is equal to its own.
	 * @param obj an object
	 * @return whether it equals this one
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof ComparableIterator) &&
										 wrapped.equals(
												 ((ComparableIterator) obj).wrapped));
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ComparableIterator";
	}
}
