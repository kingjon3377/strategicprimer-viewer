package controller.map.drivers;

import controller.map.misc.IOHandler;
import javax.swing.*;
import model.misc.IDriverModel;
import model.resources.ResourceManagementDriver;
import view.resources.ResourceAddingFrame;

/**
 * A class to start the resource-entry app.
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
public class ResourceAddingGUIDriver implements SimpleDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(true, "-d", "--add-resource", ParamCount.AtLeastOne,
								"Add resources to maps",
								"Add resources for players to maps"
			);

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
	 * @param options
	 * @param model the driver-model that should be used by the app
	 * @throws DriverFailedException if the driver fails for some reason
	 */
	@Override
	public void startDriver(final SPOptions options, final IDriverModel model)
			throws DriverFailedException {
		final ResourceManagementDriver driverModel;
		if (model instanceof ResourceManagementDriver) {
			driverModel = (ResourceManagementDriver) model;
		} else {
			driverModel = new ResourceManagementDriver(model);
		}
		SwingUtilities.invokeLater(
				() -> new ResourceAddingFrame(driverModel, new IOHandler(driverModel))
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
