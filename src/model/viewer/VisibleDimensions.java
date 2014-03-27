package model.viewer;


/**
 * The minimum and maximum rows and columns drawn.
 *
 * TODO: tests
 *
 * @author kingjon
 *
 */
public class VisibleDimensions {
	/**
	 * The lowest row we draw.
	 */
	private final int minRow;
	/**
	 * The highest row we draw.
	 */
	private final int maxRow;
	/**
	 * The lowest column we draw.
	 */
	private final int minCol;
	/**
	 * The highest column we draw.
	 */
	private final int maxCol;

	/**
	 * Constructor.
	 *
	 * @param minimumRow the minimum row
	 * @param maximumRow the maximum row
	 * @param minimumCol the minimum column
	 * @param maximumCol the maximum column
	 */
	public VisibleDimensions(final int minimumRow, final int maximumRow,
			final int minimumCol, final int maximumCol) {
		minRow = minimumRow;
		maxRow = maximumRow;
		minCol = minimumCol;
		maxCol = maximumCol;
	}

	/**
	 *
	 * @return the minimum visible row
	 */
	public int getMinimumRow() {
		return minRow;
	}

	/**
	 *
	 * @return the maximum visible row
	 */
	public int getMaximumRow() {
		return maxRow;
	}

	/**
	 *
	 * @return the minimum visible column
	 */
	public int getMinimumCol() {
		return minCol;
	}

	/**
	 *
	 * @return the maximum visible column
	 */
	public int getMaximumCol() {
		return maxCol;
	}

	/**
	 *
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		final String retval = new StringBuilder(256).append("VisibleDimensions: (")
				.append(minRow).append(", ").append(minCol)
				.append(") to (").append(maxRow).append(", ")
				.append(maxCol).append(')').toString();
		assert retval != null;
		return retval;
	}

	/**
	 * @param other another VisibleDimensions
	 * @return whether it's the same size as this one.
	 */
	public boolean isSameSize(final VisibleDimensions other) {
		return (maxCol - minCol) == (other.maxCol - other.minCol)
				&& (maxRow - minRow) == (other.maxRow - other.minRow);
	}

	/**
	 * @return the width (in columns) visible.
	 */
	public int getWidth() {
		return maxCol - minCol;
	}

	/**
	 * @return the height (in rows) visible.
	 */
	public int getHeight() {
		return maxRow - minRow;
	}
}
