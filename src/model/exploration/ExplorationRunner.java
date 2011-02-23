package model.exploration;

import model.viewer.Tile;
import model.viewer.TileType;

/**
 * A class to create exploration results. The initial implementation is a bit
 * hackish, and should be generalized and improved.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class ExplorationRunner {
	/**
	 * @param tile
	 *            a tile
	 * @return what the owner of a fortress on the tile knows
	 */
	public String defaultResults(final Tile tile) {
		final StringBuilder sb = new StringBuilder("The primary rock type here is "); // NOPMD
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
	 * FIXME: This data should be read from file, not embedded in the program.
	 * @param tile a tile
	 * @return the main kind of rock on the tile
	 */
	// ESCA-JAVA0076:
	public String getPrimaryRock(final Tile tile) {
		if (tile.getRow() < 23) {
			if (tile.getCol() < 29) {
				return "gabbro"; // NOPMD
			} else if (tile.getCol() < 58) {
				return "basalt"; // NOPMD
			} else {
				return "sandstone"; // NOPMD
			}
		} else if (tile.getRow() < 46) {
			if (tile.getCol() < 29) {
				return "limestone"; // NOPMD
			} else if (tile.getCol() < 58) {
				return "granite"; // NOPMD
			} else {
				return "shale"; // NOPMD
			}
		} else {
			if (tile.getCol() < 29) {
				return "gneiss"; // NOPMD
			} else if (tile.getCol() < 58) {
				return "chalk"; // NOPMD
			} else {
				return "slate";
			}
		}
	}
	/**
	 * FIXME: This data should be read from file, not embedded in the program.
	 * @param tile a forest tile
	 * @return the main kind of tree on the tile
	 */
	// ESCA-JAVA0076:
	public String getPrimaryTree(final Tile tile) {
		if (TileType.BorealForest.equals(tile.getType())) {
			if (tile.getRow() < 35) {
				if (tile.getCol() < 45) {
					return "pine"; // NOPMD
				} else {
					return "fir"; // NOPMD
				}
			} else {
				if (tile.getCol() < 45) {
					return "larch"; // NOPMD
				} else {
					return "spruce"; // NOPMD
				}
			}
		} else if (TileType.TemperateForest.equals(tile.getType())) {
			if (tile.getRow() < 35) {
				if (tile.getCol() < 45) {
					return "oak"; // NOPMD
				} else {
					return "maple"; // NOPMD
				}
			} else {
				if (tile.getCol() < 45) {
					return "beech"; // NOPMD
				} else {
					return "elm";
				}
			}
		} else {
			throw new IllegalArgumentException("Only forests have primary trees");
		}
	}
}
