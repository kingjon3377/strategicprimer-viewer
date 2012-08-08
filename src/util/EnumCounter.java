package util;

import java.util.EnumMap;
import java.util.Map;

/**
 * A class to count instances of enums.
 * 
 * @author Jonathan Lovelace
 * 
 * @param <T> the enumerated type we're counting
 */
public class EnumCounter<T extends Enum<T>> {
	/**
	 * Constructor.
	 * 
	 * @param type the enumerated type we're dealing with
	 */
	public EnumCounter(final Class<T> type) {
		map = new EnumMap<T, Integer>(type);
	}

	/**
	 * The map we use as a backend.
	 */
	private final Map<T, Integer> map;

	/**
	 * @param value a value to increase the counter for
	 */
	public void count(final T value) {
		if (map.containsKey(value)) {
			map.put(value, Integer.valueOf(map.get(value).intValue() + 1));
		} else {
			map.put(value, Integer.valueOf(1));
		}
	}

	/**
	 * @param values a sequence of values to count.
	 */
	public void countMany(final T... values) {
		for (final T value : values) {
			count(value);
		}
	}

	/**
	 * @param value a value we want a count for
	 * @return the count for the specified value
	 */
	public int getCount(final T value) {
		if (map.containsKey(value)) {
			return map.get(value).intValue(); // NOPMD
		} else {
			return 0;
		}
	}
}
