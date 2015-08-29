package model.exploration.old;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import model.map.Point;
import model.map.TileFixture;
import model.map.TileType;
import util.ComparablePair;
import util.Pair;
import util.SingletonRandom;

/**
 * A table where the event is selected at random.
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public class RandomTable implements EncounterTable {
	/**
	 * A list of items.
	 */
	private final List<ComparablePair<Integer, String>> table;

	/**
	 * @param terrain ignored
	 * @param fixtures ignored
	 * @param point ignored
	 *
	 * @return a random item from the table, or the last item in the table if
	 *         the normal procedure fails.
	 */
	@Override
	public String generateEvent(final Point point, final TileType terrain,
			final Iterable<@NonNull TileFixture> fixtures) {
		final int roll = SingletonRandom.RANDOM.nextInt(100);
		return getLowestMatch(roll);
	}

	/**
	 * @param value a number to check the table against
	 *
	 * @return the result of the check
	 */
	private String getLowestMatch(final int value) {
		for (final Pair<Integer, String> item : table) {
			if (value >= item.first().intValue()) {
				return item.second(); // NOPMD
			}
		}
		return table.get(table.size() - 1).second();
	}

	/**
	 * Constructor.
	 *
	 * @param items the items in the table.
	 */
	public RandomTable(final List<ComparablePair<Integer, String>> items) {
		table = new ArrayList<>(items);
		Collections.sort(table, Collections.reverseOrder());
	}

	/**
	 * @return all events that this table can produce.
	 */
	@Override
	public Set<String> allEvents() {
		final Set<String> retval = new HashSet<>();
		for (final Pair<Integer, String> pair : table) {
			retval.add(pair.second());
		}
		return retval;
	}

	/**
	 *
	 * @return a String representation of the class
	 */
	@Override
	public String toString() {
		return "RandomTable";
	}
}
