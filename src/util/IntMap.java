package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simplified Map using Integers as keys and delaying removal of items
 * remove()d until a subsequent method is called.
 *
 * @author Jonathan Lovelace
 * @param <V> the kind of thing stored in the map.
 */
public class IntMap<V> implements Map<Integer, V> { // NOPMD
	/**
	 * The map that we use as a backing store.
	 */
	private final Map<Integer, V> backing = new HashMap<Integer, V>();
	/**
	 * The list of items to remove when we're told to remove them.
	 */
	private final List<Integer> toRemove = new ArrayList<Integer>();
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
	public boolean containsKey(final Object key) {
		return backing.containsKey(key);
	}
	/**
	 * @param value a value
	 * @return whether the map contains it
	 */
	@Override
	public boolean containsValue(final Object value) {
		return backing.containsValue(value);
	}
	/**
	 * @param key a key
	 * @return the corresponding value in the map, if it exists
	 */
	@Override
	public V get(final Object key) {
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
	 * @param key a key
	 * @return false, as it isn't actually removed yet.
	 */
	@Override
	public V remove(final Object key) {
		if (key instanceof Integer) {
			toRemove.add((Integer) key);
		}
		return get(key);
	}
	/**
	 * Apply all scheduled removals.
	 */
	public void coalesce() {
		synchronized (toRemove) {
			for (Integer num : toRemove) {
				backing.remove(num);
			}
			toRemove.clear();
		}
	}
	/**
	 * Put all members of another map into the map.
	 * @param map the map to insert
	 */
	@Override
	public void putAll(final Map<? extends Integer, ? extends V> map) {
		backing.putAll(map);
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
		return backing.keySet();
	}
	/**
	 * @return the collection of values
	 */
	@Override
	public Collection<V> values() {
		return backing.values();
	}
	/**
	 * @return the set of entries in the map
	 */
	@Override
	public Set<java.util.Map.Entry<Integer, V>> entrySet() {
		return backing.entrySet();
	}
}
