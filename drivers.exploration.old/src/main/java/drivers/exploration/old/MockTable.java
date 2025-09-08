package drivers.exploration.old;

import org.jspecify.annotations.Nullable;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Stream;

import legacy.map.Point;
import legacy.map.TileType;
import legacy.map.TileFixture;
import legacy.map.MapDimensions;

/**
 * A mock {@link EncounterTable} for the apparatus to test the
 * ExplorationRunner, to produce the events the tests want in the order they
 * want, and guarantee that the runner never calls {@link #getAllEvents}.
 */
final class MockTable implements EncounterTable {
	private final Deque<String> queue = new LinkedList<>();

	public MockTable(final String... values) {
		Stream.of(values).forEach(queue::addLast);
	}

	@Override
	public String generateEvent(final Point point, final @Nullable TileType terrain, final TerrainModifier terrainMod,
	                            final Iterable<TileFixture> fixtures, final MapDimensions mapDimensions) {
		return queue.removeFirst();
	}

	@Override
	public Set<String> getAllEvents() {
		throw new IllegalStateException("Don't call MockTable#getAllEvents");
	}
}
