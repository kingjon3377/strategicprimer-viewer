package drivers.exploration.old;

import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;
import common.map.TileType;
import common.map.Point;
import common.map.MapDimensions;
import common.map.TileFixture;
import lovelace.util.SingletonRandom;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An {@link EncounterTable} where the event is selected at random.
 */
class RandomTable implements EncounterTable {
	private final List<Pair<Integer, String>> table;

	public RandomTable(final Pair<Integer, String>... items) {
		if (items.length == 0) {
			throw new IllegalArgumentException("Need at least one item");
		}
		// FIXME: Double-check that this put the low numbers first
		table = Collections.unmodifiableList(Stream.of(items)
			.sorted(Comparator.comparing(Pair::getValue0))
			.collect(Collectors.toList()));
	}

	/**
	 * Get the first item in the table whose numeric value is above the given value.
	 */
	private String lowestMatch(final int val) {
		return table.stream().filter(p -> val >= p.getValue0()).findFirst()
			.map(Pair::getValue1).orElseThrow(() -> new IllegalStateException("None matched"));
	}

	@Override
	public String generateEvent(final Point point, @Nullable final TileType terrain, final boolean mountainous,
	                            final Iterable<TileFixture> fixtures, final MapDimensions dimensions) {
		return lowestMatch(SingletonRandom.SINGLETON_RANDOM.nextInt(100));
	}

	@Override
	public Set<String> getAllEvents() {
		return table.stream().map(Pair::getValue1).collect(Collectors.toSet());
	}

	@Override
	public String toString() {
		return String.format("RandomTable of %d items", table.size());
	}
}
