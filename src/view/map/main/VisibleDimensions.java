package view.map.main;
/**
 * The minimum and maximum rows and columns drawn.
 * @author kingjon
 *
 */
public class VisibleDimensions {
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
	 * @param minRow the minimum row
	 * @param maxRow the maximum row
	 * @param minCol the minimum column
	 * @param maxCol the maximum column
	 */
	public VisibleDimensions(final int minRow, final int maxRow, final int minCol, final int maxCol) {
		minimumRow = minRow;
		maximumRow = maxRow;
		minimumCol = minCol;
		maximumCol = maxCol;
	}
	/**
	 * @return the minimum visible row
	 */
	public int getMinimumRow() {
		return minimumRow;
	}
	/**
	 * @return the maximum visible row
	 */
	public int getMaximumRow() {
		return maximumRow;
	}
	/**
	 * @return the minimum visible column
	 */
	public int getMinimumCol() {
		return minimumCol;
	}
	/**
	 * @return the maximum visible column
	 */
	public int getMaximumCol() {
		return maximumCol;
	}
}
