package controller.map.drivers;

import controller.map.drivers.DriverUsage.ParamCount;
import controller.map.misc.MapReaderAdapter;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.Subsettable;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import model.misc.SimpleMultiMapModel;
import util.NullCleaner;
import util.Pair;
import util.Warning;
import util.Warning.Action;

import static view.util.SystemOut.SYS_OUT;

/**
 * A driver to check whether player maps are subsets of the main map.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
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
public final class SubsetDriver implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-s",
			                                                            "--subset",
			                                                            ParamCount.Many,
			                                                            "Check players' " +
					                                                            "maps " +
					                                                            "against" +
					                                                            " master",

			                                                            "Check that " +
					                                                            "subordinate maps are subsets of the main map, containing "
					                                                            +
					                                                            "nothing" +
					                                                            " that " +
					                                                            "it does" +
					                                                            " not " +
					                                                            "contain" +
					                                                            " in the" +
					                                                            " same " +
					                                                            "place",
			                                                            SubsetDriver
					                                                            .class);
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			NullCleaner.assertNotNull(Logger.getLogger(SubsetDriver.class.getName()));

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
	}

	/**
	 * Run the driver.
	 *
	 * @param dmodel the driver model
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final IDriverModel dmodel) throws DriverFailedException {
		final IMultiMapModel model;
		if (dmodel instanceof IMultiMapModel) {
			model = (IMultiMapModel) dmodel;
		} else {
			model = new SimpleMultiMapModel(dmodel);
			LOGGER.warning("Subset checking does nothing with no subordinate maps");
		}
		for (final Pair<IMutableMapNG, File> pair : model.getSubordinateMaps()) {
			SYS_OUT.print(pair.second().getName());
			SYS_OUT.print("\t...\t\t");
			printReturn(doSubsetTest(model.getMap(), pair.first(), pair.second()));
		}
	}

	/**
	 * Run the driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException if the main map fails to load
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length < 2) {
			SYS_OUT.println("Usage: SubsetDriver mainMap playerMap [playerMap ...]");
			return;
		}
		final MapReaderAdapter reader = new MapReaderAdapter();
		final IMultiMapModel model = reader.readMultiMapModel(new Warning(Action.Ignore),
				new File(args[0]), MapReaderAdapter.namesToFiles(true, args));
		startDriver(model);
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
	private static Returns doSubsetTest(final Subsettable<IMapNG> mainMap, final IMapNG
			                                                                       map,
	                                    final File file) {
		try {
			if (mainMap.isSubset(map, SYS_OUT, "In " + file.getName() + ':')) {
				return Returns.OK;
			} else {
				System.out.flush();
				return Returns.Warn;
			}
		} catch (final IOException e) {
			Warning.INSTANCE.warn(e);
			return Returns.Fail;
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

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SubsetDriver";
	}
}
