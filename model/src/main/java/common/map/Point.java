package common.map;

import java.util.Comparator;
import org.jetbrains.annotations.Nullable;

/**
 * A structure encapsulating two coordinates: a row and column in the map.
 */
public final class Point implements Comparable<Point> {
	/**
	 * The first coordinate, the point's row.
	 */
	private final int row;

	/**
	 * The first coordinate, the point's row.
	 */
	public int getRow() {
		return row;
	}

	/**
	 * The second coordinate, the point's column.
	 */
	private final int column;

	/**
	 * The second coordinate, the point's column.
	 */
	public int getColumn() {
		return column;
	}

	public Point(final int row, final int column) {
		this.row = row;
		this.column = column;
	}

	/**
	 * The standard "invalid point.
	 */
	public static final Point INVALID_POINT = new Point(-1, -1);

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Point) {
			return row == ((Point) obj).getRow() && column == ((Point) obj).getColumn();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return row << 9 + column;
	}

	private volatile @Nullable String string;

	private synchronized String maybeString() {
		if (string == null) {
			// String.format("(%d, %d)", row, column) // perf bottleneck
			string = "(" + row + ", " + column + ")";
		}
		return string;
	}

	@Override
	public String toString() {
		final String str = string;
		return str == null ? maybeString() : str;
	}

	/**
	 * Compare to another point, by first row and then column.
	 */
	@Override
	public int compareTo(final Point point) {
		return Comparator.comparing(Point::getRow, Comparator.naturalOrder())
				.thenComparing(Point::getColumn).compare(this, point);
	}

	/**
	 * A point is "valid" if neither row nor column is negative.
	 */
	public boolean isValid() {
		return row >= 0 && column >= 0;
	}
}
