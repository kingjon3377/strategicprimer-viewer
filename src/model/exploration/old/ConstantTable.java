package model.exploration.old;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import model.map.Point;
import model.map.TileFixture;
import model.map.TileType;

/**
 * An EncounterTable that always returns the same value.
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
public final class ConstantTable implements EncounterTable {
	/**
	 * The value we'll always return.
	 */
	private final String value;

	/**
	 * Constructor; defaults to the empty string.
	 */
	public ConstantTable() {
		this("");
	}

	/**
	 * Constructor.
	 *
	 * @param val the value to return for all queries.
	 */
	public ConstantTable(final String val) {
		value = val;
	}

	/**
	 * @param terrain  ignored
	 * @param point    ignored
	 * @param fixtures ignored
	 * @return our specified value.
	 */
	@Override
	public String generateEvent(final Point point, final TileType terrain,
	                            final Iterable<TileFixture> fixtures) {
		return value;
	}
	/**
	 * @param terrain  ignored
	 * @param point    ignored
	 * @param fixtures any fixtures on the tile
	 * @return the event on that tile
	 */
	@Override
	public String generateEvent(final Point point, final TileType terrain,
	                            final Stream<TileFixture> fixtures) {
		return value;
	}

	/**
	 * @return all events that this table can produce.
	 */
	@Override
	public Set<String> allEvents() {
		final Set<String> retval = new HashSet<>();
		retval.add(value);
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ConstantTable: " + value;
	}
}
