package common.map;

/**
 * An encapsulation of a map's dimensions (and its map version as well).
 *
 * TODO: Now this is a record class, merge back with interface, surely?
 *
 * @param version The map version.
 * @param rows    The number of rows in the map.
 * @param columns The number of columns in the map.
 */
public record MapDimensionsImpl(int rows, int columns, int version) implements MapDimensions {
    public MapDimensionsImpl {
        if (rows < 0 || columns < 0) {
            throw new IllegalArgumentException("Dimensions cannot be negative");
        }
    }

    /**
     * The map version.
     */
    @Override
    public int version() {
        return version;
    }

    /**
     * The number of rows in the map.
     */
    @Override
    public int rows() {
        return rows;
    }

    /**
     * The number of columns in the map.
     */
    @Override
    public int columns() {
        return columns;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof MapDimensions md) {
            return md.rows() == rows && md.columns() == columns && md.version() == version;
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
