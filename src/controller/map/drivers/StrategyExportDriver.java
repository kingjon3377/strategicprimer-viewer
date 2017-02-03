package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import controller.map.misc.StrategyExporter;
import java.nio.file.Paths;
import java.util.Collections;
import model.misc.IDriverModel;
import model.workermgmt.IWorkerModel;
import model.workermgmt.WorkerModel;

/**
 * A command-line program to export a proto-strategy for a player from orders in a map.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2017 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class StrategyExportDriver implements SimpleDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(true, "-w", "--worker", ParamCount.One,
								   "Export a proto-strategy",
								   "Create a proto-strategy using orders stored in the map");

	static {
		USAGE.addSupportedOption("--current-turn=NN");
		USAGE.addSupportedOption("--print-empty");
		USAGE.addSupportedOption("--export=filename.txt");
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
	 * Run the driver. This form is, at the moment, primarily for use in test code, but
	 * that may change. At the moment implementations should *not* interact with the
	 * filesystem, including calling methods that will.
	 *
	 * @param cli     the interface to interact with the console user
	 * @param options any options that were passed on the command line.
	 * @param model   the driver-model that should be used by the app
	 * @throws DriverFailedException if the driver fails for some reason
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model) throws DriverFailedException {
		if (options.hasOption("--export")) {
			final IWorkerModel workerModel;
			if (model instanceof IWorkerModel) {
				workerModel = (IWorkerModel) model;
			} else {
				workerModel = new WorkerModel(model);
			}
			new StrategyExporter(workerModel)
					.writeStrategy(Paths.get(options.getArgument("--export")), options,
							Collections.emptyList());
		} else {
			throw new DriverFailedException("--export option is required",
												   new IllegalStateException("--export " +
																					 "option is required"));
		}
	}
	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "StrategyExportDriver";
	}
}
