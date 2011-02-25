package model.exploration;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
	 * The primary rock type of each quadrant.
	 */
	private static final QuadrantTable PRIMARY_ROCK;
	/**
	 * The primary tree type of each boreal forest quadrant.
	 */
	private static final QuadrantTable BOREAL_PRIMARY_TREE; // NOPMD
	/**
	 * The primary tree type of each temperate forest quadrant.
	 */
	private static final QuadrantTable TEMPERATE_PRIMARY_TREE; // NOPMD
	static {
		PRIMARY_ROCK = tryLoading("tables/major_rock", 2, Arrays.asList(
				"builtin_rock1", "builtin_rock2", "builtin_rock3",
				"builtin_rock4"));
		BOREAL_PRIMARY_TREE = tryLoading("tables/boreal_major_tree", 2,
				Arrays.asList("btree1", "btree2", "btree3", "btree4"));
		TEMPERATE_PRIMARY_TREE = tryLoading("tables/temperate_major_tree", 2,
				Arrays.asList("ttree1", "ttree2", "ttree3", "ttree4"));
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
	// ESCA-JAVA0076:
	public String getPrimaryRock(final Tile tile) {
		return PRIMARY_ROCK.generateEvent(tile);
	}

	/**
	 * @param tile
	 *            a forest tile
	 * @return the main kind of tree on the tile
	 */
	// ESCA-JAVA0076:
	public String getPrimaryTree(final Tile tile) {
		if (TileType.BorealForest.equals(tile.getType())) {
			return BOREAL_PRIMARY_TREE.generateEvent(tile); // NOPMD
		} else if (TileType.TemperateForest.equals(tile.getType())) {
			return TEMPERATE_PRIMARY_TREE.generateEvent(tile);
		} else {
			throw new IllegalArgumentException(
					"Only forests have primary trees");
		}
	}
}
