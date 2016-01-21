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
import model.map.TileFixture;
import model.map.fixtures.mobile.Unit;
import util.NullCleaner;
import util.Warning;
import view.util.SystemOut;

/**
 * A hackish class to help fix TODOs (missing content) in the map.
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
	 * A helper to get strings from the user.
	 */
	private final ICLIHelper helper = new CLIHelper();
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
	 */
	public void fixAllUnits() {
		for (final Point point : map.locations()) {
			final SimpleTerrain terrain = getTerrain(point);
			for (final TileFixture fix : map.getOtherFixtures(point)) {
				if ((fix instanceof Unit) && "TODO".equals(((Unit) fix).getKind())) {
					fixUnit((Unit) fix, terrain);
				}
			}
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
		Unforested, Forested, Ocean, Other
	}
	/**
	 * Fix a stubbed-out kind for a unit.
	 *
	 * @param unit    the unit to fix
	 * @param terrain the terrain the unit is in
	 */
	private void fixUnit(final Unit unit, final SimpleTerrain terrain) {
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
				SystemOut.SYS_OUT.printf(
						"Setting unit with ID #%d (%d / 5328) to kind %s%n",
						Integer.valueOf(unit.getID()), Integer.valueOf(count), job);
				unit.setKind(job);
				return;
			}
		}
		try {
			final String kind =
					helper.inputString("What's the next possible kind for "
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
			if (map.isMountainous(location)) {
				return SimpleTerrain.Unforested;
			} else if (map.getForest(location) != null) {
				return SimpleTerrain.Forested;
			} else {
				return SimpleTerrain.Unforested;
			}
		default:
			return SimpleTerrain.Unforested; // Should never get here, but ...
		}
	}

	/**
	 * Run the driver.
	 *
	 * @param args maps to run on
	 */
	public static void main(final String... args) {
		final MapReaderAdapter reader = new MapReaderAdapter();
		for (final String arg : args) {
			if (arg == null) {
				continue;
			}
			final IMutableMapNG map;
			final File file = new File(arg);
			try {
				map = reader.readMap(file, Warning.DEFAULT);
			} catch (final IOException | XMLStreamException | SPFormatException e) {
				LOGGER.log(Level.SEVERE, "Error reading map " + arg, e);
				continue;
			}
			final TODOFixerDriver driver = new TODOFixerDriver(map);
			driver.fixAllUnits();
			try {
				reader.write(file, map);
			} catch (final IOException e) {
				LOGGER.log(Level.SEVERE, "I/O error writing map to " + arg, e);
			}
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
