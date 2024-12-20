package legacy.map;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

/**
 * A view of locations on the map in order, starting at a given point.
 */
public final class PointIterable implements Iterable<Point> {

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
	private final @Nullable Point selection;

	public PointIterable(final MapDimensions dimensions, final boolean forwards, final boolean horizontal,
						 final Point selection) {
		this.dimensions = dimensions;
		this.forwards = forwards;
		this.horizontal = horizontal;
		this.selection = selection;
	}

	public PointIterable(final MapDimensions dimensions, final boolean forwards, final boolean horizontal) {
		this.dimensions = dimensions;
		this.forwards = forwards;
		this.horizontal = horizontal;
		selection = null;
	}

	private static final class PointIteratorImpl implements Iterator<Point> {
		/**
		 * Whether we should search forwards (if true) or backwards (if false).
		 */
		private final boolean forwards;

		/**
		 * Whether we should search horizontally (if true) or vertically (if false)
		 */
		private final boolean horizontal;

		/**
		 * The maximum row in the map.
		 */
		private final int maxRow;

		/**
		 * The maximum column in the map.
		 */
		private final int maxColumn;

		/**
		 * The row where we started.
		 */
		private final int startRow;

		/**
		 * The column where we started.
		 */
		private final int startColumn;

		/**
		 * If "item" is zero or positive, return it; otherwise, return "wrap".
		 */
		private static int wrap(final int item, final int wrap) {
			return (item < 0) ? wrap : item;
		}

		public PointIteratorImpl(final MapDimensions dimensions, final @Nullable Point selection,
		                         final boolean forwards, final boolean horizontal) {
			maxRow = dimensions.rows() - 1;
			maxColumn = dimensions.columns() - 1;
			this.forwards = forwards;
			this.horizontal = horizontal;
			if (Objects.nonNull(selection)) {
				startRow = wrap(selection.row(), maxRow);
				startColumn = wrap(selection.column(), maxColumn);
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
			return """
					PointIterator: Started at (%d, %d), currently at (%d, %d), searching %sly %swards and no farther \
					than (%d, %d)"""
					.formatted(startRow, startColumn, row, column, (horizontal) ? "horizontal" : "vertical",
					(forwards) ? "for" : "back", maxRow, maxColumn);
		}

		@Override
		public boolean hasNext() {
			return !(started && row == startRow && column == startColumn);
		}

		@Override
		public Point next() throws NoSuchElementException {
			if (started && row == startRow && column == startColumn) {
				throw new NoSuchElementException("Iteration finished");
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
		return new PointIteratorImpl(dimensions, selection, forwards, horizontal);
	}
}

