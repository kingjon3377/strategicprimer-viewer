package util;

import java.util.Comparator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A comparator for Pairs, that uses provided comparators to compare first the first item
 * in the pair, then the second.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
 * @param <T> the first type in the pair
 * @param <U> the second type in the pair
 * @author Jonathan Lovelace
 */
public final class PairComparator<@NonNull T, @NonNull U>
		extends Pair<@NonNull Comparator<@NonNull T>, @NonNull Comparator<@NonNull U>>
		implements Comparator<@NonNull Pair<@NonNull T, @NonNull U>> {
	/**
	 * Constructor.
	 *
	 * @param firstItem  the first comparator
	 * @param secondItem the second comparator
	 */
	public PairComparator(final Comparator<T> firstItem, final Comparator<U>
			                                                     secondItem) {
		super(firstItem, secondItem);
	}

	/**
	 * Compare two pairs.
	 *
	 * @param firstPair  the first pair
	 * @param secondPair the second pair
	 */
	@Override
	public int compare(@Nullable final Pair<T, U> firstPair,
	                   @Nullable final Pair<T, U> secondPair) {
		if ((firstPair == null) || (secondPair == null)) {
			throw new NullPointerException("asked to compare null Pair");
		}
		final int firstResult = first().compare(firstPair.first(), secondPair.first());
		if (firstResult == 0) {
			return second().compare(firstPair.second(), secondPair.second());
		} else {
			return firstResult;
		}
	}

}
