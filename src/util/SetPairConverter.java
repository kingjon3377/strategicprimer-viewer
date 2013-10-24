package util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A class to convert a map's Entry set to an iterable of Pairs.
 *
 * @author Jonathan Lovelace
 *
 * @param <I> the first parameter of the map
 * @param <K> the second parameter of the map.
 */
public class SetPairConverter<I, K> implements Iterable<Pair<I, K>> {
	/**
	 * @param map the set we're wrapping.
	 */
	public SetPairConverter(final Map<I, K> map) {
		theMap = map;
	}

	/**
	 * The map.
	 */
	private final Map<I, K> theMap;

	/**
	 * @return the iterator
	 */

	@Override
	public Iterator<Pair<I, K>> iterator() {
		final Iterator<Entry<I, K>> iter = theMap.entrySet().iterator();
		assert iter != null;
		return new IteratorImpl<>(iter);
	}

	/**
	 * The class that does most of the work.
	 * @param <I> the first parameter of the map
	 * @param <K> the second parameter of the map
	 * @author Jonathan Lovelace
	 */
	private static class IteratorImpl<I, K> implements Iterator<Pair<I, K>> {
		/**
		 * Constructor.
		 *
		 * @param iter the iterator we're a wrapper around.
		 */
		IteratorImpl(final Iterator<Map.Entry<I, K>> iter) {
			wrapped = iter;
		}

		/**
		 * The object we're a wrapper around.
		 */
		private final Iterator<Map.Entry<I, K>> wrapped;

		/**
		 * @return whether there's more in the iterator
		 */
		@Override
		public boolean hasNext() {
			return wrapped.hasNext();
		}

		// ESCA-JAVA0277:
		/**
		 * @return the next pair in line
		 */
		@Override
		public Pair<I, K> next() {
			final Map.Entry<I, K> entry = wrapped.next();
			final I key = entry.getKey();
			final K value = entry.getValue();
			assert key != null && value != null;
			return Pair.of(key, value);
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			wrapped.remove();
		}

	}
}
