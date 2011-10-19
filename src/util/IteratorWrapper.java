package util;

import java.util.Iterator;

/**
 * A wrapper around an iterator to let it be used in for-each loops. XML parsing
 * in particular always seems to hand me an iterator, so I normally have to use
 * hasNext() and next(), which static analysis programs always tell me I can
 * convert to for-each loops ... but until now I couldn't.
 * 
 * @author Jonathan Lovelace
 * 
 * @param <T>
 *            the type of the iterator.
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
	 * @param iterator
	 *            the iterator to wrap
	 */
	public IteratorWrapper(final Iterator<T> iterator) {
		iter = iterator;
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
		return iter.toString();
	}
}
