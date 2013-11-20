package util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An empty iterator.
 * @param <T> the type parameter
 * @author Jonathan Lovelace
 *
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
				"Nothing to remove in an empty iterator");
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "EmptyIterator";
	}
}