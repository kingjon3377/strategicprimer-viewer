package lovelace.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;

/**
 * A stream of all the types that a given object satisfies.
 */
public final class TypeStream implements Iterable<Class<?>> {
	private final Object obj;
	private final List<Class<?>> cache = new LinkedList<>();

	public TypeStream(final Object obj) {
		this.obj = obj;
	}

	@Override
	public Iterator<Class<?>> iterator() {
		return new TypeIterator(obj, cache);
	}

	@Override
	public String toString() {
		return "TypeStream for " + obj;
	}

	private static final class TypeIterator implements Iterator<Class<?>> {
		private final LinkedList<Class<?>> ourCopy;
		private final List<Class<?>> cache;
		private final Collection<Class<?>> classes = new HashSet<>();
		private final Queue<Class<?>> queue = new LinkedList<>();
		private final String str;

		public TypeIterator(final Object obj, final List<Class<?>> cache) {
			this.cache = cache;
			ourCopy = new LinkedList<>(cache);
			queue.add(obj.getClass());
			str = "TypeStream iterator for: " + obj;
		}

		@Override
		public boolean hasNext() {
			return (!ourCopy.isEmpty()) || (!queue.isEmpty());
		}

		@Override
		public Class<?> next() {
			if (!ourCopy.isEmpty()) {
				return ourCopy.remove();
			}
			while (!queue.isEmpty()) {
				final Class<?> item = queue.remove();
				if (!classes.contains(item)) {
					classes.add(item);
					final Class<?> superclass = item.getSuperclass();
					if (Objects.nonNull(superclass)) {
						queue.add(superclass);
					}
					queue.addAll(Arrays.asList(item.getInterfaces()));
					cache.add(item);
					return item;
				}
			}
			throw new NoSuchElementException("No more supertypes");
		}

		@Override
		public String toString() {
			return str;
		}
	}
}
