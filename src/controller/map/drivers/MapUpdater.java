package controller.map.drivers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.Point;
import model.map.Tile;
import model.map.TileType;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.DriverUsage;
import controller.map.misc.MapReaderAdapter;
import controller.map.misc.DriverUsage.ParamCount;

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
	 * To avoid creating instance after instance ...
	 */
	private final MapReaderAdapter adapter = new MapReaderAdapter();
	/**
	 * Update a derived map from the master.
	 * @param master the master map
	 * @param derived the derived map to update.
	 * @return the derived method.
	 */
	public IMap update(final IMap master, final IMap derived) {
		System.out.print("Updating ");
		if (!master.getDimensions().equals(derived.getDimensions())) {
			throw new IllegalArgumentException("Map sizes don't match");
		}
		for (final Point point : derived.getTiles()) {
			if (shouldUpdate(derived.getTile(point), master.getTile(point))) {
				derived.getTile(point).update(master.getTile(point));
			}
		}
		return derived;
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
	 * Load a map; if this fails, throw a DriverFailedException.
	 *
	 * @param filename the name of the map to load
	 *
	 * @return the map
	 * @throws DriverFailedException on any of the errors that may crop up
	 */
	private IMap loadMap(final String filename) throws DriverFailedException {
		System.out.print(filename);
		System.out.print(": Reading ");
		try {
			return adapter.readMap(filename, new Warning(
					Warning.Action.Ignore));
		} catch (final FileNotFoundException e) {
			throw new DriverFailedException(buildString("File ", filename,
					" not found"), e);
		} catch (final XMLStreamException e) {
			throw new DriverFailedException(buildString(
					"XML stream error parsing ", filename), e);
		} catch (final IOException e) {
			throw new DriverFailedException(buildString(
					"I/O error processing ", filename), e);
		} catch (final SPFormatException e) {
			throw new DriverFailedException(buildString(filename,
					" contained invalid data"), e);
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
			throw new DriverFailedException("Need at least two arguments",
					new IllegalArgumentException("Not enough arguments"));
		}
		System.out.print("Base ");
		final IMap master = loadMap(args[0]);
		System.out.println("Finished");
		for (final String arg : args) {
			if (arg.equals(args[0])) {
				continue;
			}
			System.out.print("Reading ");
			// ESCA-JAVA0177:
			final IMap derived = update(master, loadMap(arg));
			try {
				System.out.print("Writing ");
				adapter.write(arg, derived);
				System.out.println("Finished");
			} catch (IOException except) {
				LOGGER.log(Level.SEVERE, "I/O error writing map " + arg, except);
			}
		}
	}

	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-u",
			"--update", ParamCount.Many, "Update player maps",
			"Update subordinate maps from 'master' data in main map",
			MapUpdater.class);

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE_OBJ;
	}
}
