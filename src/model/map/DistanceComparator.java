package model.map;

import java.util.Comparator;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A class to compare Points based on their distance to a specified point (such as a
 * player's HQ).
 *
 * @author Jonathan Lovelace
 */
public final class DistanceComparator implements Comparator<@NonNull Point> {
	/**
	 * The point we want to measure distance from.
	 */
	private final Point base;

	/**
	 * Constructor.
	 * @param center the point we want to measure distance from
	 */
	public DistanceComparator(final Point center) {
		base = center;
	}

	/**
	 * Compare two points on the basis of distance from the base point.
	 * @param firstPoint  the first point
	 * @param secondPoint the second point
	 * @return the result of the comparison
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public int compare(final Point firstPoint, final Point secondPoint) {
		return Integer.compare(distance(firstPoint), distance(secondPoint));
	}

	/**
	 * Note that this returns the *square* of the distance; for *comparison* that
	 * suffices, and is far faster than taking umpteen square roots. For *display*
	 * remember to take the square root.
	 *
	 * @param point a point
	 * @return the square of the distance to it from the base
	 */
	public int distance(final Point point) {
		return ((point.getCol() - base.getCol()) * (point.getCol() - base.getCol())) +
					   ((point.getRow() - base.getRow()) *
								(point.getRow() - base.getRow()));
	}

	/**
	 * Returns a String describing how far a point is from "HQ", which the base point is
	 * presumed to be.
	 * @param point a point
	 * @return its distance from HQ, formatted for print
	 */
	public String distanceString(final Point point) {
		final int dist = distance(point);
		if (dist == 0) {
			return "(at HQ)";
		} else {
			return String.format("(%.0f tiles from HQ) ",
					Double.valueOf(Math.sqrt(dist)));
		}
	}

	/**
	 * A simple toString().
	 * @return a string representation of this class
	 */
	@Override
	public String toString() {
		return "DistanceComparator with base " + base;
	}
}
