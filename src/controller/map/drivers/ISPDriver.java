package controller.map.drivers;

import model.map.HasName;
import model.misc.IDriverModel;

/**
 * An interface for drivers, so one main() method can start different ones
 * depending on options.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public interface ISPDriver extends HasName {
	/**
	 * Run the driver. If the driver is a GUIDriver, this should use
	 * SwingUtilities.invokeLater(); if it's a CLIDriver, that's not necessary.
	 *
	 * @param args any command-line arguments that should be passed to the
	 *        driver.
	 * @throws DriverFailedException if it's impossible for the driver to start.
	 */
	void startDriver(String... args) throws DriverFailedException;

	/**
	 * Run the driver. This form is, at the moment, primarily for use in test
	 * code, but that may change. At the moment implementations should *not*
	 * interact with the filesystem, including calling methods that will.
	 *
	 * @param model
	 *            the driver-model that should be used by the app
	 * @throws DriverFailedException
	 *             if the driver fails for some reason
	 */
	void startDriver(IDriverModel model) throws DriverFailedException;

	/**
	 * @return an object indicating how to use and invoke the driver.
	 */
	DriverUsage usage();

}
