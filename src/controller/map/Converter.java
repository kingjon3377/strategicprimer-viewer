package controller.map;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.TileType;
import model.viewer.SPMap;

/**
 * A program to create a new-style map from an old-style map
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class Converter {
	// /**
	// * The array of new-style tiles.
	// */
	// private final Tile[][] newTiles;
	/**
	 * The array of new-style tile types.
	 */
	private final TileType[][] newTypes;
	// /**
	// * A buffer to use while iterating.
	// */
	// private final Tile[][] buffer;
	/**
	 * A buffer to use while iterating
	 */
	private final TileType[][] tBuffer;
	/**
	 * An array of elevations.
	 */
	private final char[][] elevations;
	/**
	 * A buffer for elevations.
	 */
	private final char[][] eBuffer;
	/**
	 * The size of the array in rows.
	 */
	private final int rows;
	/**
	 * The size of the array in columns.
	 */
	private final int cols;
	/**
	 * How many little tiles per big tile in each dimension
	 */
	private static final int SUBTILES_PER_TILE = 66;

	/**
	 * Constructor.
	 * 
	 * @param map
	 *            the old-style map to convert
	 */
	public Converter(final SPMap map) {
		rows = map.rows() * SUBTILES_PER_TILE;
		cols = map.cols() * SUBTILES_PER_TILE;
		LOGGER.log(Level.INFO, "rows = " + rows);
		LOGGER.log(Level.INFO, "cols = " + cols);
		// newTiles = new Tile[rows][cols];
		// buffer = new Tile[rows][cols];
		newTypes = new TileType[rows][cols];
		tBuffer = new TileType[rows][cols];
		elevations = new char[rows][cols];
		eBuffer = new char[rows][cols];
		for (int row = 0; row < map.rows(); row++) {
			for (int col = 0; col < map.cols(); col++) {
				for (int i = 0; i < SUBTILES_PER_TILE; i++) {
					for (int j = 0; j < SUBTILES_PER_TILE; j++) {
						// newTiles[row * SUBTILES_PER_TILE + i][col
						// * SUBTILES_PER_TILE + j] = convertTile(map
						// .getTile(row, col));
						newTypes[row * SUBTILES_PER_TILE + i][col
								* SUBTILES_PER_TILE + j] = convertType(map
								.getTile(row, col).getType());
						elevations[row * SUBTILES_PER_TILE + i][col
								* SUBTILES_PER_TILE + j] = convertElevation(map
								.getTile(row, col).getType());
					}
				}
			}
		}
		LOGGER.log(Level.INFO, "Finished constructor");
	}

	// /**
	// * Convert an old-style tile into a new-style tile. Hoping this inlines
	// ...
	// *
	// * @param tile
	// * the old-style tile to convert
	// * @return an equivalent new-style tile
	// */
	// public Tile convertTile(final model.Tile tile) {
	// return new Tile(convertType(tile.getType()), convertElevation(tile
	// .getType()));
	// }

	/**
	 * Convert an old-style terrain type to a new-style tile type. Hoping this
	 * inlines ...
	 * 
	 * @param type
	 *            the old-style terrain type
	 * @return the equivalent new-style tile type.
	 */
	private static TileType convertType(final model.viewer.TileType type) {
		return type == model.viewer.TileType.Desert ? TileType.DESERT
				: type == model.viewer.TileType.Jungle ? TileType.SWAMP
						: type == model.viewer.TileType.NotVisible ? TileType.UNEXPLORED
								: type == model.viewer.TileType.Ocean ? TileType.WATER
										: type == model.viewer.TileType.Tundra ? TileType.ICE
												: TileType.PLAINS;
	}

	private static final char MEDIUM_LOW = 256;
	private static final char MEDIUM_HIGH = 768;
	private static final char HIGH = 1024;
	private static final char LOW = 0;
	private static final char MEDIUM = 512;

	/**
	 * Get an appropriate elevation for an old-style tile type.
	 * 
	 * @param type
	 *            the old-style tile type.
	 * @return an appropriate elevation.
	 */
	private static char convertElevation(final model.viewer.TileType type) {
		return type == model.viewer.TileType.BorealForest
				|| type == model.viewer.TileType.Tundra ? MEDIUM_HIGH
				: type == model.viewer.TileType.Jungle ? MEDIUM_LOW
						: type == model.viewer.TileType.Mountain ? HIGH
								: type == model.viewer.TileType.Ocean ? LOW
										: MEDIUM;
	}

	// private static final int MAX_ELEV_CHANGE = 3;

	/**
	 * Reduce the blockiness of the new map. Caller should iterate until this
	 * returns zero.
	 * 
	 * @return how many tiles were changed
	 */
	public int iterate() {
		int changes = 0; // NOPMD
		copyToBuffer();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (tooMuchHigher(eBuffer[i][j],
						eBuffer[mod(i - 1, rows)][mod(j - 1, cols)],
						eBuffer[mod(i - 1, rows)][j],
						eBuffer[mod(i - 1, rows)][(j + 1) % cols],
						eBuffer[i][mod(j - 1, cols)])
						|| tooMuchHigher(eBuffer[i][j], eBuffer[i][(j + 1)
								% cols],
								eBuffer[(i + 1) % rows][mod(j - 1, cols)],
								eBuffer[(i + 1) % rows][j], eBuffer[(i + 1)
										% rows][(j + 1) % cols])) {
					elevations[i][j]--;
					changes++; // NOPMD
				}
				if (tooMuchLower(eBuffer[i][j],
						eBuffer[mod(i - 1, rows)][mod(j - 1, cols)],
						eBuffer[mod(i - 1, rows)][j],
						eBuffer[mod(i - 1, rows)][(j + 1) % cols],
						eBuffer[i][mod(j - 1, cols)])
						|| tooMuchLower(eBuffer[i][j], eBuffer[i][(j + 1)
								% cols],
								eBuffer[(i + 1) % rows][mod(j - 1, cols)],
								eBuffer[(i + 1) % rows][j], eBuffer[(i + 1)
										% rows][(j + 1) % cols])) {
					elevations[i][j]++;
					changes++; // NOPMD
				}
				if (changes % 2063 < 5
						&& !shouldChangeType(tBuffer[i][j],
								tBuffer[mod(i - 1, rows)][mod(j - 1, cols)],
								tBuffer[mod(i - 1, rows)][j],
								tBuffer[mod(i - 1, rows)][(j + 1) % cols],
								tBuffer[i][mod(j - 1, cols)],
								tBuffer[i][(j + 1) % cols],
								tBuffer[(i + 1) % rows][mod(j - 1, cols)],
								tBuffer[(i + 1) % rows][j],
								tBuffer[(i + 1) % rows][(j + 1) % cols])
								.equals(tBuffer[i][j])) {
					newTypes[i][j] = shouldChangeType(tBuffer[i][j],
							tBuffer[mod(i - 1, rows)][mod(j - 1, cols)],
							tBuffer[mod(i - 1, rows)][j],
							tBuffer[mod(i - 1, rows)][(j + 1) % cols],
							tBuffer[i][mod(j - 1, cols)], tBuffer[i][(j + 1)
									% cols],
							tBuffer[(i + 1) % rows][mod(j - 1, cols)],
							tBuffer[(i + 1) % rows][j],
							tBuffer[(i + 1) % rows][(j + 1) % cols]);
					changes++; // NOPMD
				}
			}
		}
		return changes;
	}

	/**
	 * @param integ
	 *            an integer
	 * @param mod
	 *            another integer
	 * @return i % mod, taking negatives properly into account.
	 */
	private static int mod(final int integ, final int mod) {
		return integ < 0 ? mod + integ : integ <= mod ? integ : mod(
				integ - mod, mod);
	}

	/**
	 * 
	 */
	private void copyToBuffer() {
		for (int i = 0; i < rows; i++) {
			System.arraycopy(newTypes[i], 0, tBuffer[i], 0, cols);
			System.arraycopy(elevations[i], 0, eBuffer[i], 0, cols);
			// for (int j = 0; j < cols; j++) {
			// tBuffer[i][j] = newTypes[i][j];
			// eBuffer[i][j] = elevations[i][j];
			// }
		}
	}

	private static final int[] counts = new int[TileType.values().length];

	private static TileType shouldChangeType(final TileType tile,
			final TileType... moreTiles) {
		Arrays.fill(counts, 0);
		counts[tile.ordinal()] = 2;
		for (final TileType next : moreTiles) {
			counts[next.ordinal()]++;
		}
		TileType common = TileType.UNEXPLORED; // NOPMD
		int max = -1; // NOPMD
		for (final TileType type : TileType.values()) {
			if (counts[type.ordinal()] > max) {
				common = type; // NOPMD
				max = counts[type.ordinal()]; // NOPMD
			}
		}
		return common;
	}

	private static boolean tooMuchLower(final char tile, final char tile2,
			final char tile3, final char tile4, final char tile5) {
		return tile - tile2 < -2 || tile - tile3 < -2 || tile - tile4 < -2
				|| tile - tile5 < -3;
	}

	private static boolean tooMuchHigher(final char tile, final char tile2,
			final char tile3, final char tile4, final char tile5) {
		return tile - tile2 > 2 || tile - tile3 > 2 || tile - tile4 > 2
				|| tile - tile5 > 3;
	}

	/**
	 * Write the final map to file
	 * 
	 * @param filename
	 *            name of the file to write to
	 * @throws IOException
	 *             on I/O error
	 */
	public void writeToFile(final String filename) throws IOException {
		final BufferedWriter out = new BufferedWriter(new FileWriter(filename));
		out.append(Integer.toString(rows));
		out.append(' ');
		out.append(Integer.toString(cols));
		out.append('\n');
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				out.append(Integer.toString(newTypes[i][j].ordinal()));
				out.append(' ');
			}
			out.append('\n');
		}
		out.append('\n');
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				// ESCA-JAVA0262:
				out.append(Integer.toString(elevations[i][j]));
				out.append(' ');
			}
			out.append('\n');
		}
		out.close();
	}

	private static final Logger LOGGER = Logger.getLogger(Converter.class
			.getName());

	/**
	 * Entry point for this program
	 * 
	 * @param args
	 *            the XML map to read
	 */
	public static void main(final String[] args) {
		try {
			final Converter conv = new Converter(
					new MapReader().readMap(args[0]));
			int changes = 1;
			int iterations = 0;
			while (changes != 0 && iterations < 100000) {
				changes = conv.iterate();
				iterations++;
				LOGGER.info("iteration " + iterations + ", changes = "
						+ changes);
			}
			conv.writeToFile("/home/kingjon/new_map.spmap");
		} catch (final XMLStreamException e) {
			LOGGER.log(Level.SEVERE, "XML parsing error", e);
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error", e);
		}
	}
}
