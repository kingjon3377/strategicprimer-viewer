package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import controller.map.misc.IOHandler;
import javax.swing.SwingUtilities;
import model.misc.IDriverModel;
import model.workermgmt.IWorkerModel;
import model.workermgmt.WorkerModel;
import view.worker.WorkerMgmtFrame;

/**
 * A class to start the user worker management GUI.
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
public final class WorkerStart implements SimpleDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(true, "-w", "--worker", ParamCount.AtLeastOne,
								"Manage a player's workers in units",
								"Organize the members of a player's units."
			);
	static {
		USAGE.addSupportedOption("--current-turn=NN");
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE;
	}

	/**
	 * Run the driver. This form is, at the moment, primarily for use in test code, but
	 * that may change.
	 *
	 * @param cli
	 * @param options
	 * @param model the driver-model that should be used by the app
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model) {
		final IWorkerModel driverModel;
		if (model instanceof IWorkerModel) {
			driverModel = (IWorkerModel) model;
		} else {
			driverModel = new WorkerModel(model);
		}
		SwingUtilities.invokeLater(() -> new WorkerMgmtFrame(options, driverModel,
																	new IOHandler
																			(driverModel))
												 .setVisible(true));
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "WorkerStart";
	}
}
