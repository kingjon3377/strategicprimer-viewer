package model.map;

import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;

/**
 * An encapsulation of a map's dimensions (and its map version as well).
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class MapDimensions {
	/**
	 * The map version.
	 */
	public final int version;

	/**
	 * The number of columns in the map.
	 */
	public final int cols;

	/**
	 * The number of rows in the map.
	 */
	public final int rows;

	/**
	 * Constructor.
	 *
	 * @param numRows the number of rows
	 * @param numCols the number of columns
	 * @param ver     the map version number
	 */
	public MapDimensions(final int numRows, final int numCols, final int ver) {
		rows = numRows;
		cols = numCols;
		version = ver;
	}

	/**
	 * @return The number of rows in the map.
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * @return The number of columns in the map.
	 */
	public int getColumns() {
		return cols;
	}

	/**
	 * @return The map version.
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @param obj an object
	 * @return whether it equals this
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return obj == this || obj instanceof MapDimensions
				                      && equalsImpl((MapDimensions) obj);
	}

	/**
	 * @param obj a map-dimensions object
	 * @return whether it equals this
	 */
	private boolean equalsImpl(final MapDimensions obj) {
		return obj.rows == rows && obj.cols == cols && obj.version == version;
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return rows + cols << 2;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(60);
		builder.append("Map dimensions: ");
		builder.append(rows);
		builder.append(" rows x ");
		builder.append(cols);
		builder.append(" cols; map version ");
		builder.append(version);
		return NullCleaner.assertNotNull(builder.toString());
	}
}
