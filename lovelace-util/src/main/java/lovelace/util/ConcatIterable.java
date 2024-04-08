package lovelace.util;

import java.util.Iterator;
import java.util.List;
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
		wrapped = List.of(iterables);
	}

	@Override
	public Iterator<T> iterator() {
		return new ConcatIterator<>(wrapped);
	}

	@Override
	public String toString() {
		return "ConcatIterable wrapping " + wrapped.size() + " iterables";
	}

	private static final class ConcatIterator<T> implements Iterator<T> {
		private final Deque<Iterator<? extends T>> wrapped = new LinkedList<>();

		public ConcatIterator(final Iterable<? extends Iterable<? extends T>> wrapped) {
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
			throw new NoSuchElementException("All iterators exhausted.");
		}

		@Override
		public String toString() {
			return "ConcatIterator wrapping " + wrapped.size() + " iterators";
		}
	}
}
