package util;

import java.util.Comparator;
import org.eclipse.jdt.annotation.NonNull;

/**
 * An interface for a class that is both a Pair of Comparators and a Comparator of Pairs.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2017 Jonathan Lovelace
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
public interface PairComparator<@NonNull T, @NonNull U>
		extends Pair<@NonNull Comparator<@NonNull T>, @NonNull Comparator<@NonNull U>>,
						Comparator<@NonNull Pair<@NonNull T, @NonNull U>> {
	// no new members
}
