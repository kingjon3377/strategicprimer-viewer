package controller.map.drivers;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.CLIHelper;
import controller.map.misc.ICLIHelper;
import controller.map.misc.MapReaderAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import model.map.IMutableMapNG;
import model.map.Point;
import model.map.fixtures.mobile.Unit;
import util.NullCleaner;
import util.Warning;

/**
 * A hackish class to help fix TODOs (missing content) in the map.
 *
 * TODO: Add tests of this functionality.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2014-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class TODOFixerDriver {
	/**
	 * The map we operate on.
	 */
	private final IMutableMapNG map;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = NullCleaner
			                                     .assertNotNull(Logger.getLogger(
					                                     TODOFixerDriver.class
							                                     .getName()));

	/**
	 * @param operand the map we operate on
	 */
	public TODOFixerDriver(final IMutableMapNG operand) {
		map = operand;
	}

	/**
	 * Search for and fix units with kinds missing.
	 * @param cli the interface to the user
	 */
	public void fixAllUnits(final ICLIHelper cli) {
		// TODO: How to make this use Stream API instead of loop?
		for (final Point point : map.locations()) {
			final SimpleTerrain terrain = getTerrain(point);
			map.streamOtherFixtures(point).filter(Unit.class::isInstance)
					.map(Unit.class::cast).filter(unit -> "TODO".equals(unit.getKind()))
					.forEach(unit -> fixUnit(NullCleaner.assertNotNull(unit), terrain, cli));
		}
	}

	/**
	 * How many units we've fixed.
	 */
	private int count = -1;
	/**
	 * Possible kinds of terrain.
	 */
	private enum SimpleTerrain {
		/**
		 * Plains, desert, and mountains.
		 */
		Unforested,
		/**
		 * Temperate, forest, boreal forest, and steppe.
		 */
		Forested,
		/**
		 * Ocean.
		 */
		Ocean,
		/**
		 * Anything else.
		 */
		Other
	}
	/**
	 * Fix a stubbed-out kind for a unit.
	 *
	 * @param unit    the unit to fix
	 * @param terrain the terrain the unit is in
	 * @param cli the helper to get input from the user
	 */
	private void fixUnit(final Unit unit, final SimpleTerrain terrain,
	                     final ICLIHelper cli) {
		final Random random = new Random(unit.getID());
		count++;
		final Collection<String> jobList;
		final String desc;
		switch (terrain) {
		case Unforested:
			jobList = plainsList;
			desc = "plains, desert, or mountains";
			break;
		case Forested:
			jobList = forestList;
			desc = "forest or jungle";
			break;
		case Ocean:
			jobList = oceanList;
			desc = "ocean";
			break;
		case Other:
		default:
			jobList = new ArrayList<>();
			desc = "other";
		}
		for (final String job : jobList) {
			if (random.nextBoolean()) {
				cli.printf(
						"Setting unit with ID #%d (%d / 5328) to kind %s%n",
						Integer.valueOf(unit.getID()), Integer.valueOf(count), job);
				unit.setKind(job);
				return;
			}
		}
		try {
			final String kind =
					cli.inputString("What's the next possible kind for "
							                   + desc + "? ");
			unit.setKind(kind);
			jobList.add(kind);
		} catch (final IOException e) {
			LOGGER.log(Level.FINE, "I/O error interacting with user", e);
		}
	}

	/**
	 * A list of unit kinds (jobs) for plains etc.
	 */
	private final Collection<String> plainsList = new ArrayList<>();
	/**
	 * A list of unit kinds (jobs) for forest and jungle.
	 */
	private final Collection<String> forestList = new ArrayList<>();
	/**
	 * A list of unit kinds (jobs) for ocean.
	 */
	private final Collection<String> oceanList = new ArrayList<>();

	/**
	 * @param location a location in the map
	 * @return the kind of terrain, with very coarse granularity, here
	 */
	@SuppressWarnings("deprecation")
	private SimpleTerrain getTerrain(final Point location) {
		switch (map.getBaseTerrain(location)) {
		case Jungle:
		case BorealForest:
		case Steppe:
		case TemperateForest:
			return SimpleTerrain.Forested;
		case Desert:
		case Mountain:
		case Tundra:
		case NotVisible: // Should never happen, but ...
			return SimpleTerrain.Unforested;
		case Ocean:
			return SimpleTerrain.Ocean;
		case Plains:
			return getPlainsTerrain(location);
		default:
			return SimpleTerrain.Unforested; // Should never get here, but ...
		}
	}
	/**
	 * @param location a location
	 * @return the appropriate terrain for it if it is plains
	 */
	private SimpleTerrain getPlainsTerrain(final Point location) {
		if (map.isMountainous(location) || map.getForest(location) == null) {
			return SimpleTerrain.Unforested;
		} else {
			return SimpleTerrain.Forested;
		}
	}
	/**
	 * Run the driver.
	 *
	 * @param args maps to run on
	 */
	@SuppressWarnings("NestedTryStatement")
	public static void main(final String... args) {
		try (final ICLIHelper cli = new CLIHelper()) {
			final MapReaderAdapter reader = new MapReaderAdapter();
			for (final String arg : args) {
				if (arg == null) {
					continue;
				}
				final IMutableMapNG map;
				//noinspection ObjectAllocationInLoop
				final File file = new File(arg);
				try {
					map = reader.readMap(file, Warning.DEFAULT);
				} catch (final IOException | XMLStreamException | SPFormatException e) {
					LOGGER.log(Level.SEVERE, "Error reading map " + arg, e);
					continue;
				}
				//noinspection ObjectAllocationInLoop
				final TODOFixerDriver driver = new TODOFixerDriver(map);
				driver.fixAllUnits(cli);
				try {
					reader.write(file, map);
				} catch (final IOException e) {
					LOGGER.log(Level.SEVERE, "I/O error writing map to " + arg, e);
				}
			}
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error closing CLIHelper", except);
		}
	}

	/**
	 * @return a string representation of this class
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TODOFixerDriver";
	}
}
