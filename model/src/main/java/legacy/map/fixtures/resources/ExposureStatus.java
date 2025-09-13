package legacy.map.fixtures.resources;

import lovelace.util.EnumParser;
import lovelace.util.ThrowingFunction;

import java.text.ParseException;

/**
 * Exposure status of ground, mineral resources, etc.
 *
 * @author Jonathan Lovelace
 */
public enum ExposureStatus {
	EXPOSED, HIDDEN;

	@Override
	public String toString() {
		return name().toLowerCase();
	}

	private static final ThrowingFunction<String, ExposureStatus, IllegalArgumentException> PARSER =
			new EnumParser<>(ExposureStatus.class, values().length);

	public static ExposureStatus parse(final String str) throws ParseException {
		return PARSER.apply(str);
	}

}
