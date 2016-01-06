package util;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A pair of Comparables.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @param <K> the type of the first item
 * @param <V> the type of the second item
 * @author Jonathan Lovelace
 * @see Pair
 */
@SuppressWarnings("EqualsAndHashcode")
public final class ComparablePair<K extends Comparable<K>, V extends Comparable<V>>
		extends Pair<K, V> implements Comparable<@NonNull ComparablePair<K, V>> {
	/**
	 * Constructor.
	 *
	 * @param firstItem  The first item in the pair.
	 * @param secondItem The second item in the pair.
	 */
	private ComparablePair(final K firstItem, final V secondItem) {
		super(firstItem, secondItem);
	}

	/**
	 * Compare to another pair.
	 *
	 * @param other the other pair
	 * @return the result of the comparison.
	 */
	@Override
	public int compareTo(final ComparablePair<K, V> other) {
		final int cmp = first().compareTo(other.first());
		if (cmp == 0) {
			return second().compareTo(other.second()); // NOPMD
		} else {
			return cmp;
		}
	}

	/**
	 * Create a pair without having to specify the types.
	 *
	 * @param <K>    The type of the first element in the pair
	 * @param <V>    The type of the second element in the pair
	 * @param first  The first element in the pair.
	 * @param second The second element in the pair.
	 * @return a pair containing the two elements
	 */
	public static <K extends Comparable<K>, V extends Comparable<V>>
	ComparablePair<K, V> of(final K first, final V second) {
		return new ComparablePair<>(first, second);
	}

	@SuppressWarnings("RawUseOfParameterized")
	@Override
	public boolean equals(final Object obj) {
		return (this == obj) || ((obj instanceof ComparablePair) &&
				                         first().equals(((ComparablePair) obj).first()
				                         ) &&
				                         second().equals(
						                         ((ComparablePair) obj).second()));
	}
}
