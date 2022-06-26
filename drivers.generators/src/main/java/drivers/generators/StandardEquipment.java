package drivers.generators;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import lovelace.util.LovelaceLogger;
import lovelace.util.FileSplitter;
import java.util.List;
import java.util.HashMap;
import java.util.Optional;

/**
 * Standard equipment to always (or nearly always, for level-1 workers) provide
 * workers coming from villages, loaded from file.
 *
 * The first column is either 'Job;Level' or just the Job name; if the former
 * is present the latter is ignored for workers with exactly that level of that
 * Job. (TODO: maybe go with "if nothing specified use the highest level
 * present less than the one provided"?) The second column is a
 * semicolon-separated list of equipment to include.
 *
 * TODO: convert to three-column format once tools in place, with -1 for "any level"
 */
/*package*/ final class StandardEquipment {
	private StandardEquipment() {
	}

	// TODO: Convert to use a multimap once we bring in a dependency
	private static final Map<String, List<String>> EQUIPMENT = init();

	private static Map<String, List<String>> init() {
		final Map<String, List<String>> retval = new HashMap<>();
		final Map<String, String> initial;
		try {
			initial = FileSplitter.getFileContents("generation/standard_equipment.txt",
				x -> x);
		} catch (final IOException except) {
			throw new RuntimeException(except);
		}
		for (final Map.Entry<String, String> entry : initial.entrySet()) {
			final String key = entry.getKey();
			final String val = entry.getValue();
			final String[] keySplit = key.split(";");
			final String innerKey;
			if (keySplit.length == 1) {
				innerKey = key;
			} else if (keySplit.length == 2) {
				innerKey = keySplit[0] + '-' + keySplit[1];
			} else {
				LovelaceLogger.warning("Unexpected format(separator) in std-equipment file");
				continue;
			}
			retval.put(innerKey, List.of(val.split(";")));
		}
		return retval; // FIXME: Copy into a more-aptly-sized map
	}

	public static List<String> standardEquipment(final String job, final int level) {
		return Optional.ofNullable(EQUIPMENT.get(job + '-' + level))
			.orElseGet(() -> Optional.ofNullable(EQUIPMENT.get(job))
				.orElseGet(Collections::emptyList));
	}
}
