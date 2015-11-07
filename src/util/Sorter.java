package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class to encapsulate our not-in-place List sorting method.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2013 Jonathan Lovelace
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
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
	 * @param <T> the type of list
	 * @param list A list
	 * @return A sorted version of the list
	 */
	public static <T extends Comparable<T>> List<T> sort(
			final List<? extends T> list) {
		final List<T> newList = new ArrayList<>(list);
		Collections.sort(newList);
		return newList;
	}
}
