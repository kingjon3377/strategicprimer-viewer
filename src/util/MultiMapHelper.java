package util;

import java.util.Map;
import java.util.function.Function;

/**
 * A class containing a method to streamline collection-of-collection operations where
 * we really want a MultiMap.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class MultiMapHelper {
	/**
	 * @param map a map
	 * @param key a key
	 * @param constructor an operation to transform the key into a new value if
	 *                       it's missing from the map
	 * @return the value in the map, or the newly added value created using the given
	 * function if the key was missing from the map
	 */
	public static <K, V> V getMapValue(final Map<K, V> map, final K key,
									   final Function<K, V> constructor) {
		if (map.containsKey(key)) {
			return map.get(key);
		} else {
			final V value = constructor.apply(key);
			map.put(key, value);
			return value;
		}
	}
}
