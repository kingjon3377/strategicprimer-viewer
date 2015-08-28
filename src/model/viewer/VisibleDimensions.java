package model.viewer;

import util.NullCleaner;


/**
 * The minimum and maximum rows and columns drawn.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
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
		return NullCleaner.assertNotNull(new StringBuilder(256)
				.append("VisibleDimensions: (").append(minRow).append(", ")
				.append(minCol).append(") to (").append(maxRow).append(", ")
				.append(maxCol).append(')').toString());
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
