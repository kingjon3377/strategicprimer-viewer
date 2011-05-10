package model.exploration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import model.viewer.Tile;

/**
 * A table for legacy "events."
 * 
 * @author Jonathan Lovelace
 */
public class LegacyTable implements EncounterTable {
	/**
	 * The data structure behind this class.
	 */
	private final Map<Integer, String> map;

	/**
	 * Constructor.
	 * 
	 * @param entryMap
	 *            the entries in the table.
	 */
	public LegacyTable(final Map<Integer, String> entryMap) {
		map = new HashMap<Integer, String>(entryMap);
	}

	/**
	 * @param tile
	 *            the tile in question
	 * @return the event on that tile
	 */
	@Override
	public String generateEvent(final Tile tile) {
		return map.get(tile.getEvent());
	}

	/**
	 * @return all events this table can generate.
	 */
	@Override
	public Set<String> allEvents() {
		return new HashSet<String>(map.values());
	}

}
