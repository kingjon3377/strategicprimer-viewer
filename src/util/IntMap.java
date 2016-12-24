package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A simplified Map using Integers as keys and delaying removal of items remove()d until a
 * subsequent method is called.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @param <V> the kind of thing stored in the map.
 * @author Jonathan Lovelace
 */
public final class IntMap<V> implements PatientMap<@NonNull Integer, V> {
	/**
	 * The map that we use as a backing store.
	 */
	private final Map<@NonNull Integer, V> backing = new HashMap<>();
	/**
	 * The list of items to remove when we're told to remove them.
	 */
	private final Collection<@NonNull Integer> toRemove = new ArrayList<>();

	/**
	 * The size of the map.
	 * @return the size of the map
	 */
	@Override
	public int size() {
		return backing.size();
	}

	/**
	 * Whether the map is empty.
	 * @return whether the map is empty
	 */
	@Override
	public boolean isEmpty() {
		return backing.isEmpty();
	}

	/**
	 * Whether the map contains the given key.
	 * @param key a key
	 * @return whether the map contains it
	 */
	@Override
	public boolean containsKey(@Nullable final Object key) {
		return backing.containsKey(key);
	}

	/**
	 * Whether the map contains the given value.
	 * @param value a value
	 * @return whether the map contains it
	 */
	@Override
	public boolean containsValue(@Nullable final Object value) {
		return backing.containsValue(value);
	}

	/**
	 * Get the value for the given key.
	 * @param key a key
	 * @return the corresponding value in the map, if it exists
	 */
	@Override
	@Nullable
	public V get(@Nullable final Object key) {
		return backing.get(key);
	}

	/**
	 * Put the given value in the map for the given key.
	 * @param key   a key
	 * @param value a value
	 * @return the result of putting the value into the map at the key
	 */
	@Override
	public V put(final Integer key, final V value) {
		return backing.put(key, value);
	}

	/**
	 * Schedule the given key to be removed.
	 * *Schedules* the item to be removed; doesn't remove it yet!
	 *
	 * @param key a key
	 * @return false, as it isn't actually removed yet.
	 */
	@Override
	@Nullable
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
			toRemove.forEach(backing::remove);
			toRemove.clear();
		}
	}

	/**
	 * Put all members of another map into the map.
	 *
	 * @param map the map to insert
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public void putAll(@Nullable final Map<? extends Integer, ? extends V> map) {
		//noinspection ConstantConditions
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
	 * The key set.
	 * @return the key set
	 */
	@Override
	public Set<Integer> keySet() {
		return backing.keySet();
	}

	/**
	 * The value set.
	 * @return the collection of values
	 */
	@Override
	public Collection<V> values() {
		return backing.values();
	}

	/**
	 * The entry set.
	 * @return the set of entries in the map
	 */
	@Override
	public Set<Map.Entry<@NonNull Integer, V>> entrySet() {
		return backing.entrySet();
	}

	/**
	 * The String representation of the map.
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return backing.toString();
	}
}
