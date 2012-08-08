package model.map;

/**
 * A structure encapsulating two coordinates.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class Point implements Comparable<Point>, XMLWritable {
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
	 * @param row The first coordinate
	 * @param col The second coordinate
	 */
	public Point(final int row, final int col) {
		myRow = row;
		myCol = col;
	}

	/**
	 * 
	 * @param obj the other object
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
	 * @param point another point
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
		return "(" + myRow + ", " + myCol + ")";
	}

	/**
	 * @return an XML representation of the point
	 */
	@Override
	@Deprecated
	public String toXML() {
		return "row=\"" + myRow + "\" column=\"" + myCol + "\"";
	}

	/**
	 * Note that this shouldn't ever be called; Points aren't represented by
	 * tags, they're an implementation detail that shouldn't be exposed to
	 * serialization.
	 * 
	 * @return The name of the file this is to be written to.
	 */
	@Deprecated
	@Override
	public String getFile() {
		return file;
	}

	/**
	 * @param fileName the name of the file this should be written to.
	 */
	@Override
	public void setFile(final String fileName) {
		file = fileName;
	}

	/**
	 * The name of the file this is to be written to.
	 */
	private String file;
}
