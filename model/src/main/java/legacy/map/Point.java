package legacy.map;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;

import org.jetbrains.annotations.Nullable;

/**
 * A structure encapsulating two coordinates: a row and column in the map.
 *
 * @param row    The first coordinate, the point's row.
 * @param column The second coordinate, the point's column.
 */
public record Point(int row, int column) implements Comparable<Point>, Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	/**
	 * The first coordinate, the point's row.
	 */
	@Override
	public int row() {
		return row;
	}

	/**
	 * The second coordinate, the point's column.
	 */
	@Override
	public int column() {
		return column;
	}

	/**
	 * The standard "invalid point."
	 */
	public static final Point INVALID_POINT = new Point(-1, -1);

	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Point(final int objRow, final int objColumn)) {
			return row == objRow && column == objColumn;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return row << 9 + column;
	}

	@Override
	public String toString() {
		return "(" + row + "," + column + ")";
	}

	/**
	 * Compare to another point, by first row and then column.
	 */
	@Override
	public int compareTo(final Point point) {
		return Comparator.comparing(Point::row, Comparator.naturalOrder())
				.thenComparing(Point::column).compare(this, point);
	}

	/**
	 * A point is "valid" if neither row nor column is negative.
	 */
	public boolean isValid() {
		return row >= 0 && column >= 0;
	}
}
