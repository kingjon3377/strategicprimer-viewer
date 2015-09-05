package controller.map.drivers;

import static view.util.SystemOut.SYS_OUT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.CLIHelper;
import controller.map.misc.ICLIHelper;
import controller.map.misc.MapReaderAdapter;
import model.exploration.HuntingModel;
import model.map.IMapNG;
import model.map.MapDimensions;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;
import model.map.fixtures.FortressMember;
import model.map.fixtures.Ground;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.towns.Fortress;
import util.TypesafeLogger;
import util.Warning;
import util.Warning.Action;

/**
 * A driver for running exploration results, etc., using the new model.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 */
public final class QueryCLI implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-m",
			"--map", ParamCount.One, "Answer questions about a map.",
			"Look at tiles on a map. Or run hunting, gathering, or fishing.",
			QueryCLI.class);

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(QueryCLI.class);
	/**
	 * Helper to get numbers from the user, etc.
	 */
	private final ICLIHelper helper = new CLIHelper();

	/**
	 * How many hours we assume a working day is for a hunter or such.
	 */
	private static final int HUNTER_HOURS = 10;
	/**
	 * How many encounters per hour for a hunter or such.
	 */
	private static final int HOURLY_ENCOUNTERS = 4;

	/**
	 * @param map the map to explore
	 * @param ostream the stream to write output to
	 */
	private void repl(final IMapNG map, final Appendable ostream) {
		final HuntingModel hmodel = new HuntingModel(map);
		try {
			String input = helper.inputString("Command: ");
			while (input.length() > 0 && input.charAt(0) != 'q') {
				handleCommand(map, hmodel, ostream, input.charAt(0));
				input = helper.inputString("Command: ");
			}
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O exception", except);
		}
	}

	/**
	 * @param map the map
	 * @param hmodel the hunting model
	 * @param ostream the stream to write to
	 * @param input the command
	 *
	 * @throws IOException on I/O error
	 */
	public void handleCommand(final IMapNG map, final HuntingModel hmodel,
			final Appendable ostream, final char input) throws IOException {
		switch (input) {
		case '?':
			usage(ostream);
			break;
		case 'f':
			fortressInfo(map, selectPoint(), ostream);
			break;
		case 'h':
			hunt(hmodel, selectPoint(), true, ostream, HUNTER_HOURS
					* HOURLY_ENCOUNTERS);
			break;
		case 'i':
			hunt(hmodel, selectPoint(), false, ostream, HUNTER_HOURS
					* HOURLY_ENCOUNTERS);
			break;
		case 'g':
			gather(hmodel, selectPoint(), ostream, HUNTER_HOURS
					* HOURLY_ENCOUNTERS);
			break;
		case 'e':
			herd(ostream);
			break;
		case 't':
			new TrapModelDriver().startDriver(map);
			break;
		case 'd':
			distance(map.dimensions(), ostream);
			break;
		case 'c':
			count(map, CLIHelper.toList(map.players()), ostream);
			break;
		default:
			ostream.append("Unknown command.\n");
			break;
		}
	}
	/**
	 * Count the workers belonging to a player.
	 * @param map the map
	 * @param players the list of players in the map
	 * @param ostream the stream to write results to
	 * @throws IOException on I/O error interacting with user
	 */
	private void count(final IMapNG map, final List<Player> players,
			final Appendable ostream) throws IOException {
		final int playerNum = helper.chooseFromList(players,
				"Players in the map:", "Map contains no players",
				"Owner of workers to count: ", true);
		if (playerNum < 0 || playerNum >= players.size()) {
			return;
		}
		Player player = players.get(playerNum);
		int count = 0;
		for (Point loc : map.locations()) {
			for (TileFixture fix : map.getOtherFixtures(loc)) {
				if (fix instanceof IUnit
						&& player.equals(((IUnit) fix).getOwner())) {
					for (UnitMember member : (IUnit) fix) {
						if (member instanceof IWorker) {
							count++;
						}
					}
				} else if (fix instanceof Fortress) {
					for (FortressMember unit : (Fortress) fix) {
						if (unit instanceof IUnit
								&& player.equals(((IUnit) unit).getOwner())) {
							for (UnitMember member : (IUnit) unit) {
								if (member instanceof IWorker) {
									count++;
								}
							}
						}
					}
				}
			}
		}
		ostream.append(player.getName());
		ostream.append(" has ");
		ostream.append(Integer.toString(count));
		ostream.append(" workers.\n");
	}

	/**
	 * Report the distance between two points.
	 *
	 * TODO: use some sort of pathfinding
	 *
	 * @param dims the dimensions of the map
	 * @param ostream
	 *            the stream to write to
	 * @throws IOException on I/O error dealing with user input
	 */
	private void distance(final MapDimensions dims, final Appendable ostream)
			throws IOException {
		ostream.append("Starting point:\t");
		final Point start = selectPoint();
		ostream.append("Destination:\t");
		final Point end = selectPoint();
		final int rawXdiff = start.row - end.row;
		final int rawYdiff = start.col - end.col;
		final int xdiff;
		if (rawXdiff < (dims.rows / 2)) {
			xdiff = rawXdiff;
		} else {
			xdiff = dims.rows - rawXdiff;
		}
		final int ydiff;
		if (rawYdiff < (dims.cols / 2)) {
			ydiff = rawYdiff;
		} else {
			ydiff = dims.cols - rawYdiff;
		}
		ostream.append("Distance (as the crow flies, in tiles):\t");
		ostream.append(Long.toString(Math.round(Math.sqrt(xdiff
				* xdiff + ydiff * ydiff))));
	}
	/**
	 * Run herding. TODO: Move the logic here into the HuntingModel or a similar
	 * class.
	 *
	 * @param ostream
	 *            the stream to write to
	 * @throws IOException on I/O error dealing with user input
	 */
	private void herd(final Appendable ostream) throws IOException {
		final double rate; // The amount of milk per animal
		final int time; // How long it takes to milk one animal, in minutes.
		final boolean poultry;
		if (helper.inputBoolean("Are these small animals, like sheep?\t")) {
			rate = 1.5;
			time = 15;
			poultry = false;
		} else if (helper.inputBoolean("Are these dairy cattle?\t")) {
			rate = 4;
			time = 20;
			poultry = false;
		} else if (helper.inputBoolean("Are these chickens?\t")) {
			// TODO: Support other poultry
			rate = .75;
			time = 12;
			poultry = true;
		} else {
			rate = 3;
			time = 20;
			poultry = false;
		}
		final int count = helper.inputNumber("How many animals?\t");
		if (count == 0) {
			ostream.append("With no animals, no cost and no gain.\n");
			return; // NOPMD
		} else if (count < 0) {
			ostream.append("Can't have a negative number of animals.\n");
			return; // NOPMD
		}
		final int herders = helper.inputNumber("How many herders?\t");
		if (herders <= 0) {
			ostream.append("Can't herd with no herders.\n");
			return; // NOPMD
		}
		final int animalsPerHerder = (count + herders - 1) / herders;
		if (poultry) {
			ostream.append("Gathering eggs takes ");
			ostream.append(Integer.toString(animalsPerHerder * 2));
			ostream.append(" minutes; cleaning up after them,\n");
			ostream.append(String.format(
					"which should be done every third turn at least, takes %.1f hours.%n",
					Double.valueOf(animalsPerHerder * 0.5)));
			ostream.append(String.format(
					"This produces %.0f eggs, totaling %.1f oz.%n",
					Double.valueOf(rate * count),
					Double.valueOf(rate * 2.0 * count)));
		} else {
			ostream.append("Tending the animals takes ");
			ostream.append(Integer.toString(animalsPerHerder * time));
			ostream.append(" minutes, or ");
			ostream.append(Integer.toString(animalsPerHerder * (time - 5)));
			ostream.append(" minutes with expert herders, twice daily.\n");
			ostream.append("Gathering them for each milking takes 30 min more.\n");
			ostream.append(String.format(
					"This produces %,.1f gallons, %,.1f lbs, of milk per day.%n",
					Double.valueOf(rate * count),
					Double.valueOf(rate * 8.6 * count)));
		}
	}
	/**
	 * Run hunting, fishing, or trapping.
	 *
	 * @param hmodel the hunting model
	 * @param point where to hunt or fish
	 * @param land true if this is hunting, false if fishing
	 * @param ostream the stream to write to
	 * @param encounters how many encounters to show
	 * @throws IOException on I/O error writing to stream
	 */
	private static void hunt(final HuntingModel hmodel, final Point point,
			final boolean land, final Appendable ostream, final int encounters)
			throws IOException {
		if (land) {
			for (final String item : hmodel.hunt(point, encounters)) {
				ostream.append(item);
				ostream.append('\n');
			}
		} else {
			for (final String item : hmodel.fish(point, encounters)) {
				ostream.append(item);
				ostream.append('\n');
			}
		}
	}

	/**
	 * Run food-gathering.
	 *
	 * @param hmodel the hunting model to get results from
	 * @param point around where to gather
	 * @param ostream the stream to write to
	 * @param encounters how many encounters to show
	 * @throws IOException on I/O error writing to stream
	 */
	private static void gather(final HuntingModel hmodel, final Point point,
			final Appendable ostream, final int encounters) throws IOException {
		for (final String item : hmodel.gather(point, encounters)) {
			ostream.append(item);
			ostream.append('\n');
		}
	}

	/**
	 * Give the data the player automatically knows about a user-specified tile
	 * if he has a fortress on it.
	 *
	 * @param map the map
	 * @param location the selected location
	 * @param ostream the stream to print results to
	 * @throws IOException on I/O error writing to stream
	 */
	private static void fortressInfo(final IMapNG map, final Point location,
			final Appendable ostream) throws IOException {
		ostream.append("Terrain is ");
		ostream.append(map.getBaseTerrain(location).toString());
		ostream.append('\n');
		final List<TileFixture> fixtures =
				CLIHelper.toList(map.getOtherFixtures(location));
		final List<Ground> ground = new ArrayList<>();
		final List<Forest> forests = new ArrayList<>();
		final Ground localGround = map.getGround(location);
		if (localGround != null) {
			ground.add(localGround);
		}
		final Forest forest = map.getForest(location);
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
			ostream.append("Kind(s) of ground (rock) on the tile:\n");
			for (final Ground fix : ground) {
				ostream.append(fix.toString());
				ostream.append('\n');
			}
		}
		if (!forests.isEmpty()) {
			ostream.append("Kind(s) of forests on the tile:\n");
			for (final Forest fix : forests) {
				ostream.append(fix.toString());
				ostream.append('\n');
			}
		}
	}

	/**
	 * @return the poin the user specifies.
	 * @throws IOException on I/O error.
	 */
	private Point selectPoint() throws IOException {
		return PointFactory.point(helper.inputNumber("Row: "),
				helper.inputNumber("Column: "));
	}

	/**
	 *
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "ExplorationCLI";
	}

	/**
	 * Prints a usage message.
	 *
	 * @param ostream the stream to write it to.
	 * @throws IOException on I/O error writing to stream
	 */
	public static void usage(final Appendable ostream) throws IOException {
		ostream.append("The following commands are supported:\n");
		ostream.append("Fortress: Print what a player automatically knows ");
		ostream.append("about his fortress's tile.\n");
		final String encounters =
				Integer.toString(HUNTER_HOURS * HOURLY_ENCOUNTERS);
		ostream.append("Hunt/fIsh: Generates up to ");
		ostream.append(encounters);
		ostream.append(" encounters with animals.\n");
		ostream.append("Gather: Generates up to ");
		ostream.append(encounters);
		ostream.append(" encounters with fields, meadows, groves, ");
		ostream.append("orchards, or shrubs.\n");
		ostream.append("hErd: Determine the output from and time required for ");
		ostream.append("maintaining a herd.\n");
		ostream.append("Trap: Switch to the trap-modeling program ");
		ostream.append("to run trapping or fish-trapping.\n");
		ostream.append("Distance: Report the distance between two points.\n");
		ostream.append("Count: Count how many workers belong to a player.\n");
		ostream.append("Quit: Exit the program.\n");
	}

	/**
	 * Run the driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException if something goes wrong
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			throw new DriverFailedException("Need one argument",
					new IllegalArgumentException("Need one argument"));
		}
		final File file = new File(args[0]);
		try {
			repl(new MapReaderAdapter().readMap(file, new Warning(Action.Warn)),
					SYS_OUT);
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
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE_OBJ;
	}

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
}
