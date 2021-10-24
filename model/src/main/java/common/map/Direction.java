package common.map;

import java.util.Objects;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

/**
 * An enumeration of directions of possible travel. Note that the order is
 * chosen to ensure each item's {@link java.lang.Enum#ordinal ordinal} is the
 * same as in the Ceylon version, where it was explicit rather than based on
 * the order of cases. We use the ordinal to get a consistent sort order for UI
 * purposes.
 */
public enum Direction implements Comparable<Direction> {
	Northwest("northwest"),
	North("north"),
	Northeast("northeast"),
	West("west"),
	Nowhere("nowhere"),
	East("east"),
	Southwest("southwest"),
	South("south"),
	Southeast("southeast");
	private Direction(String string) {
		this.string = string;
	}
	/**
	 * A representation of the direction for debugging and UI purposes.
	 */
	private final String string;
	/**
	 * A representation of the direction for debugging and UI purposes.
	 */
	@Override
	public String toString() {
		return string;
	}
	public static @Nullable Direction parse(String direction) {
		return Stream.of(Direction.values())
			.filter((dir) -> Objects.equals(dir.toString(), direction))
			.findAny().orElse(null);
	}
}