package model.mining;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import model.map.Point;
import model.map.PointFactory;
import util.NullCleaner;
import view.util.SystemOut;

/**
 * A class to model the distribution of a mineral to be mined. Note that the
 * constructor can be *very* computationally expensive!
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
 *
 */
public final class MiningModel {
	/**
	 * A mapping from positions (normalized so they could be spit out into a
	 * spreadsheet) to LodeStatuses.
	 */
	private final Map<Point, LodeStatus> data = new HashMap<>();
	/**
	 * The max row and col we get to.
	 */
	private final Point maxPoint;
	/**
	 * Constructor.
	 * @param initial the status to give to the mine's starting point
	 * @param seed a number to seed the RNG
	 */
	public MiningModel(final LodeStatus initial, final long seed) {
		final Map<Point, LodeStatus> unnormalized = new HashMap<>();
		unnormalized.put(PointFactory.point(0, 0), initial);
		final Random rng = new Random(seed);
		final Queue<Point> queue = new LinkedList<>();
		queue.add(PointFactory.point(0, 0));
		long counter = 0;
		long pruneCounter = 0;
		while (!queue.isEmpty()) {
			final Point point = queue.remove();
			counter++;
			if (counter % 100000 == 0) {
				System.out.println(point);
			} else if (counter % 1000 == 0) {
				System.out.append('.');
			}
			// Limit the size of the output spreadsheet
			if (Math.abs(point.row) > 400) {
				pruneCounter++;
				continue;
			} else if (Math.abs(point.col) > 300) {
				pruneCounter++;
				continue;
			}
			final Point left = PointFactory.point(point.row, point.col - 1);
			final Point down = PointFactory.point(point.row + 1, point.col);
			final Point right = PointFactory.point(point.row, point.col + 1);
			LodeStatus current;
			if (unnormalized.containsKey(point)) {
				current = unnormalized.get(point);
			} else {
				current = null;
			}
			if (current == null || LodeStatus.None == current) {
				continue;
			}
			if (!unnormalized.containsKey(right)) {
				unnormalized.put(right, LodeStatus.adjacent(current, rng));
				queue.add(right);
			}
			if (!unnormalized.containsKey(down)) {
				unnormalized.put(down, LodeStatus.adjacent(current, rng));
				queue.add(down);
			}
			if (!unnormalized.containsKey(left)) {
				unnormalized.put(left, LodeStatus.adjacent(current, rng));
				queue.add(left);
			}
		}
		SystemOut.SYS_OUT.printf("%nPruned %d branches beyond our boundaries%n", Long.valueOf(pruneCounter));
		final int minCol =
				getMinCol(NullCleaner.assertNotNull(unnormalized.keySet()));
		for (final Map.Entry<Point, LodeStatus> entry : unnormalized.entrySet()) {
			Point key = entry.getKey();
			data.put(PointFactory.point(key.row, key.col - minCol),
					entry.getValue());
		}
		maxPoint = createMaxPoint(NullCleaner.assertNotNull(data.keySet()));
	}
	/**
	 * @return the bottom-right corner
	 */
	public Point getMaxPoint() {
		return maxPoint;
	}
	/**
	 * @param point a point
	 * @return what's there
	 */
	public LodeStatus statusAt(final Point point) {
		if (data.containsKey(point)) {
			return NullCleaner.assertNotNull(data.get(point));
		} else {
			return LodeStatus.None;
		}
	}
	/**
	 * @param set a set of Points
	 * @return the lowest column in the set
	 */
	private static int getMinCol(final Iterable<Point> set) {
		int retval = 0;
		for (final Point point : set) {
			if (point.col < retval) {
				retval = point.col;
			}
		}
		return retval;
	}
	/**
	 * @param set a set of points
	 * @return a Point with the highest column and row in the set
	 */
	private static Point createMaxPoint(final Iterable<Point> set) {
		int maxCol = 0;
		int maxRow = 0;
		for (final Point point : set) {
			if (point.col > maxCol) {
				maxCol = point.col;
			}
			if (point.row > maxRow) {
				maxRow = point.row;
			}
		}
		return PointFactory.point(maxRow, maxCol);
	}
	/**
	 * @return a string representation of this class
	 */
	@Override
	public String toString() {
		return "MiningModel";
	}
}
