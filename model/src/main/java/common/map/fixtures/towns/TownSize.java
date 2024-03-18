package common.map.fixtures.towns;

/**
 * Sizes of towns, fortifications, and cities.
 */
public enum TownSize {
	Small("small"),
	Medium("medium"),
	Large("large");

	TownSize(final String str) {
		string = str;
	}

	private final String string;

	@Override
	public String toString() {
		return string;
	}

	public static TownSize parseTownSize(final String size) {
		return switch (size) {
			case "small" -> Small;
			case "medium" -> Medium;
			case "large" -> Large;
			default -> throw new IllegalArgumentException("Failed to parse TownSize from '%s'".formatted(size));
		};
	}
}
