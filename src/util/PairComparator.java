package util;

import java.util.Comparator;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A comparator for Pairs, that uses provided comparators to compare first the
 * first item in the pair, then the second.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
 * this program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 * @param <T>
 *            the first type in the pair
 * @param <U>
 *            the second type in the pair
 */
public class PairComparator<T, U> extends Pair<Comparator<T>, Comparator<U>> implements Comparator<Pair<T, U>> {
	public PairComparator(final Comparator<T> one, final Comparator<U> two) {
		super(one, two);
	}
	@Override
	public int compare(@Nullable final Pair<T, U> one, @Nullable final Pair<T, U> two) {
		if (one == null || two == null) {
			throw new NullPointerException("asked to compare null Pair");
		}
		final int firstResult = first().compare(one.first(), two.first());
		if (firstResult == 0) {
			return second().compare(one.second(), two.second());
		} else {
			return firstResult;
		}
	}

}
