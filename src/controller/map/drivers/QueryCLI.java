package controller.map.drivers;

import static view.util.SystemOut.SYS_OUT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.exploration.HuntingModel;
import model.map.IMap;
import model.map.ITile;
import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;
import model.map.fixtures.Ground;
import model.map.fixtures.terrain.Forest;
import util.TypesafeLogger;
import util.Warning;
import util.Warning.Action;
import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.CLIHelper;
import controller.map.misc.MapReaderAdapter;

/**
 * A driver for running exploration results, etc., using the new model.
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
	private final CLIHelper helper = new CLIHelper();

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
	private void repl(final IMap map, final PrintStream ostream) {
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
	public void handleCommand(final IMap map, final HuntingModel hmodel,
			final PrintStream ostream, final char input) throws IOException {
		switch (input) {
		case '?':
			usage(ostream);
			break;
		case 'f':
			fortressInfo(selectTile(map), ostream);
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
		default:
			ostream.println("Unknown command.");
			break;
		}
	}

	/**
	 * Run herding. TODO: Move the logic here into the HuntingModel or a similar
	 * class.
	 *
	 * @param ostream
	 *            the stream to write to
	 * @throws IOException on I/O error dealing with user input
	 */
	private void herd(final PrintStream ostream) throws IOException {
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
			ostream.println("With no animals, no cost and no gain.");
			return; // NOPMD
		} else if (count < 0) {
			ostream.println("Can't have a negative number of animals.");
			return; // NOPMD
		}
		final int herders = helper.inputNumber("How many herders?\t");
		if (herders <= 0) {
			ostream.println("Can't herd with no herders.");
			return; // NOPMD
		}
		final int animalsPerHerder = (count + herders - 1) / herders;
		ostream.print("Tending the animals takes ");
		ostream.print(animalsPerHerder * time);
		ostream.print(" minutes, or ");
		ostream.print(animalsPerHerder * (time - 5));
		ostream.println(" minutes with expert herders, twice daily.");
		if (poultry) {
			ostream.println(String.format(
					"This produces %.0f eggs, totaling %.1f oz.",
					Double.valueOf(rate * count),
					Double.valueOf(rate * 2.0 * count)));
		} else {
			ostream.println("Gathering them for each milking takes 30 min more.");
			ostream.println(String.format(
					"This produces %,.1f gallons, %,.1f lbs, of milk per day.",
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
	 */
	private static void hunt(final HuntingModel hmodel, final Point point,
			final boolean land, final PrintStream ostream, final int encounters) {
		if (land) {
			for (final String item : hmodel.hunt(point, encounters)) {
				ostream.println(item);
			}
		} else {
			for (final String item : hmodel.fish(point, encounters)) {
				ostream.println(item);
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
	 */
	private static void gather(final HuntingModel hmodel, final Point point,
			final PrintStream ostream, final int encounters) {
		for (final String item : hmodel.gather(point, encounters)) {
			ostream.println(item);
		}
	}

	/**
	 * Give the data the player automatically knows about a user-specified tile
	 * if he has a fortress on it.
	 *
	 * @param tile the selected tile
	 * @param ostream the stream to print results to
	 */
	private static void fortressInfo(final ITile tile, final PrintStream ostream) {
		ostream.print("Terrain is ");
		ostream.println(tile.getTerrain());
		final List<TileFixture> fixtures = CLIHelper.toList(tile);
		final List<Ground> ground = new ArrayList<>();
		final List<Forest> forests = new ArrayList<>();
		for (final TileFixture fix : fixtures) {
			if (fix instanceof Ground) {
				ground.add((Ground) fix);
			} else if (fix instanceof Forest) {
				forests.add((Forest) fix);
			}
		}
		if (!ground.isEmpty()) {
			ostream.println("Kind(s) of ground (rock) on the tile:");
			for (final Ground fix : ground) {
				ostream.println(fix);
			}
		}
		if (!forests.isEmpty()) {
			ostream.println("Kind(s) of forests on the tile:");
			for (final Forest fix : forests) {
				ostream.println(fix);
			}
		}
	}

	/**
	 * @param map The map we're dealing with
	 * @return The tile the user specifies.
	 * @throws IOException on I/O error
	 */
	private ITile selectTile(final IMap map) throws IOException {
		return map.getTile(selectPoint());
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
	 * @param args command-line arguments
	 */
	public static void main(final String[] args) {
		try {
			new QueryCLI().startDriver(args);
		} catch (final DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
		}
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
	 */
	public static void usage(final PrintStream ostream) {
		ostream.println("The following commands are supported:");
		ostream.print("Fortress: Print what a player automatically knows ");
		ostream.println("about his fortress's tile.");
		final int encounters = HUNTER_HOURS * HOURLY_ENCOUNTERS;
		ostream.print("Hunt/fIsh: Generates up to ");
		ostream.print(encounters);
		ostream.println(" encounters with animals.");
		ostream.print("Gather: Generates up to ");
		ostream.print(encounters);
		ostream.print(" encounters with fields, meadows, groves, ");
		ostream.println("orchards, or shrubs.");
		ostream.print("hErd: Determine the output from and time required for ");
		ostream.println("maintaining a herd.");
		ostream.println("Quit: Exit the program.");
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
			repl(new MapReaderAdapter().readMap(file, new Warning(
					Action.Warn)), SYS_OUT);
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
