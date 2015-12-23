package util;

import java.util.Iterator;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A class to convert a map's Entry set to an iterable of Pairs.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @param <I> the first parameter of the map
 * @param <K> the second parameter of the map.
 * @author Jonathan Lovelace
 */
public final class SetPairConverter<@NonNull I, @NonNull K>
		implements Iterable<Pair<I, K>> {
	/**
	 * The map.
	 */
	private final Map<I, K> theMap;

	/**
	 * @param map the set we're wrapping.
	 */
	@SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
	public SetPairConverter(final Map<I, K> map) {
		theMap = map;
	}

	/**
	 * @return the iterator
	 */

	@Override
	public Iterator<Pair<I, K>> iterator() {
		return new IteratorImpl<>(theMap.entrySet().iterator());
	}

	/**
	 * The class that does most of the work.
	 *
	 * @param <I> the first parameter of the map
	 * @param <K> the second parameter of the map
	 * @author Jonathan Lovelace
	 */
	private static final class IteratorImpl<@NonNull I, @NonNull K>
			implements Iterator<@NonNull Pair<I, K>> {
		/**
		 * The object we're a wrapper around.
		 */
		private final Iterator<Map.Entry<I, K>> wrapped;

		/**
		 * @return a String representation of the object
		 */
		@SuppressWarnings("MethodReturnAlwaysConstant")
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

		/**
		 * @return the next pair in line
		 */
		@Override
		public Pair<I, K> next() {
			final Map.Entry<I, K> entry = wrapped.next();
			return Pair.of(entry.getKey(), entry.getValue());
		}

		/**
		 * Remove the current item in the iterator.
		 */
		@Override
		public void remove() {
			wrapped.remove();
		}

	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "SetPairConverter";
	}
}
