package drivers.map_viewer;

import lovelace.util.Range;
import java.util.Objects;

/**
 * The minimum and maximum rows and columns drawn.
 *
 * TODO: Tests
 */
public class VisibleDimensions {
	public VisibleDimensions(final int minimumRow, final int maximumRow, final int minimumColumn, final int maximumColumn) {
		this.minimumRow = minimumRow;
		this.maximumRow = maximumRow;
		this.minimumColumn = minimumColumn;
		this.maximumColumn = maximumColumn;
		rows = new Range(minimumRow, maximumRow);
		columns = new Range(minimumColumn, maximumColumn);
	}

	/**
	 * The lowest(-numbered) (top-most) row that we draw.
	 */
	private final int minimumRow;

	/**
	 * The highest(-numbered) (bottom-most) row that we draw.
	 */
	private final int maximumRow;

	/**
	 * The lowest (left-most) column we draw.
	 */
	private final int minimumColumn;

	/**
	 * The highest (right-most) column we draw.
	 */
	private final int maximumColumn;

	/**
	 * The lowest(-numbered) (top-most) row that we draw.
	 */
	public int getMinimumRow() {
		return minimumRow;
	}

	/**
	 * The highest(-numbered) (bottom-most) row that we draw.
	 */
	public int getMaximumRow() {
		return maximumRow;
	}

	/**
	 * The lowest (left-most) column we draw.
	 */
	public int getMinimumColumn() {
		return minimumColumn;
	}

	/**
	 * The highest (right-most) column we draw.
	 */
	public int getMaximumColumn() {
		return maximumColumn;
	}

	/**
	 * The rows that we draw.
	 */
	private final Range rows;

	/**
	 * The columns that we draw.
	 */
	private final Range columns;

	/**
	 * The rows that we draw.
	 */
	public Range getRows() {
		return rows;
	}

	/**
	 * The columns that we draw.
	 */
	public Range getColumns() {
		return columns;
	}

	@Override
	public String toString() {
		return String.format("VisibleDimensions: (%d, %d) to (%d, %d)", minimumRow,
			minimumColumn, maximumRow, maximumColumn);
	}

	/**
	 * The number of columns visible.
	 */
	public int getWidth() {
		return columns.size();
	}

	/**
	 * The number of rows visible.
	 */
	public int getHeight() {
		return rows.size();
	}

	@Override
	public boolean equals(final Object that) {
		return that instanceof VisibleDimensions &&
				((VisibleDimensions) that).minimumRow == minimumRow &&
				((VisibleDimensions) that).maximumRow == maximumRow &&
				((VisibleDimensions) that).minimumColumn == minimumColumn &&
				((VisibleDimensions) that).maximumColumn == maximumColumn;
	}

	@Override
	public int hashCode() {
		return Objects.hash(minimumRow, maximumRow, minimumColumn, maximumColumn);
	}
}
