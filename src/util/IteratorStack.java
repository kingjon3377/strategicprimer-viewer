package util;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A stack of iterators. Useful for when we have several collections of things
 * we need to do the same thing to. The order in which the iterators are
 * processed (despite "stack" in the name of this class) is unspecified and
 * implementation-specific; don't rely on it.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2013 Jonathan Lovelace
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
 * @param <T> the type of thing returned by the iterator.
 */
public final class IteratorStack<T> implements Iterator<T> {
	/**
	 * The queue of iterators.
	 */
	private final Deque<Iterator<T>> queue = new LinkedList<>();

	/**
	 * Constructor.
	 *
	 * @param iters sources for iterators to put in the queue.
	 */
	@SafeVarargs
	public IteratorStack(final Iterable<T>... iters) {
		for (final Iterable<T> iter : iters) {
			final Iterator<T> iterator = iter.iterator();
			if (iterator != null) {
				queue.addFirst(iterator);
			}
		}
		removeEmptyIterators();
	}

	/**
	 * Constructor.
	 *
	 * @param iters the iterators to put in the queue.
	 */
	@SafeVarargs
	public IteratorStack(final @NonNull Iterator<@NonNull T> @NonNull ... iters) {
		for (final Iterator<@NonNull T> iter : iters) {
			queue.addFirst(iter);
		}
		removeEmptyIterators();
	}

	/**
	 * Remove any empty iterators from the front of the queue.
	 */
	private void removeEmptyIterators() {
		while (!queue.isEmpty() && !queue.peekFirst().hasNext()) {
			queue.removeFirst();
		}
	}

	/**
	 * @return whether any of the iterators in the stack has any left.
	 */
	@Override
	public boolean hasNext() {
		removeEmptyIterators();
		return queue.isEmpty();
	}

	/**
	 * @return the next item in one of the iterators
	 */
	@Override
	public T next() {
		removeEmptyIterators();
		if (queue.isEmpty()) {
			throw new NoSuchElementException(
					"No elements left in any of the iterators");
		} else {
			final T retval = queue.peekFirst().next();
			removeEmptyIterators();
			return retval;
		}
	}

	/**
	 * Remove the next item from one of the iterators.
	 */
	@Override
	public void remove() {
		removeEmptyIterators();
		if (queue.isEmpty()) {
			throw new NoSuchElementException(
					"No elements left in any of the iterators");
		} else {
			queue.peekFirst().remove();
			removeEmptyIterators();
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "IteratorStack";
	}
}
