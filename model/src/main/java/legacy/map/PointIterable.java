package legacy.map;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * A view of locations on the map in order, starting at a given point.
 */
public final class PointIterable implements Iterable<Point> {

	/**
	 * The dimensions of the map we're a view of.
	 */
	private final MapDimensions dimensions;

	/**
	 * Which direction to search.
	 */
	enum IterationDirection {
		/**
		 * Left-to-right, top-to-bottom.
		 */
		Forwards,
		/**
		 * Right-to-left, bottom-to-top.
		 */
		Backwards
	}

	private final IterationDirection direction;

	/**
	 * Which axis is primary in the search.
	 */
	enum IterationOrientation {
		/**
		 * Search across, then down/up.
		 */
		Horizontal,
		/**
		 * Search down/up, then across.
		 */
		Vertical
	}

	private final IterationOrientation orientation;

	/**
	 * The selected point; we start from (just before) (0, 0) if omitted.
	 */
	private final @Nullable Point selection;

	public PointIterable(final MapDimensions dimensions, final IterationDirection direction,
	                     final IterationOrientation orientation, final Point selection) {
		this.dimensions = dimensions;
		this.direction = direction;
		this.orientation = orientation;
		this.selection = selection;
	}

	public PointIterable(final MapDimensions dimensions, final IterationDirection direction,
	                     final IterationOrientation orientation) {
		this.dimensions = dimensions;
		this.direction = direction;
		this.orientation = orientation;
		selection = null;
	}

	/**
	 * @deprecated Use the form taking the enums
	 */
	@Deprecated
	public PointIterable(final MapDimensions dimensions, final boolean forwards, final boolean horizontal,
						 final Point selection) {
		this(dimensions, forwards ? IterationDirection.Forwards : IterationDirection.Backwards,
				horizontal ? IterationOrientation.Horizontal : IterationOrientation.Vertical, selection);
	}

	/**
	 * @deprecated Use the form taking the enums
	 */
	@Deprecated
	public PointIterable(final MapDimensions dimensions, final boolean forwards, final boolean horizontal) {
		this(dimensions, forwards ? IterationDirection.Forwards : IterationDirection.Backwards,
				horizontal ? IterationOrientation.Horizontal : IterationOrientation.Vertical);
	}

	private static final class PointIteratorImpl implements Iterator<Point> {
		/**
		 * The direction of the search.
		 */
		private final IterationDirection direction;

		/**
		 * The orientation of the search.
		 */
		private final IterationOrientation orientation;

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
		                         final IterationDirection direction, final IterationOrientation orientation) {
			maxRow = dimensions.rows() - 1;
			maxColumn = dimensions.columns() - 1;
			this.direction = direction;
			this.orientation = orientation;
			if (Objects.nonNull(selection)) {
				startRow = wrap(selection.row(), maxRow);
				startColumn = wrap(selection.column(), maxColumn);
			} else if (direction == IterationDirection.Forwards) {
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
					PointIterator: Started at (%d, %d), currently at (%d, %d), searching %sly %s and no farther \
					than (%d, %d)"""
					.formatted(startRow, startColumn, row, column, orientation.toString().toLowerCase(),
					direction.toString().toLowerCase(), maxRow, maxColumn);
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
				switch (orientation) {
					case Horizontal -> {
						switch (direction) {
							case Forwards -> {
								column++;
								if (column > maxColumn) {
									column = 0;
									row++;
									if (row > maxRow) {
										row = 0;
									}
								}
							}
							case Backwards -> {
								column--;
								if (column < 0) {
									column = maxColumn;
									row--;
									if (row < 0) {
										row = maxRow;
									}
								}
							}
						}
					}
					case Vertical -> {
						switch (direction) {
							case Forwards -> {
								row++;
								if (row > maxRow) {
									row = 0;
									column++;
									if (column > maxColumn) {
										column = 0;
									}
								}
							}
							case Backwards -> {
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
					}
				}
				return new Point(row, column);
			}
		}
	}

	@Override
	public Iterator<Point> iterator() {
		return new PointIteratorImpl(dimensions, selection, direction, orientation);
	}
}

