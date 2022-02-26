package lovelace.util;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * A utility class to serve as a concatenated view of any number of iterables.
 */
public final class ConcatIterable<T> implements Iterable<T> {
	private final List<Iterable<? extends T>> wrapped;

	@SafeVarargs
	public ConcatIterable(final Iterable<? extends T>... iterables) {
		final List<Iterable<? extends T>> temp = new ArrayList<>();
		Collections.addAll(temp, iterables);
		wrapped = Collections.unmodifiableList(temp);
	}

	@Override
	public Iterator<T> iterator() {
		return new ConcatIterator<>(wrapped);
	}

	private static class ConcatIterator<T> implements Iterator<T> {
		private final Deque<Iterator<? extends T>> wrapped = new LinkedList<>();

		public ConcatIterator(final List<Iterable<? extends T>> wrapped) {
			for (final Iterable<? extends T> it : wrapped) {
				this.wrapped.addLast(it.iterator());
			}
		}

		@Override
		public boolean hasNext() {
			return wrapped.stream().anyMatch(Iterator::hasNext);
		}

		@Override
		public T next() {
			while (!wrapped.isEmpty()) {
				final Iterator<? extends T> first = wrapped.peekFirst();
				if (first.hasNext()) {
					return first.next();
				} else {
					wrapped.removeFirst();
				}
			}
			throw new NoSuchElementException();
		}
	}
}
