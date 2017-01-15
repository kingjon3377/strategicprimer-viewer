package model.viewer;

/**
 * The minimum and maximum rows and columns drawn.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 *         TODO: tests
 */
public final class VisibleDimensions {
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
	 * The top visible row.
	 * @return the minimum visible row
	 */
	public int getMinimumRow() {
		return minRow;
	}

	/**
	 * The bottom visible row.
	 * @return the maximum visible row
	 */
	public int getMaximumRow() {
		return maxRow;
	}

	/**
	 * The leftmost visible column.
	 * @return the minimum visible column
	 */
	public int getMinimumCol() {
		return minCol;
	}

	/**
	 * The rightmost visible column.
	 * @return the maximum visible column
	 */
	public int getMaximumCol() {
		return maxCol;
	}

	/**
	 * A String representation of the dimensions.
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return String.format("VisibleDimensions: (%d, %d) to (%d, %d)",
				Integer.valueOf(minRow), Integer.valueOf(minCol),
				Integer.valueOf(maxRow), Integer.valueOf(maxCol));
	}

	/**
	 * Whether another VisibleDimensions is the same size as this one.
	 * TODO: remove? IntelliJ says it's unused.
	 * @param other another VisibleDimensions
	 * @return whether it's the same size as this one.
	 */
	public boolean isSameSize(final VisibleDimensions other) {
		return ((maxCol - minCol) == (other.maxCol - other.minCol)) &&
					   ((maxRow - minRow) == (other.maxRow - other.minRow));
	}

	/**
	 * The number of columns visible. (Possibly off-by-one.)
	 * @return the width (in columns) visible.
	 */
	public int getWidth() {
		return maxCol - minCol;
	}

	/**
	 * The number of rows visible. (Possibly off-by-one.)
	 * @return the height (in rows) visible.
	 */
	public int getHeight() {
		return maxRow - minRow;
	}
}
