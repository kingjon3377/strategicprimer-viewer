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
	@Override
	@Deprecated
	public String toXML() {
		return "row=\"" + row + "\" column=\"" + col + "\"";
	}

	/**
	 * Note that this shouldn't ever be called; Points aren't represented by
	 * tags, they're an implementation detail that shouldn't be exposed to
	 * serialization.
	 *
	 * @return The name of the file this is to be written to.
	 * @deprecated Points are an implementation detail irrelevant to the serialized form, so this should never be called.
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
