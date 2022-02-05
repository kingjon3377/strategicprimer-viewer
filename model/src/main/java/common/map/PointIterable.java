package common.map;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.Nullable;

/**
 * A view of locations on the map in order, starting at a given point.
 */
public class PointIterable implements Iterable<Point> {

	/**
	 * The dimensions of the map we're a view of.
	 */
	private final MapDimensions dimensions;

	/**
	 * Whether we should search forwards (if true) or backwards (if false).
	 *
	 * TODO: convert to enum
	 */
	private final boolean forwards;

	/**
	 * Whether we should search horizontally (if true) or vertically (if false)
	 *
	 * TODO: convert to enum
	 */
	private final boolean horizontal;

	/**
	 * The selected point; we start from (just before) (0, 0) if omitted.
	 */
	@Nullable
	private final Point selection;

	public PointIterable(final MapDimensions dimensions, final boolean forwards, final boolean horizontal, final Point selection) {
		this.dimensions = dimensions;
		this.forwards = forwards;
		this.horizontal = horizontal;
		this.selection = selection;
	}

	public PointIterable(final MapDimensions dimensions, final boolean forwards, final boolean horizontal) {
		this.dimensions = dimensions;
		this.forwards = forwards;
		this.horizontal = horizontal;
		this.selection = null;
	}

	// TODO: convert to static class
	private class PointIteratorImpl implements Iterator<Point> {
		/**
		 * The maximum row in the map.
		 */
		private final int maxRow = dimensions.getRows() - 1;

		/**
		 * The maximum column in the map.
		 */
		private final int maxColumn = dimensions.getColumns() - 1;

		/**
		 * The row where we started.
		 */
		private final int startRow;

		/**
		 * The column where we started.
		 */
		private final int startColumn;

		/**
		 * If {@link item} is zero or positive, return it; otherwise, return {@link wrap}.
		 */
		int wrap(final int item, final int wrap) {
			return (item < 0) ? wrap : item;
		}

		public PointIteratorImpl() {
			if (selection != null) {
				startRow = wrap(selection.getRow(), maxRow);
				startColumn = wrap(selection.getColumn(), maxColumn);
			} else if (forwards) {
				startRow = maxRow;
				startColumn = maxColumn;
			} else {
				startRow = 0;
				startColumn = 0;
			}
			row = startRow;
			column = startColumn;
		}

		/**
		 * The current row.
		 */
		private int row;

		/**
		 * The current column.
		 */
		private int column;

		/**
		 * Whether we've started iterating.
		 */
		private boolean started = false;

		/**
		 * A diagnostic String.
		 */
		@Override
		public String toString() {
			return String.format("PointIterator: Started at (%d, %d), currently at (%d, %d), searching %sly %swards and no farther than (%d, %d)",
				startRow, startColumn, row, column, (horizontal) ? "horizontal" : "vertical",
				(forwards) ? "for" : "back", maxRow, maxColumn);
		}

		@Override
		public boolean hasNext() {
			return !(started && row == startRow && column == startColumn);
		}

		@Override
		public Point next() throws NoSuchElementException {
			if (started && row == startRow && column == startColumn) {
				throw new NoSuchElementException();
			} else {
				started = true;
				if (horizontal) {
					if (forwards) {
						column++;
						if (column > maxColumn) {
							column = 0;
							row++;
							if (row > maxRow) {
								row = 0;
							}
						}
					} else {
						column--;
						if (column < 0) {
							column = maxColumn;
							row--;
							if (row < 0) {
								row = maxRow;
							}
						}
					}
				} else {
					if (forwards) {
						row++;
						if (row > maxRow) {
							row = 0;
							column++;
							if (column > maxColumn) {
								column = 0;
							}
						}
					} else {
						row--;
						if (row < 0) {
							row = maxRow;
							column--;
							if (column < 0) {
								column = maxColumn;
							}
						}
					}
				}
				return new Point(row, column);
			}
		}
	}

	@Override
	public Iterator<Point> iterator() {
		return new PointIteratorImpl();
	}
}

