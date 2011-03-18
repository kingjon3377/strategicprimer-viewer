package model.exploration;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import model.viewer.Tile;
import model.viewer.TileType;
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
	 * Mapping from terrain type to "events."
	 */
	private final Map<TileType, String> mapping = new EnumMap<TileType, String>(
			TileType.class);
	/**
	 * Constructor.
	 * @param items the items to add to the list
	 */
	public TerrainTable(final List<Pair<TileType, String>> items) {
		for (final Pair<TileType, String> item : items) {
			mapping.put(item.first, item.second);
		}
	}
	/**
	 * @param tile a tile
	 * @return what the table has for that kind of tile.
	 */
	@Override
	public String generateEvent(final Tile tile) {
		return mapping.get(tile.getType());
	}

}
