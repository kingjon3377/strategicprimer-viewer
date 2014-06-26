package controller.map.misc;

import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Jonathan Lovelace A wrapper around Iterator implementing Comparable,
 *         so we can put it in a Pair.
 * @param <T> the type of thing being iterated over
 */
public class ComparableIterator<T> implements Iterator<T>,
		Comparable<ComparableIterator<T>> {
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
	public int compareTo(@Nullable final ComparableIterator<T> obj) {
		if (obj == null) {
			throw new IllegalArgumentException("Compared to null");
		}
		return obj.hashCode() - hashCode();
	}

	/**
	 * @return whether there's a next element.
	 */
	@Override
	public boolean hasNext() {
		return wrapped.hasNext();
	}

	// ESCA-JAVA0277:
	/**
	 * @return the next element
	 */
	@Override
	@Nullable
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
		return this == obj || obj instanceof ComparableIterator
				&& wrapped.equals(((ComparableIterator) obj).wrapped);
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ComparableIterator";
	}
}
