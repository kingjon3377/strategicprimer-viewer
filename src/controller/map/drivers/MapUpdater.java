package controller.map.drivers;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.Point;
import model.map.Tile;
import model.map.TileType;
import util.Warning;
import view.util.DriverQuit;
import controller.map.cxml.CompactXMLWriter;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;

/**
 * A driver to update derived maps (such as players' maps) from a master map.
 *
 * @author Jonathan Lovelace
 *
 */
public final class MapUpdater implements ISPDriver {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(MapUpdater.class
			.getName());
	/**
	 * Update a derived map from the master.
	 * @param master the master map
	 * @param derived the derived map to update.
	 */
	public void update(final IMap master, final IMap derived) {
		if (!master.getDimensions().equals(derived.getDimensions())) {
			throw new IllegalArgumentException("Map sizes don't match");
		}
		for (final Point point : derived.getTiles()) {
			if (shouldUpdate(derived.getTile(point), master.getTile(point))) {
				derived.getTile(point).update(master.getTile(point));
			}
		}
	}

	/**
	 * @param masterTile a tile from the master map
	 * @param tile the equivalent tile from a derived map
	 *
	 * @return whether the derived map needs updating in this tile
	 */
	public boolean shouldUpdate(final Tile masterTile, final Tile tile) {
		return !TileType.NotVisible.equals(tile.getTerrain())
				&& !TileType.NotVisible.equals(masterTile.getTerrain())
				&& !tile.equals(masterTile);
	}

	/**
	 * Driver. We won't actually overwrite the dependent map with an update, but
	 * rather print the updated version to stdout.
	 *
	 * @param args Command-line arguments: master, then a map to update.
	 */
	public static void main(final String[] args) {
		try {
			new MapUpdater().startDriver(args);
		} catch (DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
		}
	}

	/**
	 * Build a string.
	 *
	 * @param strings the strings to concatenate.
	 *
	 * @return the result of the concatenation
	 */
	private static String buildString(final String... strings) {
		final StringBuilder build = new StringBuilder(16);
		for (final String str : strings) {
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
	 *
	 * @param filename the name of the map to load
	 *
	 * @return the map
	 */
	private static IMap loadMap(final String filename) {
		try {
			return new MapReaderAdapter().readMap(filename, new Warning(
					Warning.Action.Ignore));
		} catch (final FileNotFoundException e) {
			LOGGER.log(Level.SEVERE,
					buildString("File ", filename, " not found"), e);
			DriverQuit.quit(1);
			throw PASSED_EXIT; // SUPPRESS CHECKSTYLE This is unreachable
		} catch (final XMLStreamException e) {
			LOGGER.log(Level.SEVERE,
					buildString("XML stream error parsing ", filename), e);
			DriverQuit.quit(2);
			throw PASSED_EXIT; // SUPPRESS CHECKSTYLE This is unreachable
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE,
					buildString("I/O error processing ", filename), e);
			DriverQuit.quit(3);
			throw PASSED_EXIT; // SUPPRESS CHECKSTYLE This is unreachable
		} catch (final SPFormatException e) {
			LOGGER.log(Level.SEVERE,
					buildString(filename, " contained invalid data"), e);
			DriverQuit.quit(4);
			throw PASSED_EXIT; // SUPPRESS CHECKSTYLE This is unreachable
		}
	}

	/**
	 *
	 * @return a String representation of this object
	 */
	@Override
	public String toString() {
		return "MapUpdater";
	}
	/**
	 * Start the driver.
	 * @param args command-line arguments
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length < 2) {
			throw new IllegalArgumentException("Not enough arguments");
		}
		System.out.print("Base ");
		System.out.print(args[0]);
		System.out.print(": Reading ");
		final IMap master = loadMap(args[0]);
		System.out.println("Finished");
		for (final String arg : args) {
			if (arg.equals(args[0])) {
				continue;
			}
			System.out.print(arg);
			System.out.print(": ");
			System.out.print("Reading ");
			// ESCA-JAVA0177:
			final IMap derived = loadMap(arg);
			System.out.print("Updating ");
			update(master, derived);
			System.out.print("Writing ");
			// ESCA-JAVA0266:
			PrintWriter writer;
			try {
				writer = new PrintWriter(new FileWriter(arg)); // NOPMD
			} catch (final IOException e) {
				LOGGER.log(Level.SEVERE,
						"I/O error creating writer for updated map", e);
				continue;
			}
			try {
				new CompactXMLWriter().write(writer, derived); // NOPMD
			} catch (final IOException e) {
				LOGGER.log(Level.SEVERE, "I/O error writing updated map", e);
			} finally {
				writer.close();
			}
			System.out.println("Finished");
		}
	}
}
