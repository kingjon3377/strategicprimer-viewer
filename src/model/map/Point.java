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
	private final int myRow;
	/**
	 * The second coordinate.
	 */
	private final int myCol;

	/**
	 * 
	 * @return the first coordinate.
	 */
	public final int row() {
		return myRow;
	}

	/**
	 * 
	 * @return the second coordinate.
	 */
	public final int col() {
		return myCol;
	}

	/**
	 * Constructor.
	 * 
	 * @param row
	 *            The first coordinate
	 * @param col
	 *            The second coordinate
	 */
	public Point(final int row, final int col) {
		myRow = row;
		myCol = col;
	}

	/**
	 * 
	 * @param obj
	 *            the other object
	 * @return whether this object equals another.
	 */
	@Override
	public final boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof Point && (((Point) obj).myRow == myRow && ((Point) obj).myCol == myCol));
	}

	/**
	 * 
	 * @return a hash code.
	 */
	@Override
	public final int hashCode() {
		return myRow | myCol;
	}

	/**
	 * @param point
	 *            another point
	 * 
	 * @return the result of a comparison with that point
	 */
	@Override
	public int compareTo(final Point point) {
		return ((point.row() - row()) << 7) + (point.col() - col());
	}

	/**
	 * 
	 * @return a String representation of the class
	 */
	@Override
	public String toString() {
		return "point (" + myRow + ", " + myCol + ")";
	}
}
