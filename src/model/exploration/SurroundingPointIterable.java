package model.exploration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import model.map.MapDimensions;
import model.map.Point;
import org.eclipse.jdt.annotation.NonNull;

import static model.map.PointFactory.point;

/**
 * An iterator over the points in a square surrounding a point, with points that are
 * closer appearing in the iterator multiple times.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2014-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class SurroundingPointIterable implements Iterable<@NonNull Point> {
	/**
	 * the list of points.
	 */
	private final Collection<Point> points = new ArrayList<>();
	/**
	 * The starting point. Used only in diagnostic toString().
	 */
	private final Point startingPoint;

	/**
	 * Pass the default radius of 2.
	 *
	 * @param starting   the starting point.
	 * @param dimensions the dimensions of the map
	 */
	public SurroundingPointIterable(final Point starting,
									final MapDimensions dimensions) {
		this(starting, dimensions, 2);
	}

	/**
	 * Constructor with no defaults.
	 * @param starting   the starting point.
	 * @param dimensions the dimensions of the map
	 * @param radius     how far from the starting point to go
	 */
	public SurroundingPointIterable(final Point starting, final MapDimensions dimensions,
									final int radius) {
		startingPoint = starting;
		for (int rad = radius; rad >= 0; rad--) {
			final int lowerBound = 0 - rad;
			final int upperBound = rad + 1;
			for (int row = lowerBound; row < upperBound; row++) {
				for (int col = lowerBound; col < upperBound; col++) {
					points.add(point(roundRow(starting.getRow() + row, dimensions),
							roundCol(starting.getCol() + col, dimensions)));
				}
			}
		}
	}

	/**
	 * Round a column number to fit within the map.
	 *
	 * @param col  the column number
	 * @param dims the dimensions of the map
	 * @return its equivalent that's actually within the map
	 */
	private static int roundCol(final int col, final MapDimensions dims) {
		if (col < 0) {
			return dims.cols + col;
		} else {
			return col % dims.cols;
		}
	}

	/**
	 * Round a row number to fit within the map.
	 *
	 * @param row  the row number
	 * @param dims the dimensions of the map
	 * @return its equivalent that's actually within the map
	 */
	private static int roundRow(final int row, final MapDimensions dims) {
		if (row < 0) {
			return dims.rows + row;
		} else {
			return row % dims.rows;
		}
	}

	/**
	 * An iterator over the points.
	 * @return an iterator over the points
	 */
	@Override
	public Iterator<Point> iterator() {
		return points.iterator();
	}

	/**
	 * A simple toString().
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "Points surrounding " + startingPoint;
	}
	/**
	 * A Stream view of the points.
	 * @return a Stream view of the points.
	 */
	public Stream<Point> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
}
