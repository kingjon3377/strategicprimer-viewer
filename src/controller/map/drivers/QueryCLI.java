package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import model.exploration.PoultryModel;
import model.exploration.HerdModel;
import model.exploration.HuntingModel;
import model.exploration.MammalModel;
import model.exploration.SurroundingPointIterable;
import model.map.DistanceComparator;
import model.map.FixtureIterable;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.MapDimensions;
import model.map.Player;
import model.map.Point;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.Ground;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.terrain.Forest;
import model.misc.IDriverModel;
import util.TypesafeLogger;

/**
 * A driver for running exploration results, etc., using the new model.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class QueryCLI implements SimpleDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final IDriverUsage USAGE =
			new DriverUsage(false, "-q", "--query", ParamCount.One,
								   "Answer questions about a map.",
								   "Look at tiles on a map. Or run hunting, gathering," +
										   " or fishing.");

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
	 * Accept and respond to commands.
	 * @param options options passed to the driver
	 * @param model   the driver model containing the map to explore
	 * @param cli     the interface to the user
	 */
	private static void repl(final SPOptions options, final IDriverModel model,
							 final ICLIHelper cli) {
		final HuntingModel huntModel = new HuntingModel(model.getMap());
		try {
			String input = cli.inputString("Command: ");
			while (!input.isEmpty() && (input.charAt(0) != 'q')) {
				handleCommand(options, model, huntModel, cli, input.charAt(0));
				input = cli.inputString("Command: ");
			}
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			LOGGER.log(Level.SEVERE, "I/O exception", except);
		}
	}

	/**
	 * Handle a user command.
	 * @param options   options passed to the driver
	 * @param model     the driver model
	 * @param huntModel the hunting model
	 * @param cli       the interface to the user
	 * @param input     the command
	 * @throws IOException           on I/O error
	 */
	private static void handleCommand(final SPOptions options, final IDriverModel model,
									 final HuntingModel huntModel, final ICLIHelper cli,
									 final char input) throws IOException {
		switch (input) {
		case '?':
			usage(cli);
			break;
		case 'f':
			fortressInfo(model.getMap(), cli.inputPoint("Location of fortress? "), cli);
			break;
		case 'h':
			hunt(huntModel, cli.inputPoint("Location to hunt? "), true, cli,
					HUNTER_HOURS * HOURLY_ENCOUNTERS);
			break;
		case 'i':
			hunt(huntModel, cli.inputPoint("Location to fish? "), false, cli,
					HUNTER_HOURS * HOURLY_ENCOUNTERS);
			break;
		case 'g':
			gather(huntModel, cli.inputPoint("Location to gather? "), cli,
					HUNTER_HOURS * HOURLY_ENCOUNTERS);
			break;
		case 'e':
			herd(cli, huntModel);
			break;
		case 't':
			new TrapModelDriver().startDriver(cli, options, model);
			break;
		case 'd':
			distance(model.getMapDimensions(), cli);
			break;
		case 'c':
			count(model.getMap(),
					model.getMap().streamPlayers().collect(Collectors.toList()), cli);
			break;
		case 'u':
			final Point base = cli.inputPoint("Starting point? ");
			final Optional<Point> unexplored = findUnexplored(model.getMap(), base);
			if (unexplored.isPresent()) {
				final Point point = unexplored.get();
				cli.printf("Nearest unexplored tile is (%d, %d), %.1f tiles away%n",
						Integer.valueOf(point.getRow()), Integer.valueOf(point.getCol()),
						Double.valueOf(
								distance(base, point, model.getMap().dimensions())));
			} else {
				cli.println("No unexplored tiles found.");
			}
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
	 * @param cli     the interface to the user
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
			count += countWorkers(map.getOtherFixtures(loc), player);
		}
		cli.printf("%s has %d workers.%n", player.getName(), Integer.valueOf(count));
	}
	/**
	 * Count the workers owned by the given player.
	 * @param iter an iterable over fixtures
	 * @param owner a player
	 * @return how many Workers it recursively contains owned by that player
	 */
	private static int countWorkers(final Iterable<? extends IFixture> iter,
									final Player owner) {
		int retval = 0;
		for (final IFixture fix : iter) {
			if (fix instanceof IWorker && iter instanceof HasOwner &&
						owner.equals(((HasOwner) iter).getOwner())) {
				retval++;
			} else if (fix instanceof FixtureIterable) {
				retval += countWorkers((FixtureIterable<?>) fix, owner);
			}
		}
		return retval;
	}
	/**
	 * Report the distance between two points.
	 *
	 * TODO: use some sort of pathfinding
	 *
	 * @param dims the dimensions of the map
	 * @param cli  the interface to the user
	 * @throws IOException on I/O error dealing with user input
	 */
	private static void distance(final MapDimensions dims, final ICLIHelper cli)
			throws IOException {
		final Point start = cli.inputPoint("Starting point:\t");
		final Point end = cli.inputPoint("Destination:\t");
		cli.printf("Distance (as the crow flies, in tiles):\t%.0f%n",
				Double.valueOf(distance(start, end, dims)));
	}

	/**
	 * Calculate the distance between two points.
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
	 * Round a double up.
	 * @param dbl the number to round
	 * @return the next integer above it if it's not already integral.
	 */
	private static int round(final double dbl) {
		return (int) (Math.ceil(dbl) + 0.1);
	}
	/**
	 * Run herding.
	 *
	 * @param cli       the interface to the user
	 * @param huntModel the hunting model (used for hours remaining after herding is
	 *                  done)
	 * @throws IOException on I/O error dealing with user input
	 */
	private static void herd(final ICLIHelper cli, final HuntingModel huntModel)
			throws IOException {
		final HerdModel herdModel;
		if (cli.inputBooleanInSeries("Are these small animals, like sheep?\t")) {
			herdModel = MammalModel.SmallMammals;
		} else if (cli.inputBooleanInSeries("Are these dairy cattle?\t")) {
			herdModel = MammalModel.DairyCattle;
		} else if (cli.inputBooleanInSeries("Are these chickens?\t")) {
			herdModel = PoultryModel.Chickens;
		} else if (cli.inputBooleanInSeries("Are these turkeys?\t")) {
			herdModel = PoultryModel.Turkeys;
		} else if (cli.inputBooleanInSeries("Are these pigeons?\t")) {
			herdModel = PoultryModel.Pigeons;
		} else {
			herdModel = MammalModel.LargeMammals;
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
			final int flockPerHerder = ((count + herders) - 1) / herders;
			final int hours;
			if (herdModel instanceof PoultryModel) {
				hours = herdPoultry(cli, (PoultryModel) herdModel, count, flockPerHerder);
			} else {
				hours = herdMammals(cli, (MammalModel) herdModel, count, flockPerHerder);
			}
			if ((hours < HUNTER_HOURS) &&
						cli.inputBooleanInSeries(
								"Spend remaining time as Food Gatherers? ")) {
				gather(huntModel, cli.inputPoint("Gathering location? "), cli,
						HUNTER_HOURS - hours);
			}
		}
	}

	/**
	 * Handle herding mammals.
	 * @param cli the interface for user I/O
	 * @param animal the model of the particular kind of animal being herded
	 * @param count how many animals there are
	 * @param flockPerHerder how many animals there are per herder
	 * @return how many hours each herder spends "herding"
	 * @throws IOException on I/O error
	 */
	private static int herdMammals(final ICLIHelper cli, final MammalModel animal,
								   final int count, final int flockPerHerder)
			throws IOException {
		cli.printf("Tending the animals takes %d minutes, or %d minutes with ",
				Integer.valueOf((flockPerHerder * animal.getDailyTimePerHead()) / 2),
				Integer.valueOf(
						flockPerHerder * ((animal.getDailyTimePerHead() / 2) - 5)));
		cli.println("expert herders, twice daily.");
		cli.printf("Gathering them for each milking takes %d min more.%n",
				Integer.valueOf(animal.getDailyTimeFloor() / 2));
		final double production = animal.getProductionPerHead().getNumber().doubleValue();
		cli.printf("This produces %,.1f %s, %,.1f lbs, of milk per day.%n",
				Double.valueOf(production * count),
				animal.getProductionPerHead().getUnits(),
				Double.valueOf(production * animal.getPoundsCoefficient() * count));
		final int costPerHerder;
		if (cli.inputBooleanInSeries("Are the herders experts? ")) {
			costPerHerder = animal.getDailyTimePerHead() - 5;
		} else {
			costPerHerder = animal.getDailyTimePerHead();
		}
		return round(((flockPerHerder * costPerHerder) + animal.getDailyTimeFloor()) /
							  60.0);
	}

	/**
	 * Run herding of poultry.
	 * @param cli The interface for user I/O
	 * @param bird the model of the specific kind of bird being "herded"
	 * @param count how many there are in the flock
	 * @param flockPerHerder how many there are per herder
	 * @return how long each herder spends "herding"
	 * @throws IOException on I/O error
	 */
	private static int herdPoultry(final ICLIHelper cli, final PoultryModel bird,
								   final int count, final int flockPerHerder)
			throws IOException {
		cli.printf("Gathering eggs takes %d minutes; cleaning up after them,%n",
				Integer.valueOf(flockPerHerder * bird.getDailyTimePerHead()));
		cli.printf("which should be done every %d turns at least, takes %.1f hours.%n",
				Integer.valueOf(bird.getExtraChoresInterval() + 1),
				Double.valueOf((flockPerHerder * bird.getExtraTimePerHead()) / 60.0));
		final double production = bird.getProductionPerHead().getNumber().doubleValue();
		cli.printf("This produces %.0f %s, totaling %.1f oz.%n",
				Double.valueOf(production * count),
				bird.getProductionPerHead().getUnits(),
				Double.valueOf(production * bird.getPoundsCoefficient() * count));
		final int cost;
		if (cli.inputBooleanInSeries("Do they do the cleaning this turn? ")) {
			cost = bird.getDailyTimePerHead() + bird.getExtraTimePerHead();
		} else {
			cost = bird.getDailyTimePerHead();
		}
		return round((flockPerHerder * cost) / 60.0);
	}

	/**
	 * Run hunting, fishing, or trapping.
	 *
	 * @param huntModel  the hunting model
	 * @param point      where to hunt or fish
	 * @param land       true if this is hunting, false if fishing
	 * @param cli        the interface to the user
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
	 * @param huntModel  the hunting model to get results from
	 * @param point      around where to gather
	 * @param cli        the interface to the user
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
	 * @param cli      the interface to the user
	 */
	private static void fortressInfo(final IMapNG map, final Point location,
									 final ICLIHelper cli) {
		cli.print("Terrain is ");
		cli.println(map.getBaseTerrain(location).toString());
		final List<TileFixture> fixtures =
				map.streamAllFixtures(location).filter(Objects::nonNull)
						.collect(Collectors.toList());
		final Collection<Ground> ground = new ArrayList<>();
		final Collection<Forest> forests = new ArrayList<>();
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
	 * Find the nearest obviously-reachable unexplored location.
	 * @param map  the map
	 * @param base the starting point
	 * @return the nearest obviously-reachable unexplored point
	 */
	private static Optional<Point> findUnexplored(final IMapNG map, final Point base) {
		final Queue<Point> queue = new LinkedList<>();
		queue.add(base);
		final MapDimensions dimensions = map.dimensions();
		final Collection<Point> considered = new HashSet<>();
		final List<Point> retval = new ArrayList<>();
		while (!queue.isEmpty()) {
			final Point current = queue.remove();
			final TileType currentTerrain = map.getBaseTerrain(current);
			if (considered.contains(current)) {
				continue;
			} else if (currentTerrain == TileType.NotVisible) {
				retval.add(current);
			} else if (currentTerrain != TileType.Ocean) {
				final double baseDistance = distance(base, current, dimensions);
				new SurroundingPointIterable(current, dimensions, 1).stream()
						.filter(neighbor -> distance(base, neighbor, dimensions) >=
													baseDistance).forEach(queue::add);
			}
			considered.add(current);
		}
		if (retval.isEmpty()) {
			return Optional.empty();
		} else {
			retval.sort(new DistanceComparator(base));
			return Optional.of(retval.get(0));
		}
	}

	/**
	 * Prints a usage message.
	 *
	 * @param cli the interface to the user
	 */
	private static void usage(final ICLIHelper cli) {
		cli.println("The following commands are supported:");
		cli.print("Fortress: Print what a player automatically knows ");
		cli.println("about his fortress's tile.");
		final Integer encounters = Integer.valueOf(HUNTER_HOURS * HOURLY_ENCOUNTERS);
		//noinspection HardcodedFileSeparator
		cli.printf("Hunt/fIsh: Generates up to %d encounters with animals.%n",
				encounters);
		cli.printf("Gather: Generates up to %d encounters with fields, meadows, ",
				encounters);
		cli.println("groves, orchards, or shrubs.");
		cli.print("hErd: Determine the output from and time required for ");
		cli.println("maintaining a herd.");
		cli.print("Trap: Switch to the trap-modeling program ");
		cli.println("to run trapping or fish-trapping.");
		cli.println("Distance: Report the distance between two points.");
		cli.println("Count: Count how many workers belong to a player.");
		cli.println("Unexplored: Find the nearest unexplored water not behind water.");
		cli.println("Quit: Exit the program.");
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object.
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "QueryCLI";
	}

	/**
	 * Run the driver.
	 *
	 * @param cli     the interface for console I/O
	 * @param options options passed to the driver
	 * @param model   the driver model
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model) {
		repl(options, model, cli);
	}

	/**
	 * Run the driver.
	 *
	 * @param cli the interface for user I/O
	 * @param options command-line options passed in
	 * @param args    command-line arguments
	 * @throws DriverFailedException if something goes wrong
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final String... args)
			throws DriverFailedException {
		if (args.length == 0) {
			throw new IncorrectUsageException(usage());
		}
		SimpleDriver.super.startDriver(cli, options, args);
	}

	/**
	 * The usage object.
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public IDriverUsage usage() {
		return USAGE;
	}
}
