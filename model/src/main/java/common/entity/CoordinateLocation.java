package common.entity;

import org.jetbrains.annotations.NotNull;

/**
 * An entity can be located in a particular region of a map at particular coordinates. Note that this class cannot
 * check region or coordinate upper bounds.
 *
 * @param world The world in which the region is located
 * @param region The ID of the region. TODO: Is this how we want to identify regions?
 * @param row The row within the region where the entity is located
 *            TODO: Is row/column how we want to represent position within a region?
 * @param column The column within the region where the entity is located
 */
public record CoordinateLocation(@NotNull String world, int region, int row, int column) implements Location {
	public CoordinateLocation {
		if (region < -1) {
			throw new IllegalArgumentException("Region must be -1 if unknown, or nonnegative");
		} else if (region < 0 && (row >= 0 || column >= 0)) {
			throw new IllegalArgumentException("Coordinates cannot be known if region is unknown");
		} else if (differentSignImpl(row, column) || differentSignImpl(column, row)) {
			throw new IllegalArgumentException("Either both or neither row and column must be valid");
		}
	}
	private static boolean differentSignImpl(final int one, final int two) {
		return one < 0 && two >= 0;
	}
}
