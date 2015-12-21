package model.viewer;

import model.map.MapDimensions;
import model.map.Point;
import model.map.PointFactory;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An view of locations on the map in order, starting at a given point.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
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
	private final boolean horiz;
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
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(150);
		builder.append("PointIterator: Started at (");
		builder.append(startRow);
		builder.append(", ");
		builder.append(startCol);
		builder.append("), currently at (");
		builder.append(row);
		builder.append(", ");
		builder.append(col);
		builder.append("), searching ");
		if (horiz) {
			builder.append("horizontally ");
		} else {
			builder.append("vertically ");
		}
		if (forwards) {
			builder.append("forwards ");
		} else {
			builder.append("backwards ");
		}
		builder.append("and no farther than (");
		builder.append(maxRow);
		builder.append(", ");
		builder.append(maxCol);
		builder.append(").");
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * @param val  a value
	 * @param wrap another
	 * @return the first, unless it's negative, in which case the second
	 */
	private static int wrap(final int val, final int wrap) {
		if (val < 0) {
			return wrap; // NOPMD
		} else {
			return val;
		}
	}

	/**
	 * Constructor.
	 *
	 * @param dims           the dimensions of the map we're helping to go through
	 * @param sel            the selected point; we start from 0, 0 if null
	 * @param searchForwards Whether we should search forwards (if true) or backwards (if
	 *                       false)
	 * @param searchHoriz    Whether we should search horizontally (if true) or
	 *                          vertically
	 *                       (if false)
	 */
	public PointIterator(final MapDimensions dims, @Nullable final Point sel,
	                     final boolean searchForwards, final boolean searchHoriz) {
		horiz = searchHoriz;
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
			startRow = wrap(sel.row, maxRow);
			startCol = wrap(sel.col, maxCol);
		}
		row = startRow;
		col = startCol;
	}

	/**
	 * @return false if we've reached where we started.
	 */
	@Override
	public boolean hasNext() {
		if (started) {
			return (row != startRow) || (col != startCol); // NOPMD
		} else {
			return true; // NOPMD
		}
	}

	/**
	 * @return the next point in the map.
	 */
	@Override
	public Point next() {
		if (hasNext()) {
			started = true;
			if (horiz) {
				if (forwards) {
					return horizNext(); // NOPMD
				} else {
					return horizPrev(); // NOPMD
				}
			} else {
				if (forwards) {
					return vertNext(); // NOPMD
				} else {
					return vertPrev();
				}
			}
		} else {
			throw new NoSuchElementException("We've reached the end");
		}
	}

	/**
	 * @return the next point, searching horizontally.
	 */
	private Point horizNext() {
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
	 * @return the previous point, searching horizontally.
	 */
	private Point horizPrev() {
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
	 * @return the next point, searching vertically.
	 */
	private Point vertNext() {
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
	 * @return the previous point, searching vertically.
	 */
	private Point vertPrev() {
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
		throw new UnsupportedOperationException(
				                                       "Can't remove a Point from a map.");
	}
}
