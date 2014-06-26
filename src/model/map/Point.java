package model.map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A structure encapsulating two coordinates.
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
			throw new IllegalArgumentException("Compared to null point");
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
