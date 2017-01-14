package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.exploration.HuntingModel;
import model.map.HasName;
import model.map.IMapNG;
import model.map.Point;
import model.misc.IDriverModel;
import util.TypesafeLogger;

/**
 * A driver to run a player's trapping activity.
 *
 * TODO: Tests
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class TrapModelDriver implements SimpleDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final IDriverUsage USAGE =
			new DriverUsage(false, "-r", "--trap", ParamCount.One,
								   "Run a player's trapping",
								   "Determine the results a player's trapper finds.");
	/**
	 * A somewhat lengthy prompt.
	 */
	private static final String FISH_OR_TRAP =
			"Is this a fisherman trapping fish rather than a trapper?";
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(TrapModelDriver.class);

	/**
	 * The number of minutes in an hour.
	 */
	private static final int MIN_PER_HOUR = 60;

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
	private static final List<TrapperCommand> COMMANDS =
			Collections.unmodifiableList(Arrays.asList(TrapperCommand.values()));

	/**
	 * Allow the user to interact with the driver.
	 * @param map the map to explore
	 * @param cli the interface to interact with the user
	 */
	private static void repl(final IMapNG map, final ICLIHelper cli) {
		try {
			final boolean fishing = cli.inputBooleanInSeries(FISH_OR_TRAP);
			final String name;
			if (fishing) {
				name = "fisherman";
			} else {
				name = "trapper";
			}
			int minutes = cli.inputNumber("How many hours will the " + name +
												  " work? ") * MIN_PER_HOUR;
			final Point point = cli.inputPoint("Where is the " + name + " working? ");
			final List<String> fixtures;
			final HuntingModel huntModel = new HuntingModel(map);
			if (fishing) {
				fixtures = huntModel.fish(point, minutes);
			} else {
				fixtures = huntModel.hunt(point, minutes);
			}
			int input = -1;
			while ((minutes > 0) && (input < TrapperCommand.values().length)) {
				if (input >= 0) {
					final TrapperCommand command = TrapperCommand.values()[input];
					minutes -= handleCommand(fixtures, cli,
							command, fishing);
					cli.print(inHours(minutes));
					cli.println(" remaining");
					if (command == TrapperCommand.Quit) {
						break;
					}
				}
				input = cli.chooseFromList(COMMANDS, "What should the "
															 + name + " do next?",
						"Oops! No commands",
						"Next action: ", false);
			}
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			LOGGER.log(Level.SEVERE, "I/O exception", except);
		}
	}

	/**
	 * Convert a number of minutes to an hours-and-minutes String.
	 * @param minutes a number of minutes
	 * @return a String representation, including the number of hours
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	private static String inHours(final int minutes) {
		if (minutes < MIN_PER_HOUR) {
			return Integer.toString(minutes) + " minutes";
		} else {
			return Integer.toString(minutes / MIN_PER_HOUR) + " hours, " +
						   Integer.toString(minutes % MIN_PER_HOUR) + " minutes";
		}
	}

	/**
	 * Handle a command.
	 *
	 * @param fixtures the animals generated from the tile and surrounding tiles.
	 * @param cli      the interface to interact with the user
	 * @param command  the command to handle
	 * @param fishing  whether we're dealing with *fish* traps .. which take different
	 *                 amounts of time
	 * @return how many minutes it took to execute the command
	 * @throws IOException on I/O error interacting with user
	 */
	private static int handleCommand(final List<String> fixtures,
									 final ICLIHelper cli, final TrapperCommand command,
									 final boolean fishing) throws IOException {
		switch (command) {
		case Check: // TODO: extract method?
			final String top = fixtures.remove(0);
			if (HuntingModel.NOTHING.equals(top)) {
				cli.println("Nothing in the trap");
				if (fishing) {
					return FRUITLESS_FISH_TRAP;
				} else {
					return FRUITLESS_TRAP;
				}
			} else {
				cli.printf("Found either %s or evidence of it escaping.%n", top);
				return cli.inputNumber("How long to check and deal with animal? ");
			}
		case EasyReset:
			if (fishing) {
				return 20;
			} else {
				return 5;
			}
		case Move:
			return 2;
		case Quit:
			return 0;
		case Set:
			if (fishing) {
				return 30;
			} else {
				return 45;
			}
		default:
			throw new IllegalArgumentException("Unhandled case");
		}
	}

	/**
	 * Start the driver.
	 *
	 * @param cli the interface for user I/O
	 * @param options command-line options passed in
	 * @param model   the driver model to operate on
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model) {
		repl(model.getMap(), cli);
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

	/**
	 * The possible commands.
	 */
	private enum TrapperCommand implements HasName {
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
		TrapperCommand(final String cName) {
			name = cName;
		}

		/**
		 * The "name" of the command.
		 * @return the "name" of the command
		 */
		@Override
		public String getName() {
			return name;
		}
	}
	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TrapModelDriver";
	}
}
