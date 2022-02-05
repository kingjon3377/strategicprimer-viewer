package drivers.exploration.old;

import java.util.HashSet;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;
import common.map.TileType;
import common.map.Point;
import common.map.MapDimensions;
import common.map.TileFixture;
import common.map.fixtures.terrain.Forest;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

/**
 * An {@link EncounterTable} that gives its result based on the terrain type of
 * the tile in question.
 */
class TerrainTable implements EncounterTable {
	private final Map<String, String> mapping = new HashMap<>();
	public TerrainTable(final Pair<String, String>... items) {
		Set<String> types = Stream.concat(Stream.of(TileType.values()).map(TileType::getXml),
				Stream.of("mountain", "boreal_forest", "temperate_forest"))
			.collect(Collectors.toSet());
		for (Pair<String, String> pair : items) {
			if (types.contains(pair.getValue0())) {
				mapping.put(pair.getValue0(), pair.getValue1()); // TODO: check for dupes
			} else {
				throw new IllegalArgumentException("Invalid key in mapping");
			}
		}
	}

	@Override
	public String generateEvent(final Point point, @Nullable final TileType terrain, final boolean mountainous,
	                            final Iterable<TileFixture> fixtures, final MapDimensions mapDimensions) {
		if (terrain == null) {
			throw new IllegalArgumentException(
				"Terrain table can only account for visible terrain");
		}
		final String actual;
		boolean forested = StreamSupport.stream(fixtures.spliterator(), true)
			.anyMatch(Forest.class::isInstance);
		if (mountainous) {
			actual = "mountain";
		} else if (TileType.Plains.equals(terrain) && forested) {
			actual = "temperate_forest";
		} else if (TileType.Steppe.equals(terrain) && forested) {
			actual = "boreal_forest";
		} else {
			actual = terrain.getXml();
		}
		if (mapping.containsKey(actual)) {
			return mapping.get(actual);
		} else if (mapping.containsKey(terrain.getXml())) {
			return mapping.get(terrain.getXml());
		} else {
			throw new IllegalStateException("Table does not account for terrain type " + terrain);
		}
	}

	@Override
	public Set<String> getAllEvents() {
		return new HashSet<>(mapping.values());
	}

	@Override
	public String toString() {
		return String.format("TerrainTable covering %d terrain types", mapping.size());
	}
}
