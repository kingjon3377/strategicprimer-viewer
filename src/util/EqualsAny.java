package util;

import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A class to hold a utility method for comparing a value with a number of other values.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("UtilityClassCanBeEnum")
public final class EqualsAny {
	/**
	 * Do not instantiate.
	 */
	private EqualsAny() {
		// Do nothing.
	}

	/**
	 * Compare a value with a number of other (generally constant) values.
	 *
	 * @param <T>  the type of objects we'll be comparing
	 * @param against the value to compare the others to
	 * @param values  the values to compare to it
	 * @return true if any of theme equal it, false otherwise.
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@SafeVarargs
	public static <T> boolean equalsAny(final T against,
										final T... values) {
		return Stream.of(values).anyMatch(val -> Objects.equals(against, val));
	}

	/**
	 * Compare a value with a collection of other (generally constant) values.
	 *
	 * @param <T>  the type of objects we'll be comparing
	 * @param against the value to compare the others to
	 * @param values  the values to compare to it. May be null, in which case we return
	 *                false.
	 * @return true if any of theme equal it, false otherwise.
	 */
	public static <T> boolean equalsAny(final T against,
										@Nullable final Iterable<T> values) {
		if (values == null) {
			return false;
		} else {
			return StreamSupport.stream(values.spliterator(), false)
						.anyMatch(val -> Objects.equals(against, val));
		}
	}
}
