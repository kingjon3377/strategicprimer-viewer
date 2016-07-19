package controller.map.drivers;

import controller.map.misc.CLIHelper;
import controller.map.misc.ICLIHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.DoubleToIntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import model.exploration.HerdModel;
import model.exploration.HuntingModel;
import model.map.IMapNG;
import model.map.MapDimensions;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;
import model.map.fixtures.Ground;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.towns.Fortress;
import model.misc.IDriverModel;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;
import util.TypesafeLogger;

/**
 * A driver for running exploration results, etc., using the new model.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
public final class QueryCLI implements SimpleDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-q", "--query", ParamCount.One,
								"Answer questions about a map.",
								"Look at tiles on a map. Or run hunting, gathering, " +
										"or fishing.",
								QueryCLI.class);

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(QueryCLI.class);

	/**
	 * How many hours we assume a working day is for a hunter or such.
	 */
	private static final int HUNTER_HOURS = 10;
	/**
	 * How many encounters per hour for a hunter or such.
	 */
	private static final int HOURLY_ENCOUNTERS = 4;

	/**
	 * @param model   the driver model containing the map to explore
	 * @param cli the interface to the user
	 */
	private static void repl(final IDriverModel model, final ICLIHelper cli) {
		final HuntingModel huntModel = new HuntingModel(model.getMap());
		try {
			String input = cli.inputString("Command: ");
			while (!input.isEmpty() && (input.charAt(0) != 'q')) {
				handleCommand(model, huntModel, cli, input.charAt(0));
				input = cli.inputString("Command: ");
			}
		} catch (final IOException | DriverFailedException except) {
			LOGGER.log(Level.SEVERE, "I/O exception", except);
		}
	}

	/**
	 * @param model   the driver model
	 * @param huntModel  the hunting model
	 * @param cli the interface to the user
	 * @param input   the command
	 * @throws IOException           on I/O error
	 * @throws DriverFailedException on I/O error in trap-model driver
	 */
	public static void handleCommand(final IDriverModel model,
			final HuntingModel huntModel, final ICLIHelper cli, final char input)
					throws IOException, DriverFailedException {
		switch (input) {
		case '?':
			usage(cli);
			break;
		case 'f':
			fortressInfo(model.getMap(), selectPoint(cli), cli);
			break;
		case 'h':
			hunt(huntModel, selectPoint(cli), true, cli, HUNTER_HOURS * HOURLY_ENCOUNTERS);
			break;
		case 'i':
			hunt(huntModel, selectPoint(cli), false, cli, HUNTER_HOURS * HOURLY_ENCOUNTERS);
			break;
		case 'g':
			gather(huntModel, selectPoint(cli), cli, HUNTER_HOURS * HOURLY_ENCOUNTERS);
			break;
		case 'e':
			herd(cli, huntModel);
			break;
		case 't':
			new TrapModelDriver().startDriver(model);
			break;
		case 'd':
			distance(model.getMapDimensions(), cli);
			break;
		case 'c':
			count(model.getMap(), CLIHelper.toList(model.getMap().players()), cli);
			break;
		default:
			cli.println("Unknown command.");
			break;
		}
	}

	/**
	 * Count the workers belonging to a player.
	 *
	 * @param map     the map
	 * @param players the list of players in the map
	 * @param cli the interface to the user
	 * @throws IOException on I/O error interacting with user
	 */
	private static void count(final IMapNG map, final List<Player> players,
							final ICLIHelper cli) throws IOException {
		final int playerNum = cli.chooseFromList(players,
				"Players in the map:", "Map contains no players",
				"Owner of workers to count: ", true);
		if ((playerNum < 0) || (playerNum >= players.size())) {
			return;
		}
		final Player player = players.get(playerNum);
		int count = 0;
		for (final Point loc : map.locations()) {
			for (final TileFixture fix : map.getOtherFixtures(loc)) {
				if ((fix instanceof IUnit) && player.equals(((IUnit) fix).getOwner())) {
					count += StreamSupport.stream(((IUnit) fix).spliterator(), false)
									.filter(IWorker.class::isInstance).count();
				} else if (fix instanceof Fortress) {
					count += StreamSupport.stream(((Fortress) fix).spliterator(), false)
									.filter(IUnit.class::isInstance)
									.map(IUnit.class::cast)
									.filter(unit -> player.equals(unit.getOwner()))
									.flatMap(unit -> StreamSupport
															.stream(unit.spliterator(),
																	false))
									.filter(IWorker.class::isInstance).count();
				}
			}
		}
		cli.printf("%s has %d workers.%n", player.getName(), Integer.valueOf(count));
	}

	/**
	 * Report the distance between two points.
	 *
	 * TODO: use some sort of pathfinding
	 *
	 * @param dims    the dimensions of the map
	 * @param cli the interface to the user
	 * @throws IOException on I/O error dealing with user input
	 */
	private static void distance(final MapDimensions dims, final ICLIHelper cli)
			throws IOException {
		cli.print("Starting point:\t");
		final Point start = selectPoint(cli);
		cli.print("Destination:\t");
		final Point end = selectPoint(cli);
		cli.printf("Distance (as the crow flies, in tiles):\t%.0f%n",
				Double.valueOf(distance(start, end, dims)));
	}

	/**
	 * @param base a first point
	 * @param dest a second point
	 * @param dims the dimensions of the map
	 * @return the distance between the two points
	 */
	private static double distance(final Point base, final Point dest,
								final MapDimensions dims) {
		final int rawXDiff = base.getRow() - dest.getRow();
		final int rawYDiff = base.getCol() - dest.getCol();
		final int xDiff;
		if (rawXDiff < (dims.rows / 2)) {
			xDiff = rawXDiff;
		} else {
			xDiff = dims.rows - rawXDiff;
		}
		final int yDiff;
		if (rawYDiff < (dims.cols / 2)) {
			yDiff = rawYDiff;
		} else {
			yDiff = dims.cols - rawYDiff;
		}
		return Math.round(Math.sqrt((xDiff * xDiff) + (yDiff * yDiff)));

	}
	/**
	 * Run herding.
	 *
	 * @param cli the interface to the user
	 * @param huntModel the hunting model (used for hours remaining after herding is done)
	 * @throws IOException on I/O error dealing with user input
	 */
	private static void herd(final ICLIHelper cli, final HuntingModel huntModel)
			throws IOException {
		final HerdModel herdModel;
		final boolean poultry;
		if (cli.inputBoolean("Are these small animals, like sheep?\t")) {
			herdModel = HerdModel.SmallMammals;
			poultry = false;
		} else if (cli.inputBoolean("Are these dairy cattle?\t")) {
			herdModel = HerdModel.DairyCattle;
			poultry = false;
		} else if (cli.inputBoolean("Are these chickens?\t")) {
			herdModel = HerdModel.Chickens;
			poultry = true;
		} else if (cli.inputBoolean("Are these turkeys?\t")) {
			herdModel = HerdModel.Turkeys;
			poultry = true;
		} else {
			herdModel = HerdModel.LargeMammals;
			poultry = false;
		}
		final int count = cli.inputNumber("How many animals?\t");
		if (count == 0) {
			cli.println("With no animals, no cost and no gain.");
			return;
		} else if (count < 0) {
			cli.println("Can't have a negative number of animals.");
			return;
		} else {
			final int herders = cli.inputNumber("How many herders?\t");
			if (herders <= 0) {
				cli.println("Can't herd with no herders.");
				return;
			}
			final int animalsPerHerder = ((count + herders) - 1) / herders;
			final DoubleToIntFunction round = dbl -> (int) (Math.ceil(dbl) + 0.1);
			final int hours;
			if (poultry) {
				cli.printf("Gathering eggs takes %d minutes; cleaning up after them,%n",
						Integer.valueOf(animalsPerHerder * herdModel.getDailyTimePerHead()));
				cli.printf(
						"which should be done every %d turns at least, takes %.1f " +
								"hours.%n",
						Integer.valueOf(herdModel.getExtraChoresInterval() + 1),
						Double.valueOf(animalsPerHerder * herdModel.getExtraTimePerHead() / 60.0));
				cli.printf("This produces %.0f %s, totaling %.1f oz.%n",
						Double.valueOf(herdModel.getProductionPerHead() * count),
						herdModel.getProductionUnit(),
						Double.valueOf(herdModel.getProductionPerHead() * herdModel.getPoundsCoefficient() * count));
				if (cli.inputBoolean("Do they do the cleaning this turn? ")) {
					hours = round.applyAsInt(
							animalsPerHerder * (herdModel.getDailyTimePerHead() +
														herdModel.getExtraTimePerHead()) /
									60.0);
				} else {
					hours = round.applyAsInt(
							animalsPerHerder * herdModel.getDailyTimePerHead() / 60.0);
				}
			} else {
				cli.printf("Tending the animals takes %d minutes, or %d minutes with ",
						Integer.valueOf(animalsPerHerder * herdModel.getDailyTimePerHead() / 2),
						Integer.valueOf(animalsPerHerder * (herdModel.getDailyTimePerHead() / 2 - 5)));
				cli.println("expert herders, twice daily.");
				cli.printf("Gathering them for each milking takes %d min more.%n",
						Integer.valueOf(herdModel.getDailyTimeFloor() / 2));
				cli.printf("This produces %,.1f %s, %,.1f lbs, of milk per day.%n",
						Double.valueOf(herdModel.getProductionPerHead() * count),
						herdModel.getProductionUnit(),
						Double.valueOf(herdModel.getProductionPerHead() *
											   herdModel.getPoundsCoefficient() * count));
				if (cli.inputBoolean("Are the herders experts? ")) {
					hours = round.applyAsInt(
							((animalsPerHerder * (herdModel.getDailyTimePerHead() - 5)) +
									 herdModel.getDailyTimeFloor()) / 60.0);
				} else {
					hours = round.applyAsInt(
							(animalsPerHerder * herdModel.getDailyTimePerHead() +
									 herdModel.getDailyTimeFloor()) / 60.0);
				}
			}
			if ((hours < HUNTER_HOURS) &&
						cli.inputBoolean("Spend remaining time as Food Gatherers? ")) {
				gather(huntModel, selectPoint(cli), cli, HUNTER_HOURS - hours);
			}
		}
	}

	/**
	 * Run hunting, fishing, or trapping.
	 *
	 * @param huntModel     the hunting model
	 * @param point      where to hunt or fish
	 * @param land       true if this is hunting, false if fishing
	 * @param cli the interface to the user
	 * @param encounters how many encounters to show
	 */
	private static void hunt(final HuntingModel huntModel, final Point point,
							final boolean land, final ICLIHelper cli,
							final int encounters) {
		if (land) {
			huntModel.hunt(point, encounters).forEach(cli::println);
		} else {
			huntModel.fish(point, encounters).forEach(cli::println);
		}
	}

	/**
	 * Run food-gathering.
	 *
	 * @param huntModel     the hunting model to get results from
	 * @param point      around where to gather
	 * @param cli the interface to the user
	 * @param encounters how many encounters to show
	 */
	private static void gather(final HuntingModel huntModel, final Point point,
							final ICLIHelper cli, final int encounters) {
		huntModel.gather(point, encounters).forEach(cli::println);
	}

	/**
	 * Give the data the player automatically knows about a user-specified tile if he has
	 * a fortress on it.
	 *
	 * @param map      the map
	 * @param location the selected location
	 * @param cli the interface to the user
	 */
	private static void fortressInfo(final IMapNG map, final Point location,
									final ICLIHelper cli) {
		cli.print("Terrain is ");
		cli.println(NullCleaner.assertNotNull(map.getBaseTerrain(location).toString()));
		final List<TileFixture> fixtures =
				map.streamOtherFixtures(location).collect(Collectors.toList());
		final Collection<Ground> ground = new ArrayList<>();
		@Nullable
		final Ground locGround = map.getGround(location);
		if (locGround != null) {
			ground.add(locGround);
		}
		@Nullable
		final Forest forest = map.getForest(location);
		final Collection<Forest> forests = new ArrayList<>();
		if (forest != null) {
			forests.add(forest);
		}
		for (final TileFixture fix : fixtures) {
			if (fix instanceof Ground) {
				ground.add((Ground) fix);
			} else if (fix instanceof Forest) {
				forests.add((Forest) fix);
			}
		}
		if (!ground.isEmpty()) {
			cli.println("Kind(s) of ground (rock) on the tile:");
			ground.stream().map(Object::toString).forEach(cli::println);
		}
		if (!forests.isEmpty()) {
			cli.println("Kind(s) of forests on the tile:");
			forests.stream().map(Object::toString).forEach(cli::println);
		}
	}

	/**
	 * @param cli the interface to the user
	 * @return the point the user specifies.
	 * @throws IOException on I/O error.
	 */
	private static Point selectPoint(final ICLIHelper cli) throws IOException {
		return PointFactory.point(cli.inputNumber("Row: "),
				cli.inputNumber("Column: "));
	}

	/**
	 * @return a String representation of the object.
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "QueryCLI";
	}

	/**
	 * Prints a usage message.
	 *
	 * @param cli the interface to the user
	 */
	public static void usage(final ICLIHelper cli) {
		cli.println("The following commands are supported:");
		cli.print("Fortress: Print what a player automatically knows ");
		cli.println("about his fortress's tile.");
		final Integer encounters = Integer.valueOf(HUNTER_HOURS * HOURLY_ENCOUNTERS);
		cli.printf("Hunt/fIsh: Generates up to %d encounters with animals.%n", encounters);
		cli.printf("Gather: Generates up to %d encounters with fields, meadows, ",
				encounters);
		cli.println("groves, orchards, or shrubs.");
		cli.print("hErd: Determine the output from and time required for ");
		cli.println("maintaining a herd.");
		cli.print("Trap: Switch to the trap-modeling program ");
		cli.println("to run trapping or fish-trapping.");
		cli.println("Distance: Report the distance between two points.");
		cli.println("Count: Count how many workers belong to a player.");
		cli.println("Quit: Exit the program.");
	}

	/**
	 * Run the driver.
	 *
	 * @param model the driver model
	 * @throws DriverFailedException on I/O error
	 */
	@Override
	public void startDriver(final IDriverModel model) throws DriverFailedException {
		try (final ICLIHelper cli = new CLIHelper()) {
			repl(model, cli);
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error closing CLIHelper", except);
		}
	}

	/**
	 * Run the driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException if something goes wrong
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			throw new IncorrectUsageException(usage());
		}
		SimpleDriver.super.startDriver(args);
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE;
	}
}
