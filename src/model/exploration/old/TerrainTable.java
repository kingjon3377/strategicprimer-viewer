package model.exploration.old;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.map.Point;
import model.map.TileFixture;
import model.map.TileType;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;
import util.Pair;

/**
 * A table that gives its result based on the terrain type of the tile in
 * question.
 *
 * @author Jonathan Lovelace
 *
 */
public class TerrainTable implements EncounterTable {
	/**
	 * Mapping from terrain type to "events".
	 */
	private final Map<TileType, String> mapping = new EnumMap<>(TileType.class);

	/**
	 * Constructor.
	 *
	 * @param items the items to add to the list
	 */
	public TerrainTable(final List<Pair<TileType, String>> items) {
		for (final Pair<TileType, String> item : items) {
			mapping.put(item.first(), item.second());
		}
	}

	/**
	 * @param terrain the terrain at the location
	 * @param fixtures ignored
	 * @param point the location of the tile
	 * @return what the table has for that kind of tile.
	 */
	@Override
	public String generateEvent(final Point point, final TileType terrain,
			@Nullable final Iterable<TileFixture> fixtures) {
		if (mapping.containsKey(terrain)) {
			return NullCleaner.assertNotNull(mapping.get(terrain));
		} else {
			throw new IllegalArgumentException(
					"Table does not account for that terrain type");
		}
	}

	/**
	 * @return all events that this table can produce.
	 */
	@Override
	public Set<String> allEvents() {
		return new HashSet<>(mapping.values());
	}

	/**
	 *
	 * @return a String representation of the class
	 */
	@Override
	public String toString() {
		return "TerrainTable";
	}
}
