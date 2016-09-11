package model.exploration.old;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import model.map.MapDimensions;
import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;
import model.map.TileType;
import util.NullCleaner;

/**
 * A class for things where results are by quadrant rather than randomly.
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
 * @author Jonathan Lovelace
 */
public final class QuadrantTable implements EncounterTable {
	/**
	 * The default size of the map in rows.
	 */
	private static final int MAP_SIZE_ROWS = 69;
	/**
	 * The default size of the map in columns.
	 */
	private static final int MAP_SIZE_COLS = 88;
	/**
	 * The items to allocate by quadrant.
	 */
	private final List<String> possibleResults;
	/**
	 * How many rows of quadrants there should be
	 */
	private final int quadrantRows;
	/**
	 * The collection of collections of results.
	 */
	private final Map<MapDimensions, Map<Point, String>> quadrants = new HashMap<>();
	/**
	 * Constructor.
	 * @param mapRows the size of the map in rows
	 * @param mapCols the size of the map in columns
	 * @param rows the number of rows of quadrants
	 * @param items the items to allocate by quadrant
	 */
	public QuadrantTable(final int mapRows, final int mapCols, final int rows,
						final List<String> items) {
		possibleResults = new ArrayList<>(items);
		quadrantRows = rows;
		final MapDimensions dimensions = new MapDimensions(mapRows, mapCols, 2);
		final Map<Point, String> firstQuadrants =
				getValuesFor(dimensions);
		quadrants.put(dimensions, firstQuadrants);
	}

	/**
	 * Get the maximum-Point-to-event mapping for a given map size, possibly from the
	 * cache.
	 * @param mapDimensions the dimensions of the map
	 * @return the quadrant mapping
	 */
	private Map<Point, String> getValuesFor(final MapDimensions mapDimensions) {
		if (quadrants.containsKey(mapDimensions)) {
			return quadrants.get(mapDimensions);
		} else {
			final Map<Point, String> retval = new HashMap<>();
			final int cols = possibleResults.size() / quadrantRows;
			int i = 0;
			final int mapCols = mapDimensions.cols;
			final int mapRows = mapDimensions.rows;
			final int colRemain = mapCols % cols;
			final int rowRemain = mapRows % quadrantRows;
			final int colStep = mapCols / cols;
			final int rowStep = mapRows / quadrantRows;
			for (int row = 0; row < (mapRows - rowRemain); row += rowStep) {
				for (int col = 0; col < (mapCols - colRemain); col += colStep) {
					retval
							.put(PointFactory.point(row, col), possibleResults.get(i));
					i++;
				}
			}
			quadrants.put(mapDimensions, retval);
			return retval;
		}
	}

	/**
	 * Constructor.
	 *
	 * @param rows  the number of rows of quadrants
	 * @param items the items to allocate by quadrant
	 */
	public QuadrantTable(final int rows, final List<String> items) {
		possibleResults = new ArrayList<>(items);
		quadrantRows = rows;
	}

	/**
	 * @param row the row of a tile
	 * @param col the column of a tile
	 * @param mapDimensions the dimensions of the map
	 * @return the result from the quadrant containing that tile.
	 */
	public String getQuadrantValue(final int row, final int col,
								   final MapDimensions mapDimensions) {
		final Map<Point, String> resultsMap = getValuesFor(mapDimensions);
		Point bestKey = PointFactory.point(-1, -1);
		for (final Point iter : resultsMap.keySet()) {
			if ((iter.getRow() <= row) && (iter.getRow() > bestKey.getRow()) &&
						(iter.getCol() <= col) && (iter.getCol() > bestKey.getCol())) {
				bestKey = iter;
			}
		}
		return NullCleaner.valueOrDefault(resultsMap.get(bestKey), "");
	}

	/**
	 * @param point    the location of the tile
	 * @param terrain  ignored
	 * @param fixtures ignored
	 * @param mapDimensions the dimensions of the map
	 * @return what the table has for that tile
	 */
	@Override
	public String generateEvent(final Point point, final TileType terrain,
								final Iterable<TileFixture> fixtures,
								final MapDimensions mapDimensions) {
		return getQuadrantValue(point.getRow(), point.getCol(), mapDimensions);
	}
	/**
	 * @param point    ignored
	 * @param terrain  ignored
	 * @param fixtures any fixtures on the tile
	 * @param mapDimensions the dimensions of the map
	 * @return the event on that tile
	 */
	@Override
	public String generateEvent(final Point point, final TileType terrain,
								final Stream<TileFixture> fixtures,
								final MapDimensions mapDimensions) {
		return getQuadrantValue(point.getRow(), point.getCol(), mapDimensions);
	}

	/**
	 * @return all events that this table can produce.
	 */
	@Override
	public Set<String> allEvents() {
		return new HashSet<>(possibleResults);
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "QuadrantTable in " + quadrants.size() + " quadrants";
	}
}
