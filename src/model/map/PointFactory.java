package model.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
		CACHE.clear();
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
	 * The cache.
	 */
	private static final Map<Integer, Map<Integer, Point>> CACHE = new ConcurrentHashMap<Integer, Map<Integer, Point>>();

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
			if (!CACHE.containsKey(boxedRow)) {
				CACHE.put(boxedRow, new ConcurrentHashMap<Integer, Point>());
			}
			if (!CACHE.get(boxedRow).containsKey(boxedCol)) {
				CACHE.get(boxedRow).put(boxedCol, new Point(row, col));
			}
			return CACHE.get(boxedRow).get(boxedCol); // NOPMD
		} else {
			return new Point(row, col);
		}
	}
}
