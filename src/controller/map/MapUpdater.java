package controller.map;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.SPMap;
import model.map.Tile;
import model.map.TileType;
import view.util.DriverQuit;

/**
 * A driver to update derived maps (such as players' maps) from a master map.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class MapUpdater {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(MapUpdater.class.getName());
	/**
	 * The master map.
	 */
	private final SPMap master;

	/**
	 * 
	 * @param masterMap
	 *            the master map
	 */
	public MapUpdater(final SPMap masterMap) {
		master = masterMap;
	}

	/**
	 * Update a derived map from the master.
	 * 
	 * @param derived
	 *            the derived map to update.
	 */
	public void update(final SPMap derived) {
		if (master.rows() != derived.rows()
				|| master.cols() != derived.cols()) {
			throw new IllegalArgumentException("Map sizes don't match");
		}
		final int rows = master.rows();
		final int cols = master.cols();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (shouldUpdate(derived.getTile(i, j), master.getTile(i, j))) {
					derived.addTile(master.getTile(i, j));
				}
			}
		}
	}

	/**
	 * @param masterTile
	 *            a tile from the master map
	 * @param tile
	 *            the equivalent tile from a derived map
	 * @return whether the derived map needs updating in this tile
	 */
	public boolean shouldUpdate(final Tile masterTile, final Tile tile) {
		return !TileType.NotVisible.equals(tile.getType())
				&& !TileType.NotVisible.equals(masterTile.getType())
				&& !tile.equals(masterTile);
	}

	/**
	 * Driver. We won't actually overwrite the dependent map with an update, but
	 * rather print the updated version to stdout.
	 * 
	 * @param args
	 *            Command-line arguments: master, then a map to update.
	 */
	public static void main(final String[] args) {
		if (args.length < 2) {
			throw new IllegalArgumentException("Not enough arguments");
		}
		// ESCA-JAVA0177:
		final MapUpdater updater = new MapUpdater(loadMap(args[0]));
		// ESCA-JAVA0177:
		final SPMap derived = loadMap(args[1]);
		updater.update(derived);
		// ESCA-JAVA0266:
		final PrintWriter writer = new PrintWriter(System.out);
		try {
		new XMLWriter(writer).write(derived);
		} finally {
		writer.close();
		}
	}
	
	/**
	 * Build a string.
	 * @param strings the strings to concatenate.
	 * @return the result of the concatenation
	 */
	private static String buildString(final String... strings) {
		final StringBuilder build = new StringBuilder(16);
		for (String str : strings) {
			build.append(str);
		}
		return build.toString();
	}
	/**
	 * An exception to throw if execution gets past System.exit().
	 */
	private static final IllegalStateException PASSED_EXIT = new IllegalStateException(
			"Execution passed System.exit()");
	/**
	 * Load a map; if this fails, log a suitable error message and quit.
	 * @param filename the name of the map to load
	 * @return the map
	 */
	private static SPMap loadMap(final String filename) {
		try {
			return new MapReaderAdapter().readMap(filename);
		} catch (final FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, buildString("File ", filename, " not found"), e);
			DriverQuit.quit(1);
			throw PASSED_EXIT;
		} catch (final XMLStreamException e) {
			LOGGER.log(Level.SEVERE,
					buildString("XML stream error parsing ", filename), e);
			DriverQuit.quit(2);
			throw PASSED_EXIT;
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, buildString("I/O error processing ", filename), e);
			DriverQuit.quit(3);
			throw PASSED_EXIT;
		} catch (SPFormatException e) {
			LOGGER.log(Level.SEVERE, buildString(filename, " contained invalid data"), e);
			DriverQuit.quit(4);
			throw PASSED_EXIT;
		}
	}
	/**
	 * @return a String representation of this object
	 */
	@Override
	public String toString() {
		return "MapUpdater";
	}
}
