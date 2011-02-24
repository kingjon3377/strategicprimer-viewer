package model.exploration;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
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
	 * FIXME: This should be read from file, not embedded in code. The primary
	 * rock type of each quadrant.
	 */
	private static final QuadrantResult PRIMARY_ROCK;
	/**
	 * FIXME: This should be read from file, not embedded in code. The primary
	 * tree type of each boreal forest quadrant.
	 */
	private static final QuadrantResult BOREAL_PRIMARY_TREE; // NOPMD
	/**
	 * FIXME: This should be read from file, not embedded in code. The primary
	 * tree type of each temperate forest quadrant.
	 */
	private static final QuadrantResult TEMPERATE_PRIMARY_TREE; // NOPMD
	static {
		final TableLoader loader = new TableLoader();
		// ESCA-JAVA0177:
		// we have to use a temporary variable or the compiler will complain
		// that the final variable might already be assigned
		QuadrantResult temp;
		try {
			temp = loader.loadQuadrantTable("tables/major_rock");
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,
					"I/O error loading the primary rock table from file", e);
			temp = new QuadrantResult(2, new LinkedList<String>(Arrays.asList(
					"builtin_rock1", "builtin_rock2", "builtin_rock3",
					"builtin_rock4")));
		}
		PRIMARY_ROCK = temp;
		BOREAL_PRIMARY_TREE = new QuadrantResult(2, new LinkedList<String>(
				Arrays.asList("pine", "fir", "larch", "spruce")));
		TEMPERATE_PRIMARY_TREE = new QuadrantResult(2, new LinkedList<String>(
				Arrays.asList("oak", "maple", "beech", "elm")));
	}

	/**
	 * @param tile
	 *            a tile
	 * @return the main kind of rock on the tile
	 */
	// ESCA-JAVA0076:
	public String getPrimaryRock(final Tile tile) {
		return PRIMARY_ROCK.getQuadrantValue(tile.getRow(), tile.getCol());
	}

	/**
	 * @param tile
	 *            a forest tile
	 * @return the main kind of tree on the tile
	 */
	// ESCA-JAVA0076:
	public String getPrimaryTree(final Tile tile) {
		if (TileType.BorealForest.equals(tile.getType())) {
			return BOREAL_PRIMARY_TREE.getQuadrantValue(tile.getRow(), // NOPMD
					tile.getCol());
		} else if (TileType.TemperateForest.equals(tile.getType())) {
			return TEMPERATE_PRIMARY_TREE.getQuadrantValue(tile.getRow(),
					tile.getCol());
		} else {
			throw new IllegalArgumentException(
					"Only forests have primary trees");
		}
	}
}
