package model.exploration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.viewer.Tile;
import model.viewer.TileType;
import controller.exploration.TableLoader;

/**
 * A class to create exploration results. The initial implementation is a bit
 * hackish, and should be generalized and improved.
 * 
 * FIXME: So that this could eventually run in a server environment, make
 * everything here non-static, so we can have multiple instances with different
 * data.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class ExplorationRunner {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(ExplorationRunner.class.getName());

	/**
	 * @param tile
	 *            a tile
	 * @return what the owner of a fortress on the tile knows
	 */
	public String defaultResults(final Tile tile) {
		final StringBuilder sb = new StringBuilder( // NOPMD
				"The primary rock type here is ");
		sb.append(getPrimaryRock(tile));
		sb.append(".\n");
		if (TileType.BorealForest.equals(tile.getType())
				|| TileType.TemperateForest.equals(tile.getType())) {
			sb.append("The main kind of tree is ");
			sb.append(getPrimaryTree(tile));
			sb.append(".\n");
		}
		return sb.toString();
	}

	/**
	 * The tables we know about.
	 */
	private final Map<String, EncounterTable> tables = new HashMap<String, EncounterTable>();
	/**
	 * A list of tables to load.
	 */
	private final String[] defaultTableList = { "major_rock", "minor_rock",
			"boreal_major_tree", "temperate_major_tree" };
	/**
	 * Loads the default set of tables.
	 */
	public void loadDefaultTables() {
		for (String table : defaultTableList) {
			tables.put(table,
					tryLoading("tables/" + table, 2, createList(table, 4)));
		}
	}

	/**
	 * Try to load a table from file, but log the error and use the given backup
	 * if it fails.
	 * 
	 * @param filename
	 *            the file to load from
	 * @param defaultRows
	 *            the number of rows to use if loading fails
	 * @param defaultItems
	 *            a list of items to use if loading fails
	 * @return a valid table, from file if that works, using the default data if
	 *         not.
	 */
	private static EncounterTable tryLoading(final String filename,
			final int defaultRows, final List<String> defaultItems) {
		try {
			return new TableLoader().loadTable(filename); // NOPMD
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error loading the table from "
					+ filename, e);
			return new QuadrantTable(defaultRows, new LinkedList<String>(
					defaultItems));
		}
	}

	/**
	 * Add a table. This is package-visibility so our test-case can use it.
	 * 
	 * @param name
	 *            The name to add the table under
	 * @param table
	 *            the table.
	 */
	void loadTable(final String name, final EncounterTable table) { // NOPMD
		tables.put(name, table);
	}

	/**
	 * @param tile
	 *            a tile
	 * @return the main kind of rock on the tile
	 */
	public String getPrimaryRock(final Tile tile) {
		return tables.get("major_rock").generateEvent(tile);
	}

	/**
	 * @param tile
	 *            a forest tile
	 * @return the main kind of tree on the tile
	 */
	public String getPrimaryTree(final Tile tile) {
		if (TileType.BorealForest.equals(tile.getType())) {
			return tables.get("boreal_major_tree").generateEvent(tile); // NOPMD
		} else if (TileType.TemperateForest.equals(tile.getType())) {
			return tables.get("temperate_major_tree").generateEvent(tile);
		} else {
			throw new IllegalArgumentException(
					"Only forests have primary trees");
		}
	}

	/**
	 * Create a list of strings, each beginning with a specified stem and ending
	 * with a sequential number.
	 * 
	 * @param stem
	 *            the string to begin each item with
	 * @param iterations
	 *            how many items should be in the list
	 * @return such a list
	 */
	private List<String> createList(final String stem,
			final int iterations) {
		if (iterations == 0) {
			return new ArrayList<String>(); // NOPMD
		} else {
			final List<String> list = createList(stem, iterations - 1);
			list.add(stem + iterations);
			return list;
		}
	}

	/**
	 * Consult a table. (Look up the given tile if it's a quadrant table, roll
	 * on it if it's a random-encounter table.) Note that the result may be the
	 * name of another table, which should then be consulted.
	 * 
	 * @param table
	 *            the name of the table to consult
	 * @param tile
	 *            the tile to refer to
	 * @return the result of the consultation
	 */
	public String consultTable(final String table, final Tile tile) {
		return tables.get(table).generateEvent(tile);
	}

	/**
	 * Consult a table, and if the result indicates recursion, perform it.
	 * Recursion is indicated by hash-marks around the name of the table to
	 * call; results are undefined if there are more than two hash marks in any
	 * given string, or if either is at the beginning or the end of the string,
	 * since we use String.split .
	 * 
	 * @param table
	 *            the name of the table to consult
	 * @param tile
	 *            the tile to refer to
	 * @return the result of the consultation
	 */
	public String recursiveConsultTable(final String table, final Tile tile) {
		String result = consultTable(table, tile);
		if (result.contains("#")) {
			final String[] split = result.split("#", 3);
			if (split.length < 3) {
				result = split[0] + recursiveConsultTable(split[1], tile);
			} else {
				result = split[0] + recursiveConsultTable(split[1], tile)
						+ split[2];
			}
		}
		return result;
	}
}
