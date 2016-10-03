package util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A utility class containing a method that turns any Iterable into a List.
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
public final class ListMaker {
	/**
	 * No need to instantiate.
	 */
	private ListMaker() {
		// No need to instantiate.
	}
	/**
	 * Turn an Iterable into a List. This is, of course, an eager implementation; make
	 * sure not to use on anything with an infinite iterator!
	 *
	 * @param <T>  the type contained in the iterable.
	 * @param iter the thing to iterate over
	 * @return a List representing the same data.
	 */
	public static <T> List<T> toList(final Iterable<T> iter) {
		return StreamSupport.stream(iter.spliterator(), false)
					   .collect(Collectors.toList());
	}
}