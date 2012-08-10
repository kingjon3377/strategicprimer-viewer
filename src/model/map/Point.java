package model.map;

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
		this.row = rowNum;
		this.col = colNum;
	}

	/**
	 *
	 * @param obj the other object
	 * @return whether this object equals another.
	 */
	@Override
	public final boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof Point && (((Point) obj).row == row && ((Point) obj).col == col));
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
	public int compareTo(final Point point) {
		return ((point.getRow() - getRow()) << 7) + (point.getCol() - getCol());
	}

	/**
	 *
	 * @return a String representation of the class
	 */
	@Override
	public String toString() {
		return "(" + row + ", " + col + ")";
	}

	/**
	 * @return an XML representation of the point
	 * @deprecated Replaced by SPIntermediateRepresentation-based output
	 */
	@Deprecated
	public String toXML() {
		return "row=\"" + row + "\" column=\"" + col + "\"";
	}
}
