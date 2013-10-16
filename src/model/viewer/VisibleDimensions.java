package model.viewer;

import java.io.Serializable;

/**
 * The minimum and maximum rows and columns drawn.
 *
 * TODO: tests
 *
 * @author kingjon
 *
 */
public class VisibleDimensions implements Serializable {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The lowest row we draw.
	 */
	private final int minimumRow;
	/**
	 * The highest row we draw.
	 */
	private final int maximumRow;
	/**
	 * The lowest column we draw.
	 */
	private final int minimumCol;
	/**
	 * The highest column we draw.
	 */
	private final int maximumCol;

	/**
	 * Constructor.
	 *
	 * @param minRow the minimum row
	 * @param maxRow the maximum row
	 * @param minCol the minimum column
	 * @param maxCol the maximum column
	 */
	public VisibleDimensions(final int minRow, final int maxRow,
			final int minCol, final int maxCol) {
		minimumRow = minRow;
		maximumRow = maxRow;
		minimumCol = minCol;
		maximumCol = maxCol;
	}

	/**
	 *
	 * @return the minimum visible row
	 */
	public int getMinimumRow() {
		return minimumRow;
	}

	/**
	 *
	 * @return the maximum visible row
	 */
	public int getMaximumRow() {
		return maximumRow;
	}

	/**
	 *
	 * @return the minimum visible column
	 */
	public int getMinimumCol() {
		return minimumCol;
	}

	/**
	 *
	 * @return the maximum visible column
	 */
	public int getMaximumCol() {
		return maximumCol;
	}

	/**
	 *
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return new StringBuilder(256).append("VisibleDimensions: (")
				.append(minimumRow).append(", ").append(minimumCol)
				.append(") to (").append(maximumRow).append(", ")
				.append(maximumCol).append(')').toString();
	}

	/**
	 * @param other another VisibleDimensions
	 * @return whether it's the same size as this one.
	 */
	public boolean isSameSize(final VisibleDimensions other) {
		return (maximumCol - minimumCol) == (other.maximumCol - other.minimumCol)
				&& (maximumRow - minimumRow) == (other.maximumRow - other.minimumRow);
	}

	/**
	 * @return the width (in columns) visible.
	 */
	public int getWidth() {
		return maximumCol - minimumCol;
	}

	/**
	 * @return the height (in rows) visible.
	 */
	public int getHeight() {
		return maximumRow - minimumRow;
	}
}
