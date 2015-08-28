package controller.map.drivers;

import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;

import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.WindowThread;
import view.map.misc.SubsetFrame;

/**
 * A driver to check whether player maps are subsets of the main map and display
 * the results graphically.
 *
 *
 * TODO: Unify with SubsetDriver somehow.
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public class SubsetGUIDriver implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(true, "-s",
			"--subset", ParamCount.Many, "Check players' maps against master",
			"Check that subordinate maps are subsets of the main map, containing "
					+ "nothing that it does not contain in the same place",
			SubsetGUIDriver.class);

	/**
	 * Run the driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException if the main map fails to load
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length < 2) {
			throw new DriverFailedException("Need at least two arguments",
					new IllegalArgumentException("Need at least two arguments"));
		}
		final SubsetFrame frame = new SubsetFrame();
		SwingUtilities.invokeLater(new WindowThread(frame));
		final File first = new File(args[0]);
		try {
			frame.loadMain(first);
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error loading main map "
					+ first.getPath(), except);
		} catch (final XMLStreamException except) {
			throw new DriverFailedException("XML error reading main map "
					+ first.getPath(), except);
		} catch (final SPFormatException except) {
			throw new DriverFailedException("Invalid SP XML in main map "
					+ first.getPath(), except);
		}
		for (final String arg : args) {
			if (arg.equals(args[0])) {
				continue;
			}
			frame.test(new File(arg));
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
		return "SubsetGUIDriver";
	}
}
