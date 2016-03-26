package controller.map.drivers;

import controller.map.drivers.DriverUsage.ParamCount;
import controller.map.misc.FileChooser;
import controller.map.misc.FileChooser.ChoiceInterruptedException;
import controller.map.misc.IOHandler;
import java.io.File;
import java.io.IOException;
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
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
public class ResourceAddingGUIDriver implements SimpleDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(true, "-d", "--add-resource", ParamCount.Many,
					               "Add resources to maps",
					               "Add resources for players to maps",
					               ResourceAddingCLIDriver.class);

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
	 * @param model the driver-model that should be used by the app
	 * @throws DriverFailedException if the driver fails for some reason
	 */
	@Override
	public void startDriver(final IDriverModel model) throws DriverFailedException {
		final ResourceManagementDriver rmmodel;
		if (model instanceof ResourceManagementDriver) {
			rmmodel = (ResourceManagementDriver) model;
		} else {
			rmmodel = new ResourceManagementDriver(model);
		}
		SwingUtilities.invokeLater(
				() -> new ResourceAddingFrame(rmmodel, new IOHandler(rmmodel))
						      .setVisible(true));
	}

	/**
	 * Run the driver.
	 *
	 * @param args Command-line arguments.
	 * @throws DriverFailedException if the driver failed to run.
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		// TODO: File-choosing like this should be handled in SimpleDriver, for drivers
		// that need files.
		try {
			if (args.length == 0) {
				SimpleDriver.super.startDriver(
						new FileChooser(new File("")).getFile().getCanonicalPath());
			} else {
				SimpleDriver.super.startDriver(args);
			}
		} catch (final ChoiceInterruptedException except) {
			throw new DriverFailedException("File choice was interrupted or user didn't choose",
					                               except);
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error getting path of the chosen file",
					                               except);
		}
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
