package model.exploration;

import java.util.Set;

import model.viewer.Tile;

/**
 * An interface for encounter tables, both quadrant and random-event tables. At
 * present we assume this is for the Judge's use; to produce output a player can
 * see unmodified we need to be able to know the explorer's Perception modifier
 * and perhaps other data.
 * 
 * @author Jonathan Lovelace
 * 
 */
public interface EncounterTable {
	/**
	 * Generates an "encounter." For QuadrantTables this is always the same for
	 * each tile; for random event tables the result will be randomly selected
	 * from that table.
	 * 
	 * @param tile
	 *            a tile
	 * @return an appropriate event for that tile
	 */
	String generateEvent(final Tile tile);

	/**
	 * For table-debugging purposes.
	 * 
	 * @return all events the table can return.
	 */
	Set<String> allEvents();
}
