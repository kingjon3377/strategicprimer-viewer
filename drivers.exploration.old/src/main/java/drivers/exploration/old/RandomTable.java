package drivers.exploration.old;

import org.javatuples.Pair;
import org.jspecify.annotations.Nullable;
import legacy.map.TileType;
import legacy.map.Point;
import legacy.map.MapDimensions;
import legacy.map.TileFixture;
import lovelace.util.SingletonRandom;

import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * An {@link EncounterTable} where the event is selected at random.
 */
final class RandomTable implements EncounterTable {
	private final List<Pair<Integer, String>> table;

	@SafeVarargs
	public RandomTable(final Pair<Integer, String>... items) {
		if (items.length == 0) {
			throw new IllegalArgumentException("Need at least one item");
		}
		// FIXME: Double-check that this put the low numbers first
		table = Stream.of(items).sorted(Comparator.comparing(Pair::getValue0)).toList();
	}

	/**
	 * Get the first item in the table whose numeric value is above the given value.
	 */
	private String lowestMatch(final int val) {
		return table.stream().filter(p -> val >= p.getValue0()).findFirst()
				.map(Pair::getValue1).orElseThrow(() -> new IllegalStateException("None matched"));
	}

	@Override
	public String generateEvent(final Point point, final @Nullable TileType terrain, final TerrainModifier terrainMod,
								final Iterable<TileFixture> fixtures, final MapDimensions dimensions) {
		return lowestMatch(SingletonRandom.SINGLETON_RANDOM.nextInt(100));
	}

	@Override
	public Set<String> getAllEvents() {
		return table.stream().map(Pair::getValue1).collect(Collectors.toSet());
	}

	@Override
	public String toString() {
		return "RandomTable of %d items".formatted(table.size());
	}
}
