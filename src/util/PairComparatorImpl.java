package util;

import java.util.Comparator;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A comparator for Pairs, that uses provided comparators to compare first the first item
 * in the pair, then the second.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @param <T> the first type in the pair
 * @param <U> the second type in the pair
 * @author Jonathan Lovelace
 */
public final class PairComparatorImpl<@NonNull T, @NonNull U> extends
		Pair.PairImpl<@NonNull Comparator<@NonNull T>, @NonNull Comparator<@NonNull U>>
		implements PairComparator<T, U> {
	/**
	 * Constructor.
	 *
	 * @param firstItem  the first comparator
	 * @param secondItem the second comparator
	 */
	public PairComparatorImpl(final Comparator<T> firstItem, final Comparator<U>
																 secondItem) {
		super(firstItem, secondItem);
	}

	/**
	 * Compare two pairs.
	 *
	 * @param firstPair  the first pair
	 * @param secondPair the second pair
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public int compare(final Pair<T, U> firstPair, final Pair<T, U> secondPair) {
		final int firstResult = first().compare(firstPair.first(), secondPair.first());
		if (firstResult == 0) {
			return second().compare(firstPair.second(), secondPair.second());
		} else {
			return firstResult;
		}
	}

}
