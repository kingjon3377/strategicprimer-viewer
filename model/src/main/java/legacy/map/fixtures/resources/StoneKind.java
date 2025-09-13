package legacy.map.fixtures.resources;

import lovelace.util.EnumParser;
import lovelace.util.ThrowingFunction;

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

	StoneKind(final String str) {
		string = str;
	}

	private final String string;

	@Override
	public String toString() {
		return string;
	}

	private static final ThrowingFunction<String, StoneKind, IllegalArgumentException> PARSER =
			new EnumParser<>(StoneKind.class, values().length);

	public static StoneKind parse(final String stone) {
		return PARSER.apply(stone);
	}
}
