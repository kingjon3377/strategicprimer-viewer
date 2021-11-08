package common.map.fixtures.towns;

/**
 * Sizes of towns, fortifications, and cities.
 */
public enum TownSize {
	Small("small"),
	Medium("medium"),
	Large("large");

	private TownSize(String str) {
		string = str;
	}

	private final String string;

	@Override public String toString() {
		return string;
	}

	public static TownSize parseTownSize(String size) {
		switch (size) {
		case "small":
			return TownSize.Small;
		case "medium":
			return TownSize.Medium;
		case "large":
			return TownSize.Large;
		default:
			throw new IllegalArgumentException(String.format(
				"Failed to parse TownSize from '%s'", size));
		}
	}
}
