package lovelace.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link DelayedRemovalMap} for {@link Integer} keys.
 *
 * FIXME: Now we're relying on {@link AbstractMap}, this needs tests of all methods both pre-removal, post-removal, and
 * post-coalescence.
 */
public class IntMap<Item> extends AbstractMap<Integer, Item> implements DelayedRemovalMap<Integer, Item> {
	private final Map<Integer, Item> backing = new HashMap<>();
	// TODO: Use a bitmap?
	@SuppressWarnings("TypeMayBeWeakened")
	private final Set<Integer> toRemove = new HashSet<>();

	/**
	 * Add all entries in the map to the to-remove list.
	 */
	@Override
	public final void clear() {
		toRemove.addAll(backing.keySet());
	}

	/**
	 * Remove all entries in the to-remove list from the map.
	 */
	@Override
	public final void coalesce() {
		toRemove.forEach(backing::remove);
		toRemove.clear();
	}

	/**
	 * A key is in the map if it is in the backing map and is not in the to-remove list.
	 */
	@Override
	public final boolean containsKey(final Object key) {
		//noinspection SuspiciousMethodCalls This "suspicious" idiom is the point of this class
		return backing.containsKey(key) && !toRemove.contains(key);
	}

	/**
	 * If the given key is in the to-remove list, returns null; otherwise, returns the value, if any, associated
	 * with it in the backing map.
	 */
	@Override
	public final @Nullable Item get(final Object key) {
		//noinspection SuspiciousMethodCalls This "suspicious" idiom is the point of this class
		if (toRemove.contains(key)) {
			return null;
		} else {
			return backing.get(key);
		}
	}

	/**
	 * Add an entry to the map, removing the key from the to-remove list if present there.
	 */
	@Override
	public final @Nullable Item put(final Integer key, final Item item) {
		final @Nullable Item retval;
		if (toRemove.contains(key)) {
			retval = null;
			toRemove.remove(key);
			backing.put(key, item);
		} else {
			retval = backing.put(key, item);
		}
		return retval;
	}

	/**
	 * Add the given key to the to-remove list. If it was already there (the entry 'had been removed' already), or
	 * there was no value associated with that key, return null; otherwise, return the value that had been
	 * associated with the key.
	 */
	@Override
	public final @Nullable Item remove(final Object key) {
		//noinspection SuspiciousMethodCalls This "suspicious" idiom is the point of this class
		if (toRemove.contains(key)) {
			return null;
		} else if (key instanceof final Integer k && backing.containsKey(key)) {
			toRemove.add(k);
			return backing.get(key);
		} else {
			return null;
		}
	}

	@Override
	public final @NotNull Set<Entry<Integer, Item>> entrySet() {
		return backing.entrySet().stream().filter(entry -> !toRemove.contains(entry.getKey()))
				.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public final String toString() {
		return "IntMap with " + size() + " mappings";
	}
}
