package model.mining;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import model.map.Point;
import model.map.PointFactory;
import util.NullCleaner;
import view.util.SystemOut;

/**
 * A class to model the distribution of a mineral to be mined. Note that the constructor
 * can be *very* computationally expensive!
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
public final class MiningModel {
	/**
	 * Kinds of mines we know how to create.
	 */
	public enum MineKind {
		/**
		 * "Normal," which *tries* to create randomly-branching "veins".
		 */
		Normal,
		/**
		 * A mine which emphasizes layers, such as a sand mine.
		 */
		Banded
	}
	/**
	 * A mapping from positions (normalized so they could be spit out into a spreadsheet)
	 * to LodeStatuses.
	 */
	private final Map<Point, LodeStatus> data = new HashMap<>();
	/**
	 * The max row and col we get to.
	 */
	private final Point maxPoint;

	/**
	 * Constructor.
	 *
	 * @param initial the status to give to the mine's starting point
	 * @param seed    a number to seed the RNG
	 * @param kind what kind of mine to model
	 */
	@SuppressWarnings("resource")
	public MiningModel(final LodeStatus initial, final long seed, final MineKind kind) {
		final Map<Point, LodeStatus> unnormalized = new HashMap<>();
		unnormalized.put(PointFactory.point(0, 0), initial);
		final Queue<Point> queue = new LinkedList<>();
		queue.add(PointFactory.point(0, 0));
		final Random rng = new Random(seed);
		final Function<LodeStatus, LodeStatus> horizontalGen;
		switch (kind) {
		case Normal:
			horizontalGen = current -> LodeStatus.adjacent(current, rng);
			break;
		case Banded:
			horizontalGen = current -> LodeStatus.bandedAdjacent(current, rng);
			break;
		default:
			throw new IllegalStateException("Unimplemented mine type");
		}
		final Function<LodeStatus, LodeStatus> verticalGen =
				current -> LodeStatus.adjacent(current, rng);
		long counter = 0;
		long pruneCounter = 0;
		while (!queue.isEmpty()) {
			final Point point = queue.remove();
			counter++;
			if ((counter % 100000) == 0) {
				SystemOut.SYS_OUT.println(point);
			} else if ((counter % 1000) == 0) {
				SystemOut.SYS_OUT.append('.');
			}
			// Limit the size of the output spreadsheet
			if ((Math.abs(point.getRow()) > 200) || (Math.abs(point.getCol()) > 100)) {
				pruneCounter++;
				continue;
			} else {
				final Point left = PointFactory.point(point.getRow(), point.getCol() - 1);
				final Point down = PointFactory.point(point.getRow() + 1, point.getCol());
				final Point right = PointFactory.point(point.getRow(), point.getCol() + 1);
				final LodeStatus current;
				if (unnormalized.containsKey(point)) {
					current = unnormalized.get(point);
				} else {
					current = LodeStatus.None;
				}
				if ((current == null) || (LodeStatus.None == current)) {
					continue;
				}
				if (!unnormalized.containsKey(right)) {
					unnormalized.put(right, horizontalGen.apply(current));
					queue.add(right);
				}
				if (!unnormalized.containsKey(down)) {
					unnormalized.put(down, verticalGen.apply(current));
					queue.add(down);
				}
				if (!unnormalized.containsKey(left)) {
					unnormalized.put(left, horizontalGen.apply(current));
					queue.add(left);
				}
			}
		}
		SystemOut.SYS_OUT.printf("%nPruned %d branches beyond our boundaries%n",
				Long.valueOf(pruneCounter));
		final int minCol =
				getMinCol(NullCleaner.assertNotNull(unnormalized.keySet()));
		for (final Map.Entry<Point, LodeStatus> entry : unnormalized.entrySet()) {
			final Point key = entry.getKey();
			data.put(PointFactory.point(key.getRow(), key.getCol() - minCol),
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
		return StreamSupport.stream(set.spliterator(), false).mapToInt(Point::getCol)
					   .min().orElse(0);
	}

	/**
	 * @param set a set of points
	 * @return a Point with the highest column and row in the set
	 */
	private static Point createMaxPoint(final Iterable<Point> set) {
		int maxCol = 0;
		int maxRow = 0;
		// I would use Stream operations for this, but that would require two passes
		// (or a better understanding than I have), while a manual loop can do it in one
		for (final Point point : set) {
			if (point.getCol() > maxCol) {
				maxCol = point.getCol();
			}
			if (point.getRow() > maxRow) {
				maxRow = point.getRow();
			}
		}
		return PointFactory.point(maxRow, maxCol);
	}

	/**
	 * @return a string representation of this class
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "MiningModel";
	}
}
