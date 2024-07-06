package exploration.common;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import legacy.map.Point;
import legacy.map.MapDimensions;

import java.util.Spliterator;
import java.util.stream.Stream;

/**
 * A stream of the points in a square surrounding a point, with points that are
 * closer appearing multiple times. The default radius is 2.
 */
public class SurroundingPointIterable implements Iterable<Point> {
	public SurroundingPointIterable(final Point startingPoint, final MapDimensions dimensions) {
		this(startingPoint, dimensions, 2);
	}

	private final MapDimensions dimensions;

	public SurroundingPointIterable(final Point startingPoint, final MapDimensions dimensions, final int radius) {
		this.dimensions = dimensions;
		for (int inner = radius; inner >= 0; inner--) {
			final int lowerBound = -inner;
			final int upperBound = inner;
			for (int row = lowerBound; row <= upperBound; row++) {
				for (int column = lowerBound; column <= upperBound; column++) {
					points.add(new Point(roundRow(startingPoint.row() + row),
							roundColumn(startingPoint.column() + column)));
				}
			}
		}
	}

	private int roundColumn(final int column) {
		if (column < 0) {
			// TODO: Should probably pass to roundColumn(), in case column is -1000 or something.
			return dimensions.columns() + column;
		} else {
			return column % dimensions.columns();
		}
	}

	private int roundRow(final int row) {
		if (row < 0) {
			return dimensions.rows() + row;
		} else {
			return row % dimensions.rows();
		}
	}

	private final Collection<Point> points = new ArrayList<>();

	@Override
	public Iterator<Point> iterator() {
		return points.iterator();
	}

	public Stream<Point> stream() {
		return points.stream();
	}

	@Override
	public Spliterator<Point> spliterator() {
		return points.spliterator();
	}
}
