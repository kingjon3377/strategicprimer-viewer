package controller.map.drivers;

import controller.map.misc.MapReaderAdapter;
import java.io.File;
import util.Warning;

/**
 * An interface for drivers which operate on a map model of some kind.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
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
@FunctionalInterface
public interface SimpleDriver extends ISPDriver {
	/**
	 * Run the driver. If the driver is a GUIDriver, this should use
	 * SwingUtilities.invokeLater(); if it's a CLIDriver, that's not necessary. This
	 * default implementation does not write to file after running the driver on the
	 * driver model.
	 *
	 * @param args any command-line arguments that should be passed to the driver.
	 * @throws DriverFailedException if it's impossible for the driver to start.
	 */
	default void startDriver(final String... args) throws DriverFailedException {
		// FIXME: This blows up if args.length == 0
		startDriver(new MapReaderAdapter()
				            .readMultiMapModel(Warning.DEFAULT, new File(args[0]),
						            MapReaderAdapter.namesToFiles(true, args)));
	}
}
