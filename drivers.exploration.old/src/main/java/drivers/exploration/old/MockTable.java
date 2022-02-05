package drivers.exploration.old;

import org.jetbrains.annotations.Nullable;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Stream;

import common.map.Point;
import common.map.TileType;
import common.map.TileFixture;
import common.map.MapDimensions;

/**
 * A mock {@link EncounterTable} for the apparatus to test the
 * ExplorationRunner, to produce the events the tests want in the order they
 * want, and guarantee that the runner never calls {@link getAllEvents}.
 */
class MockTable implements EncounterTable {
	private final Deque<String> queue = new LinkedList<>();
	public MockTable(final String... values) {
		Stream.of(values).forEach(queue::addLast);
	}

	@Override
	public String generateEvent(final Point point, @Nullable final TileType terrain, final boolean mountainous,
	                            final Iterable<TileFixture> fixtures, final MapDimensions mapDimensions) {
		return queue.removeFirst();
	}

	@Override
	public Set<String> getAllEvents() {
		throw new IllegalStateException("Don't call MockTable#getAllEvents");
	}
}
