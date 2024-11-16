package legacy.map.fixtures.resources;

import java.text.ParseException;
import java.util.stream.Stream;

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

	public static ExposureStatus parse(final String str) throws ParseException {
		return Stream.of(values()).filter(v -> v.toString().equals(str)).findAny()
				.orElseThrow(() -> new ParseException("Unexpected exposure status %s".formatted(str), 0));
	}

}
