package model.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import view.util.Coordinate;

/**
 * A cache for Points.
 *
 * @author Jonathan Lovelace
 *
 */
public final class PointFactory {
	/**
	 * Whether to use the cache.
	 */
	private static boolean useCache = true;

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
	 * The point cache.
	 */
	private static final Map<Integer, Map<Integer, Point>> POINT_CACHE =
			new ConcurrentHashMap<>();

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
			final Integer boxedRow = Integer.valueOf(row);
			final Integer boxedCol = Integer.valueOf(col);
			if (!POINT_CACHE.containsKey(boxedRow)) {
				POINT_CACHE.put(boxedRow,
						new ConcurrentHashMap<Integer, Point>());
			}
			if (!POINT_CACHE.get(boxedRow).containsKey(boxedCol)) {
				POINT_CACHE.get(boxedRow).put(boxedCol, new Point(row, col));
			}
			final Point retval = POINT_CACHE.get(boxedRow).get(boxedCol);
			assert retval != null;
			return retval; // NOPMD
		} else {
			return new Point(row, col);
		}
	}

	/**
	 * Coordinate cache.
	 */
	private static final Map<Integer, Map<Integer, Coordinate>> C_CACHE =
			new ConcurrentHashMap<>();

	/**
	 * @param xCoord an X coordinate or extent
	 * @param yCoord a Y coordinate or extent
	 *
	 * @return a Coordinate representing those coordinates.
	 */
	public static Coordinate coordinate(final int xCoord, final int yCoord) {
		if (useCache) {
			final Integer boxedX = Integer.valueOf(xCoord);
			final Integer boxedY = Integer.valueOf(yCoord);
			if (!C_CACHE.containsKey(boxedX)) {
				C_CACHE.put(boxedX,
						new ConcurrentHashMap<Integer, Coordinate>());
			}
			if (!C_CACHE.get(boxedX).containsKey(boxedY)) {
				C_CACHE.get(boxedX).put(boxedY,
						new Coordinate(xCoord, yCoord));
			}
			final Coordinate retval = C_CACHE.get(boxedX).get(boxedY);
			if (retval == null) {
				throw new IllegalStateException("Cache produced null result");
			}
			return retval; // NOPMD
		} else {
			return new Coordinate(xCoord, yCoord);
		}
	}
}
