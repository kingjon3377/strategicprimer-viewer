package common.map;

/**
 * An encapsulation of a map's dimensions (and its map version as well).
 */
// This is an interface so we can make a mock object "implementing" it and
// guarantee it is never referenced by making all of its getters throw.
public interface MapDimensions {
	/**
	 * The number of rows in the map.
	 */
	int getRows();

	/**
	 * The number of columns in the map.
	 */
	int getColumns();

	/**
	 * The map version.
	 */
	int getVersion();

	default boolean contains(final Point point) {
		return point.isValid() && point.getRow() < getRows() && point.getColumn() < getColumns();
	}

	/**
	 * The distance between two points in a map with these dimensions.
	 */
	default double distance(final Point first, final Point second) {
		int rawXDiff = first.getRow() - second.getRow();
		int rawYDiff = first.getColumn() - second.getColumn();
		int xDiff;
		if (rawXDiff < (getRows() / 2)) {
			xDiff = rawXDiff;
		} else {
			xDiff = getRows() - rawXDiff;
		}
		int yDiff;
		if (rawYDiff < (getColumns() / 2)) {
			yDiff = rawYDiff;
		} else {
			yDiff = getColumns() - rawYDiff;
		}
		return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
	}
}
