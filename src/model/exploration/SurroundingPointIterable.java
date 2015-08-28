package model.exploration;

import static model.map.PointFactory.point;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import model.map.MapDimensions;
import model.map.Point;
import util.NullCleaner;

/**
 * An iterator over the twenty-five points (including itself) surrounding a
 * point.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2014-2014 Jonathan Lovelace
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
 * @author Jonathan Lovelace
 */
public class SurroundingPointIterable implements Iterable<@NonNull Point> {
	/**
	 * Two below the point.
	 */
	private static final int TWO_BELOW = -2;
	/**
	 * Two above the point. (Since we loop to an exclusive bound, using
	 * less-than not less-than-or-equals)
	 */
	private static final int TWO_ABOVE = 3;
	/**
	 * How many times the current point needs to be added to make it come up
	 * half the time.
	 *
	 * (TODO: is that really what's going on? And should we perhaps just add
	 * the current tile in one of the other loops instead of giving it its
	 * own?)
	 */
	private static final int HALF_COUNT = 14;
	/**
	 * the list of points.
	 */
	private final List<Point> points = new ArrayList<>();
	/**
	 * @param starting the starting point.
	 * @param dimensions the dimensions of the map
	 */
	public SurroundingPointIterable(final Point starting,
			final MapDimensions dimensions) {
		for (int row = TWO_BELOW; row < TWO_ABOVE; row++) {
			for (int col = TWO_BELOW; col < TWO_ABOVE; col++) {
				points.add(point(roundRow(starting.row + row, dimensions),
						roundCol(starting.col + col, dimensions)));
			}
		}
		for (int row = -1; row < 2; row++) {
			for (int col = -1; col < 2; col++) {
				points.add(point(roundRow(starting.row + row, dimensions),
						roundCol(starting.col + col, dimensions)));
			}
		}
		for (int i = 0; i < HALF_COUNT; i++) {
			points.add(starting);
		}
	}
	/**
	 * @return an iterator over the points
	 */
	@Override
	public Iterator<Point> iterator() {
		return NullCleaner.assertNotNull(points.iterator());
	}
	/**
	 * Round a column number to fit within the map.
	 * @param col the column number
	 * @param dims the dimensions of the map
	 * @return its equivalent that's actually within the map
	 */
	private static int roundCol(final int col, final MapDimensions dims) {
		if (col < 0) {
			return dims.cols + col; // NOPMD
		} else {
			return col % dims.cols;
		}
	}
	/**
	 * Round a row number to fit within the map.
	 * @param row the row number
	 * @param dims the dimensions of the map
	 * @return its equivalent that's actually within the map
	 */
	private static int roundRow(final int row, final MapDimensions dims) {
		if (row < 0) {
			return dims.rows + row; // NOPMD
		} else {
			return row % dims.rows;
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SurroundingPointIterable";
	}
}