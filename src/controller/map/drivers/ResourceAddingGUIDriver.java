package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import controller.map.misc.MenuBroker;
import controller.map.misc.PlayerChangeMenuListener;
import javax.swing.SwingUtilities;
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
								   "Add resources for players to maps");

	static {
		USAGE.addSupportedOption("--current-turn=NN");
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
	 * that may change.
	 *
	 * @param cli the interface for user I/O
	 * @param options command-line options passed in
	 * @param model   the driver-model that should be used by the app
	 * @throws DriverFailedException if the driver fails for some reason
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model)
			throws DriverFailedException {
		final ResourceManagementDriver driverModel;
		if (model instanceof ResourceManagementDriver) {
			driverModel = (ResourceManagementDriver) model;
		} else {
			driverModel = new ResourceManagementDriver(model);
		}
		final PlayerChangeMenuListener pcml = new PlayerChangeMenuListener(driverModel);
		final MenuBroker menuHandler = new MenuBroker();
		menuHandler.register(pcml, "change current player");
		SwingUtilities.invokeLater(
				() -> {
					final ResourceAddingFrame frame =
							new ResourceAddingFrame(driverModel, menuHandler);
					pcml.addPlayerChangeListener(frame);
					frame.setVisible(true);
				});
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "WorkerStart";
	}
}
