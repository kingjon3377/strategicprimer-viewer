package model.viewer;

import java.util.Formatter;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import model.map.MapDimensions;
import model.map.Point;
import model.map.PointFactory;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An view of locations on the map in order, starting at a given point.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class PointIterator implements Iterator<@NonNull Point> {
	/**
	 * Whether we're searching forwards (if true) or backwards (if false).
	 */
	private final boolean forwards;
	/**
	 * Whether we're searching horizontally (if true) or vertically (if false).
	 */
	private final boolean horizontal;
	/**
	 * The maximum row in the map.
	 */
	private final int maxRow;
	/**
	 * The maximum column in the map.
	 */
	private final int maxCol;
	/**
	 * The row where we started.
	 */
	private final int startRow;
	/**
	 * The column where we started.
	 */
	private final int startCol;
	/**
	 * The current row.
	 */
	private int row;
	/**
	 * The current column.
	 */
	private int col;
	/**
	 * Whether we've started iterating.
	 */
	private boolean started = false;

	/**
	 * Constructor.
	 *
	 * @param dims             the dimensions of the map we're helping to go through
	 * @param sel              the selected point; we start from 0, 0 if null
	 * @param searchForwards   Whether we should search forwards (if true) or backwards
	 *                         (if false)
	 * @param searchHorizontal Whether we should search horizontally (if true) or
	 *                         vertically (if false)
	 */
	public PointIterator(final MapDimensions dims, @Nullable final Point sel,
						 final boolean searchForwards, final boolean searchHorizontal) {
		horizontal = searchHorizontal;
		forwards = searchForwards;
		maxRow = dims.getRows() - 1;
		maxCol = dims.getColumns() - 1;
		if (sel == null) {
			if (forwards) {
				startRow = maxRow;
				startCol = maxCol;
			} else {
				startRow = 0;
				startCol = 0;
			}
		} else {
			startRow = wrap(sel.getRow(), maxRow);
			startCol = wrap(sel.getCol(), maxCol);
		}
		row = startRow;
		col = startCol;
	}

	/**
	 * If the given value is negative, return the "wrap" value; otherwise, return the
	 * first given value.
	 * @param val  a value
	 * @param wrap another
	 * @return the first, unless it's negative, in which case the second
	 */
	private static int wrap(final int val, final int wrap) {
		if (val < 0) {
			return wrap;
		} else {
			return val;
		}
	}

	/**
	 * Return a diagnostic string.
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(150);
		try (final Formatter formatter = new Formatter(builder)) {
			formatter.format("PointIterator: Started at (%d, %d), currently at (%d, %d)",
					Integer.valueOf(startRow), Integer.valueOf(startCol),
					Integer.valueOf(row), Integer.valueOf(col));
			if (horizontal) {
				formatter.format(", searching horizontally ");
			} else {
				formatter.format(", searching vertically ");
			}
			if (forwards) {
				formatter.format("forwards");
			} else {
				formatter.format("backwards");
			}
			formatter.format(" and no farther than (%d, %d).", Integer.valueOf(maxRow),
					Integer.valueOf(maxCol));
		}
		return builder.toString();
	}

	/**
	 * Whether there are more points to go; false if we've returned to where we started.
	 * @return false if we've reached where we started.
	 */
	@Override
	public boolean hasNext() {
		if (started) {
			return (row != startRow) || (col != startCol);
		} else {
			return true;
		}
	}

	/**
	 * Get the next point.
	 * @return the next point in the map.
	 */
	@Override
	public Point next() {
		if (hasNext()) {
			started = true;
			if (horizontal) {
				if (forwards) {
					return horizontalNext();
				} else {
					return horizontalPrev();
				}
			} else {
				if (forwards) {
					return verticalNext();
				} else {
					return verticalPrev();
				}
			}
		} else {
			throw new NoSuchElementException("We've reached the end");
		}
	}

	/**
	 * Get the next point, searching horizontally.
	 * @return the next point, searching horizontally.
	 */
	private Point horizontalNext() {
		if (col == maxCol) {
			if (row == maxRow) {
				row = 0;
			} else {
				row++;
			}
			col = 0;
		} else {
			col++;
		}
		return PointFactory.point(row, col);
	}

	/**
	 * Get the previous point, searching horizontally.
	 * @return the previous point, searching horizontally.
	 */
	private Point horizontalPrev() {
		if (col == 0) {
			if (row == 0) {
				row = maxRow;
			} else {
				row--;
			}
			col = maxCol;
		} else {
			col--;
		}
		return PointFactory.point(row, col);
	}

	/**
	 * Get the next point, searching vertically.
	 * @return the next point, searching vertically.
	 */
	private Point verticalNext() {
		if (row == maxRow) {
			if (col == maxCol) {
				col = 0;
			} else {
				col++;
			}
			row = 0;
		} else {
			row++;
		}
		return PointFactory.point(row, col);
	}

	/**
	 * Get the previous point, searching vertically.
	 * @return the previous point, searching vertically.
	 */
	private Point verticalPrev() {
		if (row == 0) {
			if (col == 0) {
				col = maxCol;
			} else {
				col--;
			}
			row = maxRow;
		} else {
			row--;
		}
		return PointFactory.point(row, col);
	}

	/**
	 * Not implemented.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Can't remove a Point from a map.");
	}
	/**
	 * @return a Stream of the points.
	 */
	public Stream<Point> stream() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, 0), false);
	}
}
