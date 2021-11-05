package common.map.fixtures.resources;

/**
 * The kinds of stone we know about.
 */
public enum StoneKind {
	Limestone("limestone"),
	Marble("marble"),
	Slate("slate"),
	Pumice("pumice"),
	Conglomerate("conglomerate"),
	Sandstone("sandstone"),
	/**
	 * Laterite should only be found under jungle.
	 */
	Laterite("laterite"),
	Shale("shale");

	private StoneKind(String str) {
		string = str;
	}

	private final String string;
	@Override public String toString() {
		return string;
	}

	public static StoneKind parse(String stone) {
		// TODO: Replace with a HashMap cache?
		for (StoneKind kind : values()) {
			if (stone.equals(kind.toString())) {
				return kind;
			}
		}
		throw new IllegalArgumentException("Failed to parse StoneKind from " + stone);
	}
}
