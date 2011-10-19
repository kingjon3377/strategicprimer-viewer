package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class to encapsulate our not-in-place List sorting method.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class Sorter {
	/**
	 * Do not instantiate.
	 */
	private Sorter() {
		// Do nothing.
	}

	/**
	 * Sort a list, returning the sorted list but not modifying the parameter.
	 * 
	 * 
	 * @param list
	 *            A list
	 * 
	 * @return A sorted version of the list
	 */
	public static <T extends Comparable<T>> List<T> sort(
			final List<? extends T> list) {
		final List<T> newList = new ArrayList<T>(list);
		Collections.sort(newList);
		return newList;
	}
}
