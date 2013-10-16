package controller.map.drivers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.PointFactory;
import model.map.Tile;
import model.map.TileFixture;
import model.map.fixtures.Ground;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Shrub;
import model.map.fixtures.terrain.Forest;
import util.TypesafeLogger;
import util.Warning;
import view.util.SystemOut;
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
	 * @param map the map to explore
	 * @param ostream the stream to write output to
	 */
	private void repl(final IMap map, final PrintStream ostream) {
		try {
			String input = helper.inputString("Command: ");
			while (input.length() > 0 && input.charAt(0) != 'q') {
				handleCommand(map, ostream, input.charAt(0));
				input = helper.inputString("Command: ");
			}
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O exception", except);
		}
	}

	/**
	 * @param map the map
	 * @param ostream the stream to write to
	 * @param input the command
	 *
	 * @throws IOException on I/O error
	 */
	public void handleCommand(final IMap map, final PrintStream ostream,
			final char input) throws IOException {
		switch (input) {
		case '?':
			usage(ostream);
			break;
		case 'f':
			fortressInfo(selectTile(map), ostream);
			break;
		case 'h':
		case 'i':
			hunt(CLIHelper.toList(selectTile(map)), ostream, HUNTER_HOURS
					* HOURLY_ENCOUNTERS);
			break;
		case 'g':
			gather(CLIHelper.toList(selectTile(map)), ostream, HUNTER_HOURS
					* HOURLY_ENCOUNTERS);
			break;
		default:
			ostream.println("Unknown command.");
			break;
		}
	}

	/**
	 * Run hunting, fishing, or trapping.
	 *
	 * @param fixtures a list of the fixtures on the tile
	 * @param ostream the stream to write to
	 * @param encounters how many encounters to show
	 */
	private static void hunt(final List<TileFixture> fixtures,
			final PrintStream ostream, final int encounters) {
		for (int i = 0; i < encounters; i++) {
			Collections.shuffle(fixtures);
			final TileFixture fix = fixtures.get(0);
			if (fix instanceof Animal) {
				ostream.println(fix);
			} else {
				ostream.print("nothing ... (");
				ostream.print(fix.getClass().getName());
				ostream.println(')');
			}
		}
	}

	/**
	 * Run food-gathering.
	 *
	 * @param fixtures a list of the fixtures on the tile
	 * @param ostream the stream to write to
	 * @param encounters how many encounters to show
	 */
	private static void gather(final List<TileFixture> fixtures,
			final PrintStream ostream, final int encounters) {
		for (int i = 0; i < encounters; i++) {
			Collections.shuffle(fixtures);
			final TileFixture fix = fixtures.get(0);
			if (fix instanceof Grove || fix instanceof Meadow
					|| fix instanceof Shrub) {
				ostream.println(fix);
			} else {
				ostream.print("nothing ... (");
				ostream.print(fix.getClass().getName());
				ostream.println(')');
			}
		}
	}

	/**
	 * Give the data the player automatically knows about a user-specified tile
	 * if he has a fortress on it.
	 *
	 * @param tile the selected tile
	 * @param ostream the stream to print results to
	 */
	private static void fortressInfo(final Tile tile, final PrintStream ostream) {
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
	private Tile selectTile(final IMap map) throws IOException {
		return map.getTile(PointFactory.point(helper.inputNumber("Row: "),
				helper.inputNumber("Column: ")));
	}

	/**
	 * How many hours we assume a working day is for a hunter or such.
	 */
	private static final int HUNTER_HOURS = 10;
	/**
	 * How many encounters per hour for a hunter or such.
	 */
	private static final int HOURLY_ENCOUNTERS = 4;

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
		ostream.println("Fortress: Prints what a player automatically knows about his fortress's tile.");
		final int encounters = HUNTER_HOURS * HOURLY_ENCOUNTERS;
		ostream.print("Hunt/fIsh: Generates up to ");
		ostream.print(encounters);
		ostream.println(" encounters with animals.");
		ostream.print("Gather: Generates up to ");
		ostream.print(encounters);
		ostream.println(" encounters with fields, meadows, groves, orchards, or shrubs.");
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
		try {
			repl(new MapReaderAdapter().readMap(args[0], new Warning(
					Warning.Action.Warn)), SystemOut.SYS_OUT);
		} catch (final XMLStreamException e) {
			throw new DriverFailedException("XML parsing error in " + args[0],
					e);
		} catch (final FileNotFoundException e) {
			throw new DriverFailedException("File " + args[0] + " not found", e);
		} catch (final IOException e) {
			throw new DriverFailedException("I/O error reading " + args[0], e);
		} catch (final SPFormatException e) {
			throw new DriverFailedException("Map " + args[0]
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
