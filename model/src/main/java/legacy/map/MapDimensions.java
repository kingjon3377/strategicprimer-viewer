package legacy.map;

/**
 * An encapsulation of a map's dimensions (and its map version as well).
 */
// This is an interface to allow us to make a mock object "implementing" it and
// guarantee it is never referenced by making all of its getters throw.
public interface MapDimensions {
	/**
	 * The number of rows in the map.
	 */
	int rows();

	/**
	 * The number of columns in the map.
	 */
	int columns();

	/**
	 * The map version.
	 */
	int version();

	default boolean contains(final Point point) {
		return point.isValid() && point.row() < rows() && point.column() < columns();
	}

	/**
	 * The distance between two points in a map with these dimensions.
	 */
	default double distance(final Point first, final Point second) {
		final int rawXDiff = first.row() - second.row();
		final int rawYDiff = first.column() - second.column();
		final int xDiff;
		if (rawXDiff < (rows() / 2)) {
			xDiff = rawXDiff;
		} else {
			xDiff = rows() - rawXDiff;
		}
		final int yDiff;
		if (rawYDiff < (columns() / 2)) {
			yDiff = rawYDiff;
		} else {
			yDiff = columns() - rawYDiff;
		}
		return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
	}
}
