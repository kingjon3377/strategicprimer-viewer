package controller.map.drivers;

import static model.map.PointFactory.point;
import static view.util.SystemOut.SYS_OUT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
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
import model.map.HasName;
import model.map.IMapNG;
import model.map.Point;
import model.misc.IDriverModel;
import util.NullCleaner;
import util.TypesafeLogger;
import util.Warning;
import util.Warning.Action;

/**
 * A driver to run a player's trapping activity.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
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
	 * A somewhat lengthy prompt.
	 */
	private static final String FISH_OR_TRAP =
			"Is this a fisherman trapping fish rather than a trapper?";
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(TrapModelDriver.class);

	/**
	 * The number of minutes in an hour.
	 */
	private static final int MINS_PER_HOUR = 60;

	/**
	 * Helper to get numbers from the user, etc.
	 */
	private final ICLIHelper helper = new CLIHelper();

	/**
	 * How many minutes a fruitless check of a fishing trap takes.
	 */
	private static final int FRUITLESS_FISH_TRAP = 5;
	/**
	 * How many minutes a fruitless check of a trap takes.
	 */
	private static final int FRUITLESS_TRAP = 10;

	/**
	 * List of commands.
	 */
	private static final List<TrapperCommand> COMMANDS = Collections
			.unmodifiableList(Arrays.asList(TrapperCommand.values()));

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
	 * @param map the map to explore
	 * @param ostream the stream to write output to
	 */
	private void repl(final IMapNG map, final Appendable ostream) {
		try {
			final HuntingModel hmodel = new HuntingModel(map);
			final boolean fishing = helper.inputBoolean(FISH_OR_TRAP);
			final String name; // NOPMD
			if (fishing) {
				name = "fisherman";
			} else {
				name = "trapper";
			}
			int minutes = helper.inputNumber("How many hours will the " + name
					+ " work? ")
					* MINS_PER_HOUR;
			final int row = helper.inputNumber("Row of the tile where the "
					+ name + " is working: ");
			final int col = helper.inputNumber("Column of that tile: ");
			final Point point = point(row, col);
			final List<String> fixtures; // NOPMD
			if (fishing) {
				fixtures = hmodel.fish(point, minutes);
			} else {
				fixtures = hmodel.hunt(point, minutes);
			}
			int input = -1;
			while (minutes > 0 && input < TrapperCommand.values().length) {
				if (input >= 0) {
					final TrapperCommand command =
							NullCleaner
							.assertNotNull(TrapperCommand.values()[input]);
					minutes -= handleCommand(fixtures, ostream,
							command, fishing);
					ostream.append(inHours(minutes));
					ostream.append(" remaining\n");
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
	 * @param minutes a number of minutes
	 * @return a String representation, including the number of hours
	 */
	private static String inHours(final int minutes) {
		if (minutes < MINS_PER_HOUR) {
			return Integer.toString(minutes) + " minutes"; // NOPMD
		} else {
			return Integer.toString(minutes / MINS_PER_HOUR) + " hours, "
					+ Integer.toString(minutes % MINS_PER_HOUR)
					+ " minutes";
		}
	}
	/**
	 * Handle a command.
	 *
	 * @param fixtures
	 *            the animals generated from the tile and surrounding tiles.
	 * @param ostream
	 *            the output stream to write to
	 * @param command
	 *            the command to handle
	 * @param fishing
	 *            whether we're dealing with *fish* traps .. which take
	 *            different amounts of time
	 * @return how many minutes it took to execute the command
	 * @throws IOException
	 *             on I/O error interacting with user
	 */
	private int handleCommand(final List<String> fixtures,
			final Appendable ostream, final TrapperCommand command,
			final boolean fishing) throws IOException {
		switch (command) {
		case Check: // TODO: extract method?
			// ESCA-JAVA0177:
			final String top = fixtures.remove(0);
			if (HuntingModel.NOTHING.equals(top)) {
				ostream.append("Nothing in the trap\n");
				if (fishing) {
					return FRUITLESS_FISH_TRAP; // NOPMD
				} else {
					return FRUITLESS_TRAP; // NOPMD
				}
			} else {
				ostream.append("Found either ");
				ostream.append(top);
				ostream.append(" or evidence of it escaping.\n");
				return helper//NOPMD
						.inputNumber("How long to check and deal with animal? ");
			}
		case EasyReset:
			if (fishing) {
				return 20; // NOPMD
			} else {
				return 5; // NOPMD
			}
		case Move:
			return 2; // NOPMD
		case Quit:
			return 0; // NOPMD
		case Set:
			if (fishing) {
				return 30; // NOPMD
			} else {
				return 45;
			}
		default:
			throw new IllegalArgumentException("Unhandled case");
		}
	}
	/**
	 * Start the driver.
	 * @param model the driver model to operate on
	 * @throws DriverFailedException never?
	 */
	@Override
	public void startDriver(final IDriverModel model) throws DriverFailedException {
		repl(model.getMap(), SYS_OUT);
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
	 * Start this driver from another driver.
	 * @param map the map to operate on
	 * @deprecated in favor of the overload taking a DriverModel
	 */
	@Deprecated
	public void startDriver(final IMapNG map) {
		repl(map, SYS_OUT);
	}
	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE_OBJ;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TrapModelDriver";
	}
}
