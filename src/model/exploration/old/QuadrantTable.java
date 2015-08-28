package model.exploration.old;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;
import model.map.TileType;
import util.NullCleaner;

/**
 * A class for things where results are by quadrant rather than randomly.
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
public class QuadrantTable implements EncounterTable {
	/**
	 * The size of the map in rows. TODO: this should be dynamic.
	 */
	private static final int MAP_SIZE_ROWS = 69;
	/**
	 * The size of the map in columns. TODO: this should be dynamic.
	 */
	private static final int MAP_SIZE_COLS = 88;
	/**
	 * The collection of results.
	 */
	private final Map<Point, String> quadrants;

	/**
	 * Constructor.
	 *
	 * @param rows the number of rows of quadrants
	 * @param items the items to allocate by quadrant
	 */
	public QuadrantTable(final int rows, final List<String> items) {
		if (items.size() % rows != 0) {
			throw new IllegalArgumentException(Integer.toString(items.size())
					+ " items won't divide evenly into "
					+ Integer.toString(rows));
		}
		final int cols = items.size() / rows;
		final int rowstep = MAP_SIZE_ROWS / rows;
		final int colstep = MAP_SIZE_COLS / cols;
		final int rowRemain = MAP_SIZE_ROWS % rows;
		final int colRemain = MAP_SIZE_COLS % cols;
		quadrants = new HashMap<>();
		int i = 0;
		for (int row = 0; row < MAP_SIZE_ROWS - rowRemain; row += rowstep) {
			for (int col = 0; col < MAP_SIZE_COLS - colRemain; col += colstep) {
				// System.out.println("Adding " + items.get(0) + " at (" + row +
				// ", " + col +").");
				quadrants.put(PointFactory.point(row, col), items.get(i));
				i++;
			}
		}
	}

	/**
	 * @param row the row of a tile
	 * @param col the column of a tile
	 *
	 * @return the result from the quadrant containing that tile.
	 */
	public String getQuadrantValue(final int row, final int col) {
		Point bestKey = PointFactory.point(-1, -1);
		for (final Point iter : quadrants.keySet()) {
			if (iter.row <= row && iter.row > bestKey.row
					&& iter.col <= col && iter.col > bestKey.col) {
				bestKey = iter;
			}
		}
		return NullCleaner.valueOrDefault(quadrants.get(bestKey), "");
	}

	/**
	 * @param terrain ignored
	 * @param fixtures ignored
	 * @param point the location of the tile
	 * @return what the table has for that tile
	 */
	@Override
	public String generateEvent(final Point point, final TileType terrain,
			@Nullable final Iterable<TileFixture> fixtures) {
		return getQuadrantValue(point.row, point.col);
	}

	/**
	 * @return all events that this table can produce.
	 */
	@Override
	public Set<String> allEvents() {
		return new HashSet<>(quadrants.values());
	}

	/**
	 *
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "QuadrantTable";
	}
}
