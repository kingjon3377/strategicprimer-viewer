package util;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A class to hold a utility method for comparing a value with a number of other
 * values.
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
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
	 * @param <TYPE> the type of objects we'll be comparing
	 * @param against the value to compare the others to
	 * @param values the values to compare to it
	 *
	 * @return true if any of theme equal it, false otherwise.
	 */
	@SafeVarargs
	public static <TYPE> boolean equalsAny(final TYPE against,
			final TYPE... values) {
		for (final TYPE value : values) {
			if (against.equals(value)) {
				return true; // NOPMD
			}
		}
		return false;
	}

	/**
	 * Compare a value with a collection of other (generally constant) values.
	 *
	 * @param <TYPE> the type of objects we'll be comparing
	 * @param against the value to compare the others to
	 * @param values the values to compare to it. May be null, in which case we
	 *        return false.
	 *
	 * @return true if any of theme equal it, false otherwise.
	 */
	public static <TYPE> boolean equalsAny(final TYPE against,
			@Nullable final Iterable<TYPE> values) {
		if (values == null) {
			return false; // NOPMD
		} else {
			for (final TYPE value : values) {
				if (against.equals(value)) {
					return true; // NOPMD
				}
			}
			return false;
		}
	}
}
