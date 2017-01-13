package model.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jdt.annotation.Nullable;
import util.MultiMapHelper;
import view.util.Coordinate;

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
	 * The standard "invalid point."
	 */
	public static final Point INVALID_POINT = point(-1, -1);
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
	 * Whether to use the cache.
	 */
	@SuppressWarnings("StaticNonFinalField")
	private static boolean useCache = true;

	/**
	 * Do not instantiate.
	 */
	private PointFactory() {
	}

	/**
	 * Clear the cache.
	 */
	public static void clearCache() {
		POINT_CACHE.clear();
	}

	/**
	 * Whether we should use our cache of points.
	 * @param shouldUseCache whether to use the cache from now on
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	public static void shouldUseCache(final boolean shouldUseCache) {
		useCache = shouldUseCache;
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
			final Integer boxedRow = Integer.valueOf(row);
			final Integer boxedCol = Integer.valueOf(col);
			return MultiMapHelper.getMapValue(MultiMapHelper.getMapValue(POINT_CACHE,
					boxedRow, key -> new ConcurrentHashMap<>()), boxedCol,
					key -> new PointImpl(row, col));
		} else {
			return new PointImpl(row, col);
		}
	}

	/**
	 * Produce, perhaps by getting from cache, a Coordinate with the given coordinates.
	 * @param xCoordinate an X coordinate or extent
	 * @param yCoordinate a Y coordinate or extent
	 * @return a Coordinate representing those coordinates.
	 */
	public static Coordinate coordinate(final int xCoordinate, final int yCoordinate) {
		if (useCache) {
			final Integer boxedX = Integer.valueOf(xCoordinate);
			final Integer boxedY = Integer.valueOf(yCoordinate);
			return MultiMapHelper.getMapValue(MultiMapHelper.getMapValue(C_CACHE, boxedX,
					key -> new ConcurrentHashMap<>()), boxedY,
					key -> new Coordinate(xCoordinate, yCoordinate));
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
		 * The location's first coordinate, its row.
		 * @return the first coordinate.
		 */
		@Override
		public int getRow() {
			return row;
		}

		/**
		 * The location's second coordinate, its column.
		 * @return the second coordinate.
		 */
		@Override
		public int getCol() {
			return col;
		}

		/**
		 * An object is equal iff it is a Point with the same row and column.
		 * @param obj the other object
		 * @return whether this object equals another.
		 */
		@Override
		public boolean equals(@Nullable final Object obj) {
			return (this == obj) || ((obj instanceof Point) && (((Point) obj).getRow()
																		== row) &&
											 (((Point) obj).getCol() == col));
		}

		/**
		 * A hash code. TODO: This probably collides more than we want.
		 * @return a hash code.
		 */
		@Override
		public int hashCode() {
			return row | col;
		}

		/**
		 * "(row, col)".
		 * @return a String representation of the class
		 */
		@Override
		public String toString() {
			return '(' + Integer.toString(row) + ", " + Integer.toString(col) + ')';
		}
	}
}
