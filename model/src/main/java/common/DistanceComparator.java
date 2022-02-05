package common;

import common.map.MapDimensions;
import common.map.Point;

import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
/**
 * A class to compare {@link Point}s based on their distance to a specified
 * point (such as a player's HQ).
 */
public final class DistanceComparator implements Comparator<Point> {
	public DistanceComparator(final Point base, @Nullable final MapDimensions dimensions) {
		this.base = base;
		this.dimensions = dimensions;
	}

	/**
	 * The point we want to measure distance from.
	 */
	private final Point base;

	/**
	 * The dimensions of the map. May, but shouldn't, be null.
	 */
	@Nullable
	private final MapDimensions dimensions;

	/**
	 * Returns a value that is proportional to the distance from the base
	 * to the given point: in fact the *square* of the distance, to avoid
	 * taking an expensive square root.
	 */
	private int distance(final Point point) {
		int colDistRaw = Math.abs(point.getColumn() - base.getColumn());
		int rowDistRaw = Math.abs(point.getRow() - base.getRow());
		int colDist;
		int rowDist;
		if (dimensions != null && colDistRaw > dimensions.getColumns() / 2) {
			colDist = dimensions.getColumns() - colDistRaw;
		} else {
			colDist = colDistRaw;
		}
		if (dimensions != null && rowDistRaw > dimensions.getRows() / 2) {
			rowDist = dimensions.getRows() - rowDistRaw;
		} else {
			rowDist = rowDistRaw;
		}
		return (colDist * colDist) + (rowDist * rowDist);
	}

	/**
	 * Compare two points on the basis of distance from the base point.
	 */
	@Override
	public int compare(final Point firstPoint, final Point secondPoint) {
		return Integer.compare(distance(firstPoint), distance(secondPoint));
	}

	/**
	 * Returns a String describing how far a point is from "HQ", which the
	 * base point is presumed to be.
	 */
	public String distanceString(final Point point) {
		return distanceString(point, "HQ");
	}

	/**
	 * Returns a String describing how far a point is from the base point,
	 * described with the given name.
	 */
	public String distanceString(final Point point, final String name) {
		int dist = distance(point);
		if (dist < 0) {
			throw new IllegalStateException("Negative distance");
		} else if (dist == 0) {
			return String.format(" (at %s)", name);
		} else {
			return String.format(" (%.1f tiles from %s)", Math.sqrt(dist), name);
		}
	}
}
