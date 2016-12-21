package util;

import java.util.EnumMap;
import java.util.Map;

/**
 * A class to count instances of enums.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @param <T> the enumerated type we're counting
 * @author Jonathan Lovelace
 */
public final class EnumCounter<T extends Enum<T>> {
	/**
	 * The map we use as a backend. TODO: Use Accumulator, not Integer
	 */
	private final Map<T, Integer> map;

	/**
	 * Constructor.
	 *
	 * @param type the enumerated type we're dealing with
	 */
	public EnumCounter(final Class<T> type) {
		map = new EnumMap<>(type);
	}

	/**
	 * @param value a value to increase the counter for
	 */
	private void count(final T value) {
		if (map.containsKey(value)) {
			map.put(value, Integer.valueOf(map.get(value).intValue() + 1));
		} else {
			map.put(value, Integer.valueOf(1));
		}
	}

	/**
	 * @param values a sequence of values to count.
	 */
	@SafeVarargs
	public final void countMany(final T... values) {
		for (final T value : values) {
			if (value != null) {
				count(value);
			}
		}
	}

	/**
	 * @param value a value we want a count for
	 * @return the count for the specified value
	 */
	public int getCount(final T value) {
		if (map.containsKey(value)) {
			return map.get(value).intValue();
		} else {
			return 0;
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "EnumCounter";
	}
}
