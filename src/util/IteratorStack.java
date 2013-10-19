package util;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A stack of iterators. Useful for when we have several collections of things
 * we need to do the same thing to. The order in which the iterators are
 * processed (despite "stack" in the name of this class) is unspecified and
 * implementation-specific; don't rely on it.
 *
 * @author Jonathan Lovelace
 *
 * @param <T> the type of thing returned by the iterator.
 */
public class IteratorStack<T> implements Iterator<T> {
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
			queue.addFirst(iter.iterator());
		}
		removeEmptyIterators();
	}

	/**
	 * Constructor.
	 *
	 * @param iters the iterators to put in the queue.
	 */
	@SafeVarargs
	public IteratorStack(final Iterator<T>... iters) {
		for (final Iterator<T> iter : iters) {
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
	@Nullable
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
		}
		removeEmptyIterators();
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "IteratorStack";
	}
}
