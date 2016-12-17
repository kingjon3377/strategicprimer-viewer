package controller.map.drivers;

import controller.map.misc.DuplicateFixtureRemover;
import controller.map.misc.ICLIHelper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import model.map.IMutableMapNG;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import util.Pair;

/**
 * A driver to remove duplicate hills, forests, etc. from the map (to reduce the size it
 * takes up on disk and the memory and CPU it takes to deal with it).
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
public final class DuplicateFixtureRemoverCLI implements SimpleCLIDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-u", "--duplicates", ParamCount.One,
								   "Remove duplicate fixtures",
								   "Remove duplicate fixtures---identical except ID# " +
										   "and on the same tile---from a map.");

	static {
		USAGE.addSupportedOption("--current-turn=NN");
	}

	/**
	 * Run the driver.
	 *
	 * @param cli the interface for user I/O
	 * @param options command-line options passed in
	 * @param model   the driver model
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model)
			throws DriverFailedException {
		try {
			if (model instanceof IMultiMapModel) {
				for (final Pair<IMutableMapNG, Optional<Path>> pair :
						((IMultiMapModel) model).getAllMaps()) {
					DuplicateFixtureRemover.filter(pair.first(), cli);
				}
			} else {
				DuplicateFixtureRemover.filter(model.getMap(), cli);
			}
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			throw new DriverFailedException("I/O error interacting with user", except);
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
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "DuplicateFixtureRemover";
	}
}
