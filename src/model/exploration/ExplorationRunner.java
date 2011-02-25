package model.exploration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
	private static final Map<String, EncounterTable> TABLES;
	/**
	 * A list of tables to load.
	 */
	private static final String[] TABLE_LIST = { "major_rock",
			"boreal_major_tree", "temperate_major_tree" };
	static {
		TABLES = new HashMap<String, EncounterTable>();
		for (String table : TABLE_LIST) {
			TABLES.put(table,
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
	private static QuadrantTable tryLoading(final String filename,
			final int defaultRows, final List<String> defaultItems) {
		try {
			return new TableLoader().loadQuadrantTable(filename); // NOPMD
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error loading the table from "
					+ filename, e);
			return new QuadrantTable(defaultRows, new LinkedList<String>(
					defaultItems));
		}
	}

	/**
	 * @param tile
	 *            a tile
	 * @return the main kind of rock on the tile
	 */
	public String getPrimaryRock(final Tile tile) {
		return TABLES.get("major_rock").generateEvent(tile);
	}

	/**
	 * @param tile
	 *            a forest tile
	 * @return the main kind of tree on the tile
	 */
	public String getPrimaryTree(final Tile tile) {
		if (TileType.BorealForest.equals(tile.getType())) {
			return TABLES.get("boreal_major_tree").generateEvent(tile); // NOPMD
		} else if (TileType.TemperateForest.equals(tile.getType())) {
			return TABLES.get("temperate_major_tree").generateEvent(tile);
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
	private static List<String> createList(final String stem,
			final int iterations) {
		if (iterations == 0) {
			return new ArrayList<String>(); // NOPMD
		} else {
			final List<String> list = createList(stem, iterations - 1);
			list.add(stem + iterations);
			return list;
		}
	}
}
