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
 * An iterator over the points in a square surrounding a point, with points that
 * are closer appearing in the iterator multiple times.
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class SurroundingPointIterable implements Iterable<@NonNull Point> {
	/**
	 * the list of points.
	 */
	private final List<Point> points = new ArrayList<>();
	/**
	 * Pass the default radius of 2.
	 *
	 * @param starting the starting point.
	 * @param dimensions the dimensions of the map
	 */
	public SurroundingPointIterable(final Point starting,
			final MapDimensions dimensions) {
		this(starting, dimensions, 2);
	}

	/**
	 * @param starting the starting point.
	 * @param dimensions the dimensions of the map
	 * @param radius how far from the starting point to go
	 */
	public SurroundingPointIterable(final Point starting,
			final MapDimensions dimensions, final int radius) {
		for (int rad = radius; rad >= 0; rad--) {
			final int lowerBound = 0 - rad;
			final int upperBound = rad + 1;
			for (int row = lowerBound; row < upperBound; row++) {
				for (int col = lowerBound; col < upperBound; col++) {
					points.add(point(roundRow(starting.row + row, dimensions),
							roundCol(starting.col + col, dimensions)));
				}
			}
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
