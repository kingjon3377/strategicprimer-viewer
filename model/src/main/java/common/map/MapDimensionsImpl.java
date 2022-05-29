package common.map;

/**
 * An encapsulation of a map's dimensions (and its map version as well).
 */
public final class MapDimensionsImpl implements MapDimensions {
	public MapDimensionsImpl(final int rows, final int columns, final int version) {
		if (rows < 0 || columns < 0) {
			throw new IllegalArgumentException("Dimensions cannot be negative");
		}
		this.rows = rows;
		this.columns = columns;
		this.version = version;
	}

	/**
	 * The map version.
	 */
	private final int version;

	/**
	 * The map version.
	 */
	@Override
	public int getVersion() {
		return version;
	}

	/**
	 * The number of rows in the map.
	 */
	private final int rows;

	/**
	 * The number of rows in the map.
	 */
	@Override
	public int getRows() {
		return rows;
	}

	/**
	 * The number of columns in the map.
	 */
	private final int columns;

	/**
	 * The number of columns in the map.
	 */
	@Override
	public int getColumns() {
		return columns;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof MapDimensions md) {
			return md.getRows() == rows && md.getColumns() == columns && md.getVersion() == version;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return rows + (columns << 2);
	}

	@Override
	public String toString() {
		return String.format("Map dimensions: %d rows x %d columns; map version %d", rows, columns, version);
	}
}
