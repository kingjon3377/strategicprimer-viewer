package model.map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * An encapsulation of a map's dimensions (and its map version as well).
 *
 * @author Jonathan Lovelace
 *
 */
public class MapDimensions {
	/**
	 * Constructor.
	 *
	 * @param numRows the number of rows
	 * @param numCols the number of columns
	 * @param ver the map version number
	 */
	public MapDimensions(final int numRows, final int numCols, final int ver) {
		rows = numRows;
		cols = numCols;
		version = ver;
	}

	/**
	 * The number of rows in the map.
	 */
	public final int rows;

	/**
	 * @return The number of rows in the map.
	 */
	public final int getRows() {
		return rows;
	}

	/**
	 * The number of columns in the map.
	 */
	public final int cols;

	/**
	 * @return The number of columns in the map.
	 */
	public final int getColumns() {
		return cols;
	}

	/**
	 * The map version.
	 */
	public final int version;

	/**
	 * @return The map version.
	 */
	public final int getVersion() {
		return version;
	}

	/**
	 * @param obj an object
	 * @return whether it equals this
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return obj == this
				|| (obj instanceof MapDimensions
						&& ((MapDimensions) obj).rows == rows
						&& ((MapDimensions) obj).cols == cols && ((MapDimensions) obj).version == version);
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return rows + cols << 2;
	}
}
