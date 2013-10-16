package util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A wrapper around an iterator to let it be used in for-each loops. XML parsing
 * in particular always seems to hand me an iterator, so I normally have to use
 * hasNext() and next(), which static analysis programs always tell me I can
 * convert to for-each loops ... but until now I couldn't.
 *
 * @author Jonathan Lovelace
 *
 * @param <T> the type of the iterator.
 *
 */
public class IteratorWrapper<T> implements Iterable<T> {
	/**
	 * The iterator we're wrapping.
	 */
	private final Iterator<T> iter;

	/**
	 * Constructor.
	 *
	 * @param iterator the iterator to wrap
	 */
	public IteratorWrapper(@Nullable final Iterator<T> iterator) {
		if (iterator == null) {
			iter = new Iterator<T>() {
				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public T next() {
					throw new NoSuchElementException(
							"Iterator substituted for null is empty");
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException(
							"Iterator substituted for null doesn't support remove()");
				}
			};
		} else {
			iter = iterator;
		}
	}

	/**
	 * Constructor to produce a *sorted* Iterable. Unlike normal operation, this
	 * evaluates its arguments greedily.
	 *
	 * @param iterator the iterator to get elements from.
	 * @param comparator the comparator to use for sorting elements.
	 */
	public IteratorWrapper(final Iterator<T> iterator,
			final Comparator<T> comparator) {
		final PriorityQueue<T> queue = new PriorityQueue<>(1, comparator);
		while (iterator.hasNext()) {
			queue.add(iterator.next());
		}
		final Iterator<T> qIterator = queue.iterator();
		if (qIterator == null) {
			throw new IllegalStateException(
					"Somehow the PriorityQueue's iterator was null");
		} else {
			iter = qIterator;
		}
	}

	/**
	 *
	 * @return the iterator
	 */
	@Override
	public Iterator<T> iterator() {
		return iter;
	}

	/**
	 *
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		final String retval = iter.toString();
		return retval == null ? "IteratorWrapper" : retval;
	}
}
