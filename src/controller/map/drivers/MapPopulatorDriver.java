package controller.map.drivers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.Point;
import model.map.TileType;
import model.map.fixtures.mobile.Animal;
import util.SingletonRandom;
import util.Warning;
import util.Warning.Action;
import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import controller.map.misc.IDFactoryFiller;
import controller.map.misc.MapReaderAdapter;

/**
 * A driver to add some kind of fixture to suitable tiles throughout the map.
 * Customize isSuitable(), chance(), and create() before each use.
 *
 * @author Jonathan Lovelace
 *
 */
public class MapPopulatorDriver implements ISPDriver {
	private static int suitableCount = 0;
	private static int changedCount = 0;
	/**
	 * Whether the given location is suitable for the kind of fixture we're creating.
	 * @param map the map
	 * @param location the location being considered
	 * @return whether the location is suitable for whatever fixture is being added
	 */
	private static boolean isSuitable(final IMapNG map,
			final Point location) {
		TileType terrain = map.getBaseTerrain(location);
		// Hares won't appear in mountains, forests, or ocean.
		if (map.isMountainous(location)) {
			return false;
		} else if (map.getForest(location) != null) {
			return false;
		} else if (TileType.Ocean.equals(terrain)) {
			return false;
		} else {
			suitableCount++;
			return true;
		}
	}
	/**
	 * @return The probability of adding to any given tile.
	 */
	private static double chance() {
		return .05;
	}

	/**
	 * Add a fixture of the kind with which we're populating the map to the
	 * specified location
	 *
	 * @param location
	 *            the location in question
	 * @param map
	 *            the map
	 * @param idf
	 *            an ID factory to generate the necessary ID #.
	 */
	private static void create(final Point location, final IMutableMapNG map,
			final IDFactory idf) {
		changedCount++;
		map.addFixture(location,
				new Animal("hare", false, false, "wild", idf.createID()));
	}
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-l",
			"--populate", ParamCount.One, "Add missing fixtures to a map",
			"Add specified kinds of fixtures to suitable points throughout a map",
			MapPopulatorDriver.class);
	/**
	 * @return what to call the driver in a CLI list.
	 */
	@Override
	public String getName() {
		return USAGE_OBJ.getShortDescription();
	}

	/**
	 * @param nomen ignored
	 */
	@Override
	public void setName(final String nomen) {
		throw new IllegalStateException("Can't rename a driver");
	}
	/**
	 * Run the driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException if something goes wrong
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length != 1) {
			throw new DriverFailedException("Need one argument",
					new IllegalArgumentException("Need one argument"));
		}
		final File file = new File(args[0]);
		final IMutableMapNG map;
		try {
			map = new MapReaderAdapter().readMap(file, new Warning(Action.Warn));
		} catch (final XMLStreamException e) {
			throw new DriverFailedException("XML parsing error in "
					+ file.getPath(), e);
		} catch (final FileNotFoundException e) {
			throw new DriverFailedException("File " + file.getPath()
					+ " not found", e);
		} catch (final IOException e) {
			throw new DriverFailedException("I/O error reading "
					+ file.getPath(), e);
		} catch (final SPFormatException e) {
			throw new DriverFailedException("Map " + file.getPath()
					+ " contains invalid data", e);
		}
		populate(map);
		try {
			new MapReaderAdapter().write(file, map);
		} catch (IOException e) {
			throw new DriverFailedException("I/O error writing updated map", e);
		}
		System.out.print(changedCount);
		System.out.print(" out of ");
		System.out.print(suitableCount);
		System.out.println(" suitable locations were changed");
	}
	/**
	 * Add populations to the map.
	 * @param map the map
	 */
	private static void populate(final IMutableMapNG map) {
		IDFactory idf = IDFactoryFiller.createFactory(map);
		for (Point location : map.locations()) {
			if (location != null && isSuitable(map, location)
					&& SingletonRandom.RANDOM.nextDouble() < chance()) {
				create(location, map, idf);
			}
		}
	}
	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE_OBJ;
	}
}
