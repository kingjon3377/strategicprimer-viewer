package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import java.nio.file.Path;
import java.util.Formatter;
import java.util.Optional;
import java.util.logging.Logger;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.Subsettable;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import model.misc.SimpleMultiMapModel;
import util.NullCleaner;
import util.Pair;

import static view.util.SystemOut.SYS_OUT;

/**
 * A driver to check whether player maps are subsets of the main map.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class SubsetDriver implements SimpleDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final IDriverUsage USAGE =
			new DriverUsage(false, "-s", "--subset", ParamCount.AtLeastTwo,
								   "Check players' maps against master",
								   "Check that subordinate maps are subsets of the main" +
										   " map, containing nothing that it does not " +
										   "contain in the same place");
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			NullCleaner.assertNotNull(Logger.getLogger(SubsetDriver.class.getName()));

	/**
	 * Run the driver.
	 *
	 * @param cli the interface for user I/O
	 * @param options command-line options passed in
	 * @param model   the driver model
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model) {
		final IMultiMapModel mapModel;
		if (model instanceof IMultiMapModel) {
			mapModel = (IMultiMapModel) model;
		} else {
			mapModel = new SimpleMultiMapModel(model);
			LOGGER.warning("Subset checking does nothing with no subordinate maps");
		}
		for (final Pair<IMutableMapNG, Optional<Path>> pair :
				mapModel.getSubordinateMaps()) {
			final Optional<Path> filename = pair.second();
			if (filename.isPresent()) {
				SYS_OUT.print(filename.get());
				SYS_OUT.print("\t...\t\t");
				printReturn(
						doSubsetTest(mapModel.getMap(), pair.first(), filename.get()));
			} else {
				SYS_OUT.println("Map didn't have a filename; skipping ...");
			}
		}
	}

	/**
	 * Run the driver.
	 *
	 * @param cli the interface for user I/O
	 * @param options command-line options passed in
	 * @param args    command-line arguments
	 * @throws DriverFailedException if the main map fails to load
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final String... args)
			throws DriverFailedException {
		if (args.length < 2) {
			throw new IncorrectUsageException(usage());
		}
		SimpleDriver.super.startDriver(cli, options, args);
	}

	/**
	 * Print a Returns value to stdout.
	 *
	 * @param value the value to print.
	 */
	private static void printReturn(final Returns value) {
		switch (value) {
		case Fail:
			SYS_OUT.println("FAIL");
			break;
		case OK:
			SYS_OUT.println("OK");
			break;
		case Warn:
			SYS_OUT.println("WARN");
			break;
		default:
			throw new IllegalStateException("Can't get here");
		}
	}

	/**
	 * @param mainMap the main map
	 * @param map     a subordinate map
	 * @param file    the file the subordinate map came from
	 * @return the result of doing the subset test with those maps
	 */
	@SuppressWarnings("UseOfSystemOutOrSystemErr")
	private static Returns doSubsetTest(final Subsettable<IMapNG> mainMap,
										final IMapNG map, final Path file) {
		if (mainMap.isSubset(map, new Formatter(SYS_OUT), "In " + file + ':')) {
			return Returns.OK;
		} else {
			System.out.flush();
			return Returns.Warn;
		}
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public IDriverUsage usage() {
		return USAGE;
	}

	/**
	 * Possible return values for sub-maps.
	 */
	private enum Returns {
		/**
		 * The map is a subset.
		 */
		OK,
		/**
		 * The map isn't a subset.
		 */
		Warn,
		/**
		 * The map failed to load.
		 */
		Fail
	}	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "SubsetDriver";
	}
}
