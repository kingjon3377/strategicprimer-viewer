package exploration.common;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import common.map.Point;
import common.map.MapDimensions;

/**
 * A stream of the points in a square surrounding a point, with points that are
 * closer appearing multiple times. The default radius is 2.
 */
public class SurroundingPointIterable implements Iterable<Point> {
	public SurroundingPointIterable(Point startingPoint, MapDimensions dimensions) {
		this(startingPoint, dimensions, 2);
	}

	private final MapDimensions dimensions;

	public SurroundingPointIterable(Point startingPoint, MapDimensions dimensions, int radius) {
		this.dimensions = dimensions;
		for (int inner = radius; inner >= 0; inner--) {
			int lowerBound = 0 - inner;
			int upperBound = inner;
			for (int row = lowerBound; row <= upperBound; row++) {
				for (int column = lowerBound; column <= upperBound; column++) {
					points.add(new Point(roundRow(startingPoint.getRow() + row),
						roundColumn(startingPoint.getColumn() + column)));
				}
			}
		}
	}

	private int roundColumn(int column) {
		if (column < 0) {
			return dimensions.getColumns() + column; // TODO: Should probably pass to roundColumn(), inc case column is -1000 or something.
		} else {
			return column % dimensions.getColumns();
		}
	}

	private int roundRow(int row) {
		if (row < 0) {
			return dimensions.getRows() + row;
		} else {
			return row % dimensions.getRows();
		}
	}

	private final List<Point> points = new ArrayList<>();

	@Override
	public Iterator<Point> iterator() {
		return points.iterator();
	}
}