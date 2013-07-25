package model.exploration.old;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.map.Point;
import model.map.Tile;
import model.map.TileType;
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
	private final Map<TileType, String> mapping = new EnumMap<>(
			TileType.class);

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
	 * @param tile a tile
	 * @param point the location of the tile
	 * @return what the table has for that kind of tile.
	 */
	@Override
	public String generateEvent(final Point point, final Tile tile) {
		return mapping.get(tile.getTerrain());
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
