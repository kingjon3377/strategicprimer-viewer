package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.Point;
import model.map.TileType;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.Village;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import util.EqualsAny;
import util.Pair;
import util.TypesafeLogger;

/**
 * A hackish class to help fix TODOs (missing content) in the map.
 *
 * TODO: Add tests of this functionality.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2014-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class TODOFixerDriver implements SimpleCLIDriver {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(TODOFixerDriver.class);
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
	 * A map from village IDs to races.
	 */
	private final Map<Integer, String> raceMap = new HashMap<>();
	/**
	 * A list of aquatic races.
	 */
	private final List<String> raceList = new ArrayList<>();
	/**
	 * How many units we've fixed.
	 */
	private int count = -1;

	/**
	 * Search for and fix units with kinds missing.
	 *
	 * @param map the map we're operating on
	 * @param cli the interface to the user
	 */
	public void fixAllUnits(final IMapNG map, final ICLIHelper cli) {
		for (final Point point : map.locations()) {
			final SimpleTerrain terrain = getTerrain(map, point);
			map.streamOtherFixtures(point).filter(Unit.class::isInstance)
					.map(Unit.class::cast).filter(unit -> "TODO".equals(unit.getKind()))
					.forEach(unit -> fixUnit(unit, terrain, cli));
		}
	}
	/**
	 * Search for and fix aquatic villages with non-aquatic races.
	 * @param map the map we're operating on
	 * @param cli the interface to the user
	 * @throws IOException on I/O error interacting with user
	 */
	private void fixAllVillages(final IMapNG map, final ICLIHelper cli)
			throws IOException {
		final Collection<Village> all = map.locationStream()
												.filter(point -> TileType.Ocean ==
																		 map
																				 .getBaseTerrain(
																				 point))
												.flatMap(point -> map
																		  .streamOtherFixtures(
														point))
												.filter(Village.class::isInstance)
												.map(Village.class::cast)
												.filter(village -> EqualsAny.equalsAny(
														village.getRace(), "Danan",
														"dwarf", "elf", "half-elf",
														"gnome", "human"))
												.collect(Collectors.toList());
		if (!all.isEmpty()) {
			if (raceList.isEmpty()) {
				final ICLIHelper.ThrowingSupplier<String> supp =
						cliHelper -> cliHelper.inputString("Next aquatic race: ");
				for (String race = supp.get(cli).trim(); !race.isEmpty(); race = supp.get(cli).trim()) {
					raceList.add(race);
				}
			}
			for (final Village village : all) {
				fixVillage(village, cli);
			}
		}
	}
	/**
	 * Fix an aquatic village with a non-aquatic race.
	 * @param village the village to fix
	 * @param cli the interface to the user.
	 * @throws IOException on I/O error interacting with user
	 */
	private void fixVillage(final Village village, final ICLIHelper cli) {
		final Integer id = Integer.valueOf(village.getID());
		if (raceMap.containsKey(id)) {
			village.setRace(raceMap.get(id));
		} else {
			final Random random = new Random(village.getID());
			final int index = random.nextInt(raceList.size());
			final String race = raceList.get(index);
			village.setRace(race);
			raceMap.put(id, race);
		}
	}
	/**
	 * Fix a stubbed-out kind for a unit.
	 *
	 * @param unit    the unit to fix. Can't be an IUnit because it has to be a HasMutableKind.
	 * @param terrain the terrain the unit is in
	 * @param cli     the helper to get input from the user
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
				//noinspection HardcodedFileSeparator
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
			//noinspection HardcodedFileSeparator
			LOGGER.log(Level.FINE, "I/O error interacting with user", e);
		}
	}

	/**
	 * Get the simplified-terrain-model instance covering the map's terrain at the given
	 * location.
	 * @param map      the map we're dealing with
	 * @param location a location in the map
	 * @return the kind of terrain, with very coarse granularity, here
	 */
	@SuppressWarnings("deprecation")
	private static SimpleTerrain getTerrain(final IMapNG map, final Point location) {
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
			return getPlainsTerrain(map, location);
		default:
			return SimpleTerrain.Unforested; // Should never get here, but ...
		}
	}

	/**
	 * Assuming the map's terrain at the given location is plains, get the rough-model
	 * terrain there.
	 * @param map      the map we're dealing with
	 * @param location a location
	 * @return the appropriate terrain for it if it is plains
	 */
	private static SimpleTerrain getPlainsTerrain(final IMapNG map,
												  final Point location) {
		if (map.isMountainous(location) || (map.getForest(location) == null)) {
			return SimpleTerrain.Unforested;
		} else {
			return SimpleTerrain.Forested;
		}
	}

	/**
	 * Run the driver.
	 *
	 * @param cli the interface for user I/O
	 * @param options options passed to the driver
	 * @param model   the driver model to operate on
	 * @throws DriverFailedException on I/O error interacting with the user
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model) throws DriverFailedException {
		if (model instanceof IMultiMapModel) {
			for (final Pair<IMutableMapNG, Optional<Path>> pair : ((IMultiMapModel)
																		   model)
																		  .getAllMaps
																				   ()) {
				fixAllUnits(pair.first(), cli);
				try {
					fixAllVillages(pair.first(), cli);
				} catch (IOException except) {
					//noinspection HardcodedFileSeparator
					throw new DriverFailedException("I/O error", except);
				}
			}
		} else {
			fixAllUnits(model.getMap(), cli);
			try {
				fixAllVillages(model.getMap(), cli);
			} catch (IOException except) {
				//noinspection HardcodedFileSeparator
				throw new DriverFailedException("I/O error", except);
			}
		}
	}

	/**
	 * A trivial toString().
	 * @return a string representation of this class
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TODOFixerDriver";
	}

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
}
