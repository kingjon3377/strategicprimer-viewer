package model.map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A structure encapsulating two coordinates.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public class Point implements Comparable<Point> {
	/**
	 * The first coordinate.
	 */
	public final int row;
	/**
	 * The second coordinate.
	 */
	public final int col;

	/**
	 * @return the first coordinate.
	 */
	public final int getRow() {
		return row;
	}

	/**
	 * @return the second coordinate.
	 */
	public final int getCol() {
		return col;
	}

	/**
	 * Constructor.
	 *
	 * @param rowNum The first coordinate
	 * @param colNum The second coordinate
	 */
	public Point(final int rowNum, final int colNum) {
		row = rowNum;
		col = colNum;
	}

	/**
	 *
	 * @param obj the other object
	 * @return whether this object equals another.
	 */
	@Override
	public final boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof Point && ((Point) obj).row == row
				&& ((Point) obj).col == col;
	}

	/**
	 *
	 * @return a hash code.
	 */
	@Override
	public final int hashCode() {
		return row | col;
	}

	/**
	 * @param point another point
	 *
	 * @return the result of a comparison with that point
	 */
	@Override
	public int compareTo(@Nullable final Point point) {
		if (point == null) {
			throw new IllegalArgumentException("Asked to compare null point");
		}
		return (point.getRow() - getRow() << 7) + point.getCol() - getCol();
	}

	/**
	 *
	 * @return a String representation of the class
	 */
	@Override
	public String toString() {
		return '(' + Integer.toString(row) + ", " + Integer.toString(col) + ')';
	}
}
