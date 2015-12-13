package model.map;

import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNull;

import util.NullCleaner;
/**
 * A class to compare Points based on their distance to a specified point (such as a player's HQ)
 * @author Jonathan Lovelace
 */
public class DistanceComparator implements Comparator<@NonNull Point> {
	/**
	 * The point we want to measure distance from.
	 */
	Point base;
	/**
	 * @param center the point we want to measure distance from
	 */
	public DistanceComparator(final Point center) {
		base = center;
	}
	/**
	 * @param one the first point
	 * @param two the second point
	 * @return the result of the comparison
	 */
	@Override
	public int compare(final Point one, final Point two) {
		int distOne = distance(one);
		int distTwo = distance(two);
		if (distOne == distTwo) {
			return one.compareTo(two);
		} else {
			return distOne - distTwo;
		}
	}

	/**
	 * Note that this returns the *square* of the distance; for *comparison*
	 * that suffices, and is far faster than taking umpteen square roots. For
	 * *display* remember to take the square root.
	 *
	 * @param point
	 *            a point
	 * @return the square of the distance to it from the base
	 */
	public int distance(final Point point) {
		return (point.col - base.col) * (point.col - base.col) + (point.row - base.row) * (point.row - base.row);
	}
	/**
	 * @param point a point
	 * @return its distance from HQ, formatted for print
	 */
	public String distanceString(final Point point) {
		int dist = distance(point);
		if (dist == 0) {
			return "(at HQ)";
		} else {
			return NullCleaner.assertNotNull(
					String.format("(%.0f tiles from HQ) ", Double.valueOf(Math.sqrt(dist))));
		}
	}
	/**
	 * @return a string representation of this class
	 */
	@Override
	public String toString() {
		return "DistanceComparator";
	}
}
