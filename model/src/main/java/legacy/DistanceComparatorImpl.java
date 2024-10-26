package legacy;

import legacy.map.MapDimensions;
import legacy.map.Point;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * A class to compare {@link Point}s based on their distance to a specified
 * point (such as a player's HQ).
 */
public final class DistanceComparatorImpl implements DistanceComparator, Serializable {
	public DistanceComparatorImpl(final Point base, final @Nullable MapDimensions dimensions) {
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
	private final @Nullable MapDimensions dimensions;

	/**
	 * Returns a value that is proportional to the distance from the base
	 * to the given point: in fact the *square* of the distance, to avoid
	 * taking an expensive square root.
	 */
	private int distance(final Point point) {
		final int colDistRaw = Math.abs(point.column() - base.column());
		final int rowDistRaw = Math.abs(point.row() - base.row());
		final int colDist;
		final int rowDist;
		if (!Objects.isNull(dimensions) && colDistRaw > dimensions.columns() / 2) {
			colDist = dimensions.columns() - colDistRaw;
		} else {
			colDist = colDistRaw;
		}
		if (!Objects.isNull(dimensions) && rowDistRaw > dimensions.rows() / 2) {
			rowDist = dimensions.rows() - rowDistRaw;
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
	@Override
	public String distanceString(final Point point) {
		return distanceString(point, "HQ");
	}

	/**
	 * Returns a String describing how far a point is from the base point,
	 * described with the given name.
	 */
	@Override
	public String distanceString(final Point point, final String name) {
		final int dist = distance(point);
		if (dist < 0) {
			throw new IllegalStateException("Negative distance");
		} else if (dist == 0) {
			return " (at %s)".formatted(name);
		} else {
			return " (%.1f tiles from %s)".formatted(Math.sqrt(dist), name);
		}
	}
}
