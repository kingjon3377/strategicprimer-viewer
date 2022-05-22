package drivers.generators;

import java.io.IOException;
import java.util.Map;
import lovelace.util.LovelaceLogger;
import lovelace.util.FileSplitter;
import java.util.function.Predicate;
import common.map.fixtures.mobile.worker.WorkerStats;
import java.util.List;
import java.util.HashMap;

/**
 * Minimum stats to make sense with Job levels for randomly-generated workers
 * coming from villages, loaded from file.
 *
 * File format is "Job;StatAbbr StatValue" or "Job;Level;StatAbbr StatValue" (can be freely mixed)
 */
/*package*/ final class MinimumStats {
	private static final List<String> STATS = List.of("str", "dex", "con", "int", "wis", "cha");

	private static final Map<String, Map<String, Integer>> MINIMUMS = init();

	private MinimumStats() {
	}

	// TODO: Convert to use FileSplitter's three- or four-column version once available
	private static Map<String, Map<String, Integer>> init() {
		final Map<String, Map<String, Integer>> retval = new HashMap<>();
		final Map<String, Integer> initial;
		try {
			initial = FileSplitter.getFileContents("generation/minimum_stats.txt",
				Integer::parseInt);
		} catch (final IOException except) {
			throw new RuntimeException(except);
		}
		for (final Map.Entry<String, Integer> entry : initial.entrySet()) {
			final String key = entry.getKey();
			final int val = entry.getValue();
			final String[] split = key.split(";");
			final String innerKey;
			final String stat;
			if (split.length == 2) {
				innerKey = split[0];
				stat = split[1].toLowerCase();
			} else if (split.length == 3) {
				innerKey = split[0] + '-' + split[1];
				stat = split[2].toLowerCase();
			} else {
				LovelaceLogger.warning("Unexpected format (separator) in minimum-stats file");
				continue;
			}
			if (!STATS.contains(stat)) {
				LovelaceLogger.warning("Unexpected format (stat) in minimum-stats file");
				continue;
			}
			final Map<String, Integer> innerMap;
			if (retval.containsKey(innerKey)) {
				innerMap = retval.get(innerKey);
			} else {
				innerMap = new HashMap<>();
			}
			innerMap.put(stat, val);
			retval.put(innerKey, innerMap);
		}
		return retval;
	}

	private static int getModifierFor(final WorkerStats stats, final String stat) {
		final int val = switch (stat) {
			case "str" -> stats.getStrength();
			case "dex" -> stats.getDexterity();
			case "con" -> stats.getConstitution();
			case "int" -> stats.getIntelligence();
			case "wis" -> stats.getWisdom();
			case "cha" -> stats.getCharisma();
			default -> throw new IllegalArgumentException("Must be a recognized stat label");
		};
		return WorkerStats.getModifier(val);
	}

	private static boolean suitableForKey(final WorkerStats stats, final String key) {
		if (MINIMUMS.containsKey(key)) {
			final Map<String, Integer> mins = MINIMUMS.get(key);
			for (final String stat : STATS) {
				if (mins.containsKey(stat) &&
						getModifierFor(stats, stat) < mins.get(stat)) {
					return false;
				}
			}
		}
		return true;
	}

	public static Predicate<WorkerStats> suitableFor(final String job, final int level) {
		return stats -> suitableForKey(stats, job) && suitableForKey(stats, job + '-' + level);
	}
}
