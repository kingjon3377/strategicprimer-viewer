package controller.map.drivers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.HasName;
import model.map.IMap;
import model.map.MapDimensions;
import model.map.PointFactory;
import model.map.Tile;
import model.map.TileFixture;
import model.map.fixtures.mobile.Animal;
import util.TypesafeLogger;
import util.Warning;
import view.util.SystemOut;
import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.CLIHelper;
import controller.map.misc.MapReaderAdapter;

/**
 * A driver to run a player's trapping activity.
 *
 * @author Jonathan Lovelace
 *
 */
public class TrapModelDriver implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-r",
			"--trap", ParamCount.One, "Run a player's trapping",
			"Determine the results a player's trapper finds.",
			TrapModelDriver.class);
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(TrapModelDriver.class);

	/**
	 * The possible commands.
	 */
	private static enum TrapperCommand implements HasName {
		/**
		 * Set or reset a trap.
		 */
		Set("Set or reset a trap"),
		/**
		 * Check a trap.
		 */
		Check("Check a trap"),
		/**
		 * Move to the next trap.
		 */
		Move("Move to another trap"),
		/**
		 * Reset a trap that's made for easy resetting.
		 */
		EasyReset("Reset a foothold trap, e.g."),
		/**
		 * Quit.
		 */
		Quit("Quit");
		/**
		 * The "name" of the command.
		 */
		private final String name;

		/**
		 * Constructor.
		 *
		 * @param cName the "name" of the command
		 */
		private TrapperCommand(final String cName) {
			name = cName;
		}

		/**
		 * @return the "name" of the command
		 */
		@Override
		public String getName() {
			return name;
		}

		/**
		 * @param tname ignored
		 */
		@Override
		public void setName(final String tname) {
			throw new IllegalStateException("Can't rename");
		}
	}

	/**
	 * List of commands.
	 */
	private static final List<TrapperCommand> COMMANDS = Collections
			.unmodifiableList(Arrays.asList(TrapperCommand.values()));

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
	 * Helper to get numbers from the user, etc.
	 */
	private final CLIHelper helper = new CLIHelper();

	/**
	 * @param map the map to explore
	 * @param ostream the stream to write output to
	 */
	private void repl(final IMap map, final PrintStream ostream) {
		try {
			final boolean fishing = helper
					.inputBoolean("Is this a fisherman trapping fish rather than a trapper? ");
			final String name = fishing ? "fisherman" : "trapper";
			int minutes = helper.inputNumber("How many hours will the " + name
					+ " work? ")
					* MINS_PER_HOUR;
			final int row = helper.inputNumber("Row of the tile where the "
					+ name + " is working: ");
			final int col = helper.inputNumber("Column of that tile: ");
			final List<TileFixture> fixtures = getChoices(map, row, col);
			int input = -1;
			while (minutes > 0 && input < TrapperCommand.values().length) {
				if (input >= 0) {
					final TrapperCommand command = TrapperCommand.values()[input];
					assert command != null;
					minutes -= handleCommand(fixtures, ostream,
							command, fishing);
					ostream.print(inHours(minutes));
					ostream.println(" remaining");
					if (command == TrapperCommand.Quit) {
						break;
					}
				}
				input = helper.chooseFromList(COMMANDS, "What should the "
						+ name + " do next?", "Oops! No commands",
						"Next action: ", false);
			}
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O exception", except);
		}
	}

	/**
	 * The number of minutes in an hour.
	 */
	private static final int MINS_PER_HOUR = 60;

	/**
	 * @param minutes a number of minutes
	 * @return a String representation, including the number of hours
	 */
	private static String inHours(final int minutes) {
		return minutes < MINS_PER_HOUR ? Integer.toString(minutes) + " minutes"
				: Integer.toString(minutes / MINS_PER_HOUR) + " hours, "
						+ Integer.toString(minutes % MINS_PER_HOUR)
						+ " minutes";
	}

	/**
	 * @param map the map
	 * @param row a row coordinate
	 * @param col a column coordinate
	 * @return the list of all fixtures in the map in that tile or an adjacent
	 *         tile
	 */
	private static List<TileFixture> getChoices(final IMap map, final int row,
			final int col) {
		final List<TileFixture> retval = new ArrayList<>();
		final MapDimensions dims = map.getDimensions();
		for (int i = row - 1; i < row + 2; i++) {
			for (int j = col - 1; j < col + 2; j++) {
				final Tile tile = map.getTile(PointFactory.point(
						(i + dims.rows) % dims.rows, (j + dims.cols)
								% dims.cols));
				for (final TileFixture fix : tile) {
					retval.add(fix);
				}
			}
		}
		return retval;
	}

	/**
	 * Handle a command.
	 *
	 * @param fixtures the fixtures to choose from
	 * @param ostream the output stream to write to
	 * @param command the command to handle
	 * @param fishing whether we're dealing with *fish* traps .. which take
	 *        different amounts of time
	 * @return how many minutes it took to execute the command
	 * @throws IOException on I/O error interacting with user
	 */
	private int handleCommand(final List<TileFixture> fixtures,
			final PrintStream ostream, final TrapperCommand command,
			final boolean fishing) throws IOException {
		switch (command) {
		case Check:
			Collections.shuffle(fixtures);
			// ESCA-JAVA0177:
			final int retval; // NOPMD
			if (fixtures.get(0) instanceof Animal) {
				ostream.print("Found either ");
				ostream.print(fixtures.get(0).toString());
				ostream.println(" or evidence of it escaping.");
				retval = helper
						.inputNumber("How long to check and deal with animal? ");
			} else {
				ostream.println("Nothing in the trap");
				retval = fishing ? 5 : 10;
			}
			return retval; // NOPMD
		case EasyReset:
			return fishing ? 20 : 5; // NOPMD
		case Move:
			return 2; // NOPMD
		case Quit:
			return 0; // NOPMD
		case Set:
			return fishing ? 30 : 45; // NOPMD
		default:
			throw new IllegalArgumentException("Unhandled case");
		}
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
		final String filename = args[0];
		assert filename != null;
		try {
			repl(new MapReaderAdapter().readMap(filename, new Warning(
					Warning.Action.Warn)), SystemOut.SYS_OUT);
		} catch (final XMLStreamException e) {
			throw new DriverFailedException("XML parsing error in " + filename,
					e);
		} catch (final FileNotFoundException e) {
			throw new DriverFailedException("File " + filename + " not found", e);
		} catch (final IOException e) {
			throw new DriverFailedException("I/O error reading " + filename, e);
		} catch (final SPFormatException e) {
			throw new DriverFailedException("Map " + filename
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

}
