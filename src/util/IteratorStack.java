package util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * A stack of iterators. Useful for when we have several collections of things
 * we need to do the same thing to. The order in which the iterators are
 * processed (despite "stack" in the name of this class) is unspecified and
 * implementation-specific; don't rely on it.
 * 
 * @author Jonathan Lovelace
 * 
 * @param <T>
 *            the type of thing returned by the iterator.
 */
public class IteratorStack<T> implements Iterator<T> {
	/**
	 * The queue of iterators.
	 */
	private final Queue<Iterator<T>> queue = new LinkedList<Iterator<T>>();
	/**
	 * Constructor.
	 * @param iters sources for iterators to put in the queue.
	 */
	public IteratorStack(final Iterable<T>... iters) {
		for (Iterable<T> iter : iters) {
			queue.add(iter.iterator());
		}
	}
	/**
	 * Constructor.
	 * @param iters the iterators to put in the queue.
	 */
	public IteratorStack(final Iterator<T>... iters) {
		for (Iterator<T> iter : iters) {
			queue.add(iter);
		}
	}
	/**
	 * Remove any empty iterators from the front of the queue.
	 */
	private void removeEmptyIterators() {
		while ((!queue.isEmpty()) && !(queue.peek().hasNext())) {
			queue.remove();
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
			throw new NoSuchElementException("No elements left in any of the iterators");
		} else {
			return queue.peek().next();
		}
	}
	/**
	 * Remove the next item from one of the iterators.
	 */
	@Override
	public void remove() {
		removeEmptyIterators();
		if (queue.isEmpty()) {
			throw new NoSuchElementException("No elements left in any of the iterators");
		} else {
			queue.peek().remove();
		}
		removeEmptyIterators();
	}
}
