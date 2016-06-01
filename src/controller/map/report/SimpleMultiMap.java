package controller.map.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A simple implementation of the "multimap" idiom: a map from a key type to a *list*
 * of items of a value type, that always returns some list (which is then stored, so it
 * doesn't have to be added again) for every key query.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
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
 * @author Jonathan Lovelace
 */
public class SimpleMultiMap<K, V> implements Map<K, Collection<V>> {
	/**
	 * The map to which we delegate most of the implementation.
	 */
	private final Map<K, Collection<V>> delegate = new HashMap<>();
	/**
	 * @return the size of the map
	 */
	@Override
	public int size() {
		return delegate.size();
	}

	/**
	 * @return whether the map is empty
	 */
	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	/**
	 * @param key a key object
	 * @return whether the map contains any values for that key
	 */
	@Override
	public boolean containsKey(final Object key) {
		return delegate.containsKey(key);
	}

	/**
	 * @param value a value object
	 * @return whether the map contains this as a value for any key (thus this only
	 * returns true if it is a collection)
	 */
	@Override
	public boolean containsValue(final Object value) {
		return delegate.containsValue(value);
	}

	/**
	 * @param key a key object
	 * @return a list of values for that key, if it is of type K.
	 * @throws ClassCastException if it is not a valid key
	 */
	@Override
	public Collection<V> get(final Object key) {
		if (containsKey(key)) {
			return delegate.get(key);
		} else {
			final Collection<V> retval = new ArrayList<>();
			delegate.put((K) key, retval);
			return retval;
		}
	}

	/**
	 * @param key a key
	 * @param value a collection of values
	 * @return the collection that was replaced
	 */
	@Override
	public Collection<V> put(final K key, final Collection<V> value) {
		return delegate.put(key, value);
	}

	/**
	 * @param key a key
	 * @return the collection that was at that key, after removing it
	 */
	@Override
	public Collection<V> remove(final Object key) {
		return delegate.remove(key);
	}

	/**
	 * @param m a type-similar map to this one, all of whose collection-values should be
	 *             set in this one
	 */
	@Override
	public void putAll(final Map<? extends K, ? extends Collection<V>> m) {
		delegate.putAll(m);
	}

	/**
	 * Clear the map.
	 */
	@Override
	public void clear() {
		delegate.clear();
	}

	/**
	 * @return the set of keys in the map
	 */
	@Override
	public Set<K> keySet() {
		return delegate.keySet();
	}

	/**
	 * @return the collection of (collections of) values
	 */
	@Override
	public Collection<Collection<V>> values() {
		return delegate.values();
	}

	/**
	 * @return the set of key-(collection-of-)value pairs in the map
	 */
	@Override
	public Set<Entry<K, Collection<V>>> entrySet() {
		return delegate.entrySet();
	}
}
