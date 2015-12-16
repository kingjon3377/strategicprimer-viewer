package model.map;

import static util.NullCleaner.assertNotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import util.NullCleaner;
import view.util.Coordinate;

/**
 * A cache for Points.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2014 Jonathan Lovelace
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class PointFactory {
	/**
	 * Whether to use the cache.
	 */
	@SuppressWarnings("StaticNonFinalField")
	private static boolean useCache = true;

	/**
	 * The point cache.
	 */
	private static final Map<Integer, Map<Integer, Point>> POINT_CACHE =
			new ConcurrentHashMap<>();

	/**
	 * Coordinate cache.
	 */
	private static final Map<Integer, Map<Integer, Coordinate>> C_CACHE =
			new ConcurrentHashMap<>();

	/**
	 * Clear the cache.
	 */
	public static void clearCache() {
		POINT_CACHE.clear();
	}

	/**
	 * @param shouldUseCache whether to use the cache from now on
	 */
	public static void shouldUseCache(final boolean shouldUseCache) {
		useCache = shouldUseCache;
	}

	/**
	 * Do not instantiate.
	 */
	private PointFactory() {
	}

	/**
	 * Factory method. I considered replacing the cache with simply a
	 * constructor call, but after some performance testing it looks like the
	 * cache is faster as the map gets more complicated, so we'll leave it.
	 * (Note, however, that the testing was with the DrawHelperComparator and
	 * then with the EchoDriver, so not a realistic model of the application.
	 * Sigh.)
	 *
	 * @param row a row
	 * @param col a column
	 *
	 * @return a Point representing this point.
	 */
	public static Point point(final int row, final int col) {
		if (useCache) {
			final Integer boxedRow = assertNotNull(Integer.valueOf(row));
			final Integer boxedCol = assertNotNull(Integer.valueOf(col));
			if (!POINT_CACHE.containsKey(boxedRow)) {
				POINT_CACHE.put(boxedRow,
						new ConcurrentHashMap<>());
			}
			if (!POINT_CACHE.get(boxedRow).containsKey(boxedCol)) {
				POINT_CACHE.get(boxedRow).put(boxedCol, new Point(row, col));
			}
			return assertNotNull(POINT_CACHE.get(boxedRow).get(boxedCol)); // NOPMD
		} else {
			return new Point(row, col);
		}
	}

	/**
	 * @param xCoord an X coordinate or extent
	 * @param yCoord a Y coordinate or extent
	 *
	 * @return a Coordinate representing those coordinates.
	 */
	public static Coordinate coordinate(final int xCoord, final int yCoord) {
		if (useCache) {
			final Integer boxedX = assertNotNull(Integer.valueOf(xCoord));
			final Integer boxedY = assertNotNull(Integer.valueOf(yCoord));
			if (!C_CACHE.containsKey(boxedX)) {
				C_CACHE.put(boxedX,
						new ConcurrentHashMap<>());
			}
			if (!C_CACHE.get(boxedX).containsKey(boxedY)) {
				C_CACHE.get(boxedX).put(boxedY,
						new Coordinate(xCoord, yCoord));
			}
			return C_CACHE.get(boxedX).get(boxedY); // NOPMD
		} else {
			return new Coordinate(xCoord, yCoord);
		}
	}
}
