package common.map.fixtures.mobile;

import java.io.IOException;
import java.util.Map;

/**
 * Discovery DCs for animal populations based on the kind of animal, loaded
 * from file, and if not present there defaulting to what had been the flat
 * constant DC for the [[Animal]] type.
 *
 * TODO: While better than a per-<em>class</em> constant, this is still an
 * inferior solution: instead, load animals' <em>categories</em> (bird, fish,
 * general), <em>size</em> categories, and stealthiness modifiers, then use
 * those and the number of animals in the population to <em>calculate</em> the
 * DC.
 */
public final class AnimalDiscoveryDCs {
	private static Map<String, Integer> initDcs() {
		try {
			return FileSplitter.getFileContents(
				"animal_data/discovery_dc.txt",
				Integer::parseInt);
		} catch (final IOException except) {
			throw new RuntimeException(except);
		}
	}

	private static final Map<String, Integer> DCS = initDcs();

	public static int get(final String key) {
		if (DCS.containsKey(key)) {
			return DCS.get(key);
		} else {
			return 22;
		}
	}

	public static boolean containsKey(final String key) {
		return DCS.containsKey(key);
	}
}

