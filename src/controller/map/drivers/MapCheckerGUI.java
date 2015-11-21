package controller.map.drivers;

import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import model.misc.IDriverModel;
import view.map.misc.MapCheckerFrame;

/**
 * A driver to check every map file in a list for errors and report the results
 * in a window.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
public class MapCheckerGUI implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(true, "-k",
			"--check", ParamCount.Many, "Check map for errors",
			"Check a map file for errors, deprecated syntax, etc.",
			MapCheckerGUI.class);
	/**
	 * @param model ignored
	 * @throws DriverFailedException always; this doesn't make sense on a driver model
	 */
	@Override
	public void startDriver(final IDriverModel model) throws DriverFailedException {
		throw new DriverFailedException(new IllegalStateException("The MapCheckerGUI doesn't make sense on a driver model"));
	}
	/**
	 * Run the driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException if not enough arguments
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		final MapCheckerFrame window = new MapCheckerFrame();
		window.setVisible(true);
		for (final String filename : args) {
			if (filename != null) {
				window.check(filename);
			}
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
		return "MapCheckerGUI";
	}
}
