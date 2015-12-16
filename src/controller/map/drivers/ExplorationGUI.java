package controller.map.drivers;

import static view.util.SystemOut.SYS_OUT;

import java.io.File;

import javax.swing.SwingUtilities;

import controller.map.drivers.DriverUsage.ParamCount;
import controller.map.misc.IOHandler;
import controller.map.misc.MapReaderAdapter;
import controller.map.misc.WindowThread;
import model.exploration.ExplorationModel;
import model.misc.IDriverModel;
import util.Warning;
import view.exploration.ExplorationFrame;
import view.map.main.MapFileFilter;
import view.util.FilteredFileChooser;

/**
 * A class to start the exploration GUI.
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
 */
public final class ExplorationGUI implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(true, "-x",
			"--explore", ParamCount.Many, "Run exploration.",
			"Move a unit around the map, "
					+ "updating the player's map with what it sees.",
					ExplorationGUI.class);

	/**
	 * Run the driver.
	 * @param dmodel the driver model
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final IDriverModel dmodel) throws DriverFailedException {
		final ExplorationModel model;
		if (dmodel instanceof ExplorationModel) {
			model = (ExplorationModel) dmodel;
		} else {
			model = new ExplorationModel(dmodel);
		}
		SwingUtilities.invokeLater(
				new WindowThread(new ExplorationFrame(model, new IOHandler(model,
						new FilteredFileChooser(".", new MapFileFilter())))));
	}
	/**
	 * Run the driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException if the driver failed to run
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			SYS_OUT.print("Usage: ");
			SYS_OUT.print(getClass().getSimpleName());
			SYS_OUT.println(" master-map [player-map ...]");
			System.exit(1);
		}
		final ExplorationModel model = new ExplorationModel(
				new MapReaderAdapter().readMultiMapModel(Warning.INSTANCE,
						new File(args[0]), MapReaderAdapter.namesToFiles(true, args)));
		SwingUtilities.invokeLater(new WindowThread(new ExplorationFrame(
				model, new IOHandler(model, new FilteredFileChooser(
						".", new MapFileFilter())))));
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
		return usage().getShortDescription();
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
		return "ExplorationGUI";
	}
}
