package controller.map.drivers;

import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.ICLIHelper;
import controller.map.misc.IDFactoryFiller;
import controller.map.misc.IDRegistrar;
import controller.map.misc.MapReaderAdapter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.stream.XMLStreamException;
import model.map.IMutableMapNG;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.terrain.Forest;
import util.Warning;

/**
 * A driver that reads in maps and then writes them out again---this is primarily to make
 * sure that the map format is properly read, but is also useful for correcting deprecated
 * syntax. (Because of that usage, warnings are disabled.)
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
public final class EchoDriver implements UtilityDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-e", "--echo", ParamCount.Two,
								   "Read, then write a map.",
								   "Read and write a map, correcting deprecated syntax."
			);

	static {
		USAGE.addSupportedOption("--current-turn=NN");
		USAGE.setFirstParamDesc("input.xml");
		USAGE.setSubsequentParamDesc("output.xml");
	}

	/**
	 * Run the driver.
	 *
	 * @param cli
	 * @param options
	 * @param args    command-line arguments
	 * @throws DriverFailedException on error
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final String... args)
			throws DriverFailedException {
		if (args.length != 2) {
			throw new IncorrectUsageException(usage());
		}
		final IMutableMapNG map;
		final Path infile = Paths.get(args[0]);
		try {
			map =
					new MapReaderAdapter().readMap(infile, Warning.Ignore);
		} catch (final IOException | SPFormatException | XMLStreamException except) {
			throw new DriverFailedException(message(infile, except), except);
		}
		// Fix forest IDs in a way that shouldn't break consistency between main and
		// player maps too much.
		final IDRegistrar idFactory = IDFactoryFiller.createFactory(map);
		for (final Point location : map.locations()) {
			final Forest mainForest = map.getForest(location);
			if (mainForest != null && mainForest.getID() < 0) {
				final int id = 1147200 + location.getRow() * 176 + location.getCol();
				idFactory.register(id);
				mainForest.setID(id);
			}
			for (final TileFixture fix : map.getOtherFixtures(location)) {
				if (fix instanceof Forest && fix.getID() < 0) {
					((Forest) fix).setID(idFactory.createID());
				}
			}
		}
		if (options.hasOption("--current-turn")) {
			final int currentTurn =
					Integer.parseInt(options.getArgument("--current-turn"));
			map.setCurrentTurn(currentTurn);
		}
		final Path outfile = Paths.get(args[1]);
		try {
			new MapReaderAdapter().write(outfile, map);
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			throw new DriverFailedException("I/O error writing " + outfile, except);
		}
	}
	/**
	 * @param filename the name of the file being read or written
	 * @param except the exception being wrapped
	 * @return an appropriate message for the DriverFailedException
	 */
	private static String message(final Path filename, final Exception except) {
		if (except instanceof MapVersionException) {
			return "Unsupported map version";
		} else if (except instanceof IOException) {
			//noinspection HardcodedFileSeparator
			return "I/O error reading file " + filename;
		} else if (except instanceof XMLStreamException) {
			return "Malformed XML";
		} else if (except instanceof SPFormatException) {
			return "SP map format error";
		} else {
			return "Unknown error";
		}
	}
	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE;
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "EchoDriver";
	}
}
