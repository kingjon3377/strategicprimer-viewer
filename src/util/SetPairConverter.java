package util;

import java.util.Iterator;
import java.util.Map;

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
	 * The map.
	 */
	private final Map<I, K> theMap;

	/**
	 * @param map the set we're wrapping.
	 */
	public SetPairConverter(final Map<I, K> map) {
		theMap = map;
	}

	/**
	 * @return the iterator
	 */

	@Override
	public Iterator<Pair<I, K>> iterator() {
		return new IteratorImpl<>(NullCleaner.assertNotNull(theMap.entrySet()
				.iterator()));
	}

	/**
	 * The class that does most of the work.
	 * @param <I> the first parameter of the map
	 * @param <K> the second parameter of the map
	 * @author Jonathan Lovelace
	 */
	private static class IteratorImpl<I, K> implements Iterator<Pair<I, K>> {
		/**
		 * The object we're a wrapper around.
		 */
		private final Iterator<Map.Entry<I, K>> wrapped;

		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "SetPairConverter#IteratorImpl";
		}
		/**
		 * Constructor.
		 *
		 * @param iter the iterator we're a wrapper around.
		 */
		protected IteratorImpl(final Iterator<Map.Entry<I, K>> iter) {
			wrapped = iter;
		}

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
			return Pair.of(NullCleaner.assertNotNull(entry.getKey()),
					NullCleaner.assertNotNull(entry.getValue()));
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			wrapped.remove();
		}

	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SetPairConverter";
	}
}
