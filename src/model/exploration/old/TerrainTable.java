package model.exploration.old;

import model.map.Point;
import model.map.TileFixture;
import model.map.TileType;
import util.NullCleaner;
import util.Pair;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A table that gives its result based on the terrain type of the tile in question.
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
 * @author Jonathan Lovelace
 */
public final class TerrainTable implements EncounterTable {
	/**
	 * Mapping from terrain type to "events".
	 */
	private final Map<TileType, String> mapping = new EnumMap<>(TileType.class);

	/**
	 * Constructor.
	 *
	 * @param items the items to add to the list
	 */
	public TerrainTable(final Iterable<Pair<TileType, String>> items) {
		for (final Pair<TileType, String> item : items) {
			mapping.put(item.first(), item.second());
		}
	}

	/**
	 * @param terrain  the terrain at the location
	 * @param fixtures ignored
	 * @param point    the location of the tile
	 * @return what the table has for that kind of tile.
	 */
	@Override
	public String generateEvent(final Point point, final TileType terrain,
	                            final Iterable<TileFixture> fixtures) {
		if (mapping.containsKey(terrain)) {
			return NullCleaner.assertNotNull(mapping.get(terrain));
		} else {
			throw new IllegalArgumentException(
					                                  "Table does not account for that " +
							                                  "terrain type");
		}
	}

	/**
	 * @return all events that this table can produce.
	 */
	@Override
	public Set<String> allEvents() {
		return new HashSet<>(mapping.values());
	}

	/**
	 * @return a String representation of the class
	 */
	@Override
	public String toString() {
		return "TerrainTable";
	}
}
