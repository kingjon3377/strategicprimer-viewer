package model.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jdt.annotation.Nullable;
import view.util.Coordinate;

import static util.NullCleaner.assertNotNull;

/**
 * A cache for Points.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("UtilityClassCanBeEnum")
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
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	public static void shouldUseCache(final boolean shouldUseCache) {
		useCache = shouldUseCache;
	}

	/**
	 * Do not instantiate.
	 */
	private PointFactory() {
	}

	/**
	 * Factory method. I considered replacing the cache with simply a constructor call,
	 * but after some performance testing it looks like the cache is faster as the map
	 * gets more complicated, so we'll leave it. (Note, however, that the testing was
	 * with
	 * the DrawHelperComparator and then with the EchoDriver, so not a realistic model of
	 * the application. Sigh.)
	 *
	 * @param row a row
	 * @param col a column
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
			if (!assertNotNull(POINT_CACHE.get(boxedRow)).containsKey(boxedCol)) {
				assertNotNull(POINT_CACHE.get(boxedRow)).put(boxedCol, new PointImpl(row, col));
			}
			return assertNotNull(assertNotNull(POINT_CACHE.get(boxedRow)).get(boxedCol));
		} else {
			return new PointImpl(row, col);
		}
	}

	/**
	 * @param xCoordinate an X coordinate or extent
	 * @param yCoordinate a Y coordinate or extent
	 * @return a Coordinate representing those coordinates.
	 */
	public static Coordinate coordinate(final int xCoordinate, final int yCoordinate) {
		if (useCache) {
			final Integer boxedX = assertNotNull(Integer.valueOf(xCoordinate));
			final Integer boxedY = assertNotNull(Integer.valueOf(yCoordinate));
			if (!C_CACHE.containsKey(boxedX)) {
				C_CACHE.put(boxedX,
						new ConcurrentHashMap<>());
			}
			if (!assertNotNull(C_CACHE.get(boxedX)).containsKey(boxedY)) {
				assertNotNull(C_CACHE.get(boxedX)).put(boxedY,
						new Coordinate(xCoordinate, yCoordinate));
			}
			return assertNotNull(assertNotNull(C_CACHE.get(boxedX)).get(boxedY));
		} else {
			return new Coordinate(xCoordinate, yCoordinate);
		}
	}

	/**
	 * A structure encapsulating two coordinates.
	 */
	private static final class PointImpl implements Point {
		/**
		 * The first coordinate.
		 */
		private final int row;
		/**
		 * The second coordinate.
		 */
		private final int col;

		/**
		 * @return the first coordinate.
		 */
		@Override
		public int getRow() {
			return row;
		}

		/**
		 * @return the second coordinate.
		 */
		@Override
		public int getCol() {
			return col;
		}

		/**
		 * Constructor.
		 *
		 * @param rowNum The first coordinate
		 * @param colNum The second coordinate
		 */
		protected PointImpl(final int rowNum, final int colNum) {
			row = rowNum;
			col = colNum;
		}

		/**
		 * @param obj the other object
		 * @return whether this object equals another.
		 */
		@Override
		public boolean equals(@Nullable final Object obj) {
			return (this == obj) || ((obj instanceof Point) && (((Point) obj).getRow() == row) &&
											(((Point) obj).getCol() == col));
		}

		/**
		 * @return a hash code.
		 */
		@Override
		public int hashCode() {
			return row | col;
		}

		/**
		 * @return a String representation of the class
		 */
		@Override
		public String toString() {
			return '(' + Integer.toString(row) + ", " + Integer.toString(col) + ')';
		}
	}
}
