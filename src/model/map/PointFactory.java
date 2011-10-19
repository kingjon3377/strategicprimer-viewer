package model.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A cache for Points.
 * @author Jonathan Lovelace
 *
 */
public final class PointFactory {
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
	 * @param row a row
	 * @param col a column
	 * @return a Point representing this point.
	 */
	public static Point point(final int row, final int col) {
		if (!CACHE.containsKey(row)) {
			CACHE.put(row, new ConcurrentHashMap<Integer, Point>());
		}
		if (!CACHE.get(row).containsKey(col)) {
			CACHE.get(row).put(col, new Point(row, col));
		} 
		return CACHE.get(row).get(col);
	}
}
