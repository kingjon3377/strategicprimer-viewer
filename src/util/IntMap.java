package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A simplified Map using Integers as keys and delaying removal of items
 * remove()d until a subsequent method is called.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 * @param <V> the kind of thing stored in the map.
 */
public class IntMap<V> implements DelayedRemovalMap<@NonNull Integer, V> { // NOPMD
	/**
	 * The map that we use as a backing store.
	 */
	private final Map<@NonNull Integer, V> backing = new HashMap<>();
	/**
	 * The list of items to remove when we're told to remove them.
	 */
	private final List<Integer> toRemove = new ArrayList<>();

	/**
	 * @return the size of the map
	 */
	@Override
	public int size() {
		return backing.size();
	}

	/**
	 * @return whether the map is empty
	 */
	@Override
	public boolean isEmpty() {
		return backing.isEmpty();
	}

	/**
	 * @param key a key
	 * @return whether the map contains it
	 */
	@Override
	public boolean containsKey(@Nullable final Object key) {
		return backing.containsKey(key);
	}

	/**
	 * @param value a value
	 * @return whether the map contains it
	 */
	@Override
	public boolean containsValue(@Nullable final Object value) {
		return backing.containsValue(value);
	}

	/**
	 * @param key a key
	 * @return the corresponding value in the map, if it exists
	 */
	@Override
	public V get(@Nullable final Object key) {
		return backing.get(key);
	}

	/**
	 * @param key a key
	 * @param value a value
	 * @return the result of putting the value into the map at the key
	 */
	@Override
	public V put(final Integer key, final V value) {
		return backing.put(key, value);
	}

	/**
	 * *Schedules* the item to be removed; doesn't remove it yet!
	 *
	 * @param key a key
	 * @return false, as it isn't actually removed yet.
	 */
	@Override
	public V remove(@Nullable final Object key) {
		if (key instanceof Integer) {
			toRemove.add((Integer) key);
		}
		return get(key);
	}

	/**
	 * Apply all scheduled removals.
	 */
	@Override
	public void coalesce() {
		synchronized (toRemove) {
			for (final Integer num : toRemove) {
				backing.remove(num);
			}
			toRemove.clear();
		}
	}

	/**
	 * Put all members of another map into the map.
	 *
	 * @param map the map to insert
	 */
	@Override
	public void putAll(@Nullable final Map<? extends Integer, ? extends V> map) {
		if (map != null) {
			backing.putAll(map);
		}
	}

	/**
	 * Schedules all keys for removal.
	 */
	@Override
	public void clear() {
		toRemove.addAll(keySet());
	}

	/**
	 * @return the key set
	 */
	@Override
	public Set<Integer> keySet() {
		return NullCleaner.assertNotNull(backing.keySet());
	}

	/**
	 * @return the collection of values
	 */
	@Override
	public Collection<V> values() {
		return NullCleaner.assertNotNull(backing.values());
	}

	/**
	 * @return the set of entries in the map
	 */
	@SuppressWarnings("null") // There seems no way to even get it to say what constraints don't match!
	@Override
	@NonNull
	public Set<Map.Entry<@NonNull Integer, V>> entrySet() {
		return backing.entrySet();
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return NullCleaner.assertNotNull(backing.toString());
	}
}
