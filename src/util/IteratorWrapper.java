package util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.PriorityQueue;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A wrapper around an iterator to let it be used in for-each loops. XML parsing in
 * particular always seems to hand me an iterator, so I normally have to use hasNext() and
 * next(), which static analysis programs always tell me I can convert to for-each loops
 * ... but until now I couldn't.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @param <T> the type of the iterator.
 * @author Jonathan Lovelace
 */
public final class IteratorWrapper<@NonNull T> implements Iterable<@NonNull T> {
	/**
	 * The iterator we're wrapping.
	 */
	private final Iterator<T> iter;

	/**
	 * Constructor.
	 *
	 * @param iterator the iterator to wrap
	 */
	public IteratorWrapper(@Nullable final Iterator<@NonNull T> iterator) {
		if (iterator == null) {
			iter = Collections.emptyIterator();
		} else {
			iter = iterator;
		}
	}

	/**
	 * Constructor to produce a *sorted* Iterable. Unlike normal operation, this
	 * evaluates
	 * its arguments greedily.
	 *
	 * @param iterator   the iterator to get elements from.
	 * @param comparator the comparator to use for sorting elements.
	 */
	public IteratorWrapper(final Iterator<T> iterator,
						   final Comparator<T> comparator) {
		final PriorityQueue<T> queue = new PriorityQueue<>(1, comparator);
		while (iterator.hasNext()) {
			queue.add(iterator.next());
		}
		iter = queue.iterator();
	}

	/**
	 * @return the iterator
	 */
	@Override
	public Iterator<T> iterator() {
		return iter;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return Objects.toString(iter);
	}
}
